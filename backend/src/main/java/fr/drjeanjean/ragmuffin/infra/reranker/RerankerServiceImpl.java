package fr.drjeanjean.ragmuffin.infra.reranker;

import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import fr.drjeanjean.ragmuffin.infra.reranker.properties.RerankerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Profile("!test")
public class RerankerServiceImpl implements RerankerService {

    private final RestClient client;

    public RerankerServiceImpl(RerankerProperties properties) {
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public List<ScoredChunk> rerank(String query, List<ScoredChunk> chunks) {
        if (chunks.isEmpty()) {
            return chunks;
        }

        log.debug("Reranking {} chunks for query: {}", chunks.size(), query);

        var texts = chunks.stream().map(ScoredChunk::text).toList();
        var rerankerResult = doRerank(query, texts);

        var rerankedChunks = rerankerResult.stream()
                .sorted(Comparator.comparingDouble(RerankResult::score).reversed())
                .map(r -> {
                    var chunk = chunks.get(r.index());
                    return new ScoredChunk(
                            chunk.id(),
                            chunk.text(),
                            r.score(),
                            chunk.documentId(),
                            chunk.fileName(),
                            chunk.chunkIndex(),
                            chunk.workspaceId()
                    );
                })
                .toList();

        log.debug("Reranking done: top score={}", rerankerResult.stream()
                .mapToDouble(RerankResult::score).max().orElse(0));

        return rerankedChunks;
    }

    private List<RerankResult> doRerank(String query, List<String> texts) {
        return client.post()
                .uri("/rerank")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RerankRequest(query, texts, true))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    record RerankRequest(String query, List<String> texts, boolean truncate) {
    }

    record RerankResult(int index, double score) {
    }
}
