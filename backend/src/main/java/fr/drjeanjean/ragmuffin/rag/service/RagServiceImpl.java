package fr.drjeanjean.ragmuffin.rag.service;

import fr.drjeanjean.ragmuffin.infra.embedding.EmbeddingService;
import fr.drjeanjean.ragmuffin.infra.llm.LlmService;
import fr.drjeanjean.ragmuffin.infra.llm.dto.LlmConfig;
import fr.drjeanjean.ragmuffin.infra.llm.dto.Message;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import fr.drjeanjean.ragmuffin.infra.reranker.RerankerService;
import fr.drjeanjean.ragmuffin.rag.dto.AskResponse;
import fr.drjeanjean.ragmuffin.rag.dto.RagMapper;
import fr.drjeanjean.ragmuffin.rag.dto.SearchResponse;
import fr.drjeanjean.ragmuffin.workspace.Workspace;
import fr.drjeanjean.ragmuffin.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final EmbeddingService embeddingService;
    private final LlmService llmService;
    private final QdrantService qdrantService;
    private final RerankerService rerankerService;
    private final WorkspaceService workspaceService;

    @Override
    public AskResponse ask(String question, UUID workspaceId, boolean queryRewriting) {
        var workspace = workspaceService.findById(workspaceId);
        var config = llmConfig(workspace);

        var rewrittenQuestion = queryRewriting ? rewriteQuestion(question, workspace, config) : null;
        var chunks = retrieveChunks(rewrittenQuestion != null ? rewrittenQuestion : question, workspace);

        var context = formatContext(chunks);
        var messages = List.of(
                Message.system(workspace.getVertical().getSystemPrompt()),
                Message.user(context + "\n\nQuestion : " + question)
        );
        var answer = llmService.chat(messages, config);

        log.debug("Prompt : {}", workspace.getVertical().getSystemPrompt());
        log.debug("Documents : {}", context + "\n\nQuestion : " + question);
        log.debug("Answer {}", answer);

        return RagMapper.INSTANCE.toAskResponse(answer, rewrittenQuestion, chunks);
    }

    @Override
    public SearchResponse search(String question, UUID workspaceId, boolean queryRewriting) {
        var workspace = workspaceService.findById(workspaceId);
        var config = llmConfig(workspace);

        var rewrittenQuestion = queryRewriting ? rewriteQuestion(question, workspace, config) : null;
        var chunks = retrieveChunks(rewrittenQuestion != null ? rewrittenQuestion : question, workspace);

        log.debug("Search returned {} chunks for question on workspace={}", chunks.size(), workspaceId);

        return RagMapper.INSTANCE.toSearchResponse(rewrittenQuestion, chunks);
    }

    private List<ScoredChunk> retrieveChunks(String question, Workspace workspace) {
        var dense = embeddingService.embedDense(question);
        var sparse = embeddingService.embedSparse(question);

        if (!workspace.isRerank()) {
            return qdrantService.search(dense, sparse, workspace.getId(), workspace.getTopK());
        } else {
            var candidates = qdrantService.search(dense, sparse, workspace.getId(), workspace.getPrefetchSize());
            log.debug("Fetched {} candidates for reranking", candidates.size());
            var reranked = rerankerService.rerank(question, candidates);
            return reranked.stream().limit(workspace.getTopK()).toList();
        }
    }

    private String rewriteQuestion(String question, Workspace workspace, LlmConfig config) {
        var messages = List.of(
                Message.system(workspace.getVertical().getQueryRewritePrompt()),
                Message.user(question)
        );
        var rewritten = llmService.chat(messages, config).strip();
        log.debug("Query rewrite: '{}' → '{}'", question, rewritten);
        return rewritten;
    }

    private static LlmConfig llmConfig(Workspace workspace) {
        return new LlmConfig(workspace.getLlmBaseUrl(), workspace.getLlmApiKey(), workspace.getLlmModel());
    }

    private static String formatContext(List<ScoredChunk> chunks) {
        if (chunks.isEmpty()) {
            return "Aucun document pertinent trouvé.";
        }
        var sb = new StringBuilder("<documents>\n");
        for (int i = 0; i < chunks.size(); i++) {
            var chunk = chunks.get(i);
            sb.append("  <document index=\"").append(i + 1).append("\">\n");
            sb.append("    <source>").append(escapeXml(chunk.fileName())).append("</source>\n");
            sb.append("    <chunk>").append(chunk.chunkIndex()).append("</chunk>\n");
            sb.append("    <document_content>\n");
            sb.append(escapeXml(chunk.text())).append("\n");
            sb.append("    </document_content>\n");
            sb.append("  </document>\n");
        }
        sb.append("</documents>");
        return sb.toString();
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
