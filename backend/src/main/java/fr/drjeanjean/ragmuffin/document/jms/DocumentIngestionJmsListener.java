package fr.drjeanjean.ragmuffin.document.jms;

import fr.drjeanjean.ragmuffin.document.DocumentRepository;
import fr.drjeanjean.ragmuffin.document.dto.IngestionJmsMessage;
import fr.drjeanjean.ragmuffin.document.service.tool.ChunkingService;
import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.tika.TikaService;
import fr.drjeanjean.ragmuffin.infra.embedding.EmbeddingService;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.Chunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ChunkEmbeddings;
import fr.drjeanjean.ragmuffin.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DocumentIngestionJmsListener {

    private final TikaService tikaService;
    private final ChunkingService chunkingService;
    private final FileStorageService fileStorageService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final WorkspaceService workspaceService;
    private final DocumentRepository documentRepository;
    private final JsonMapper jsonMapper;

    @JmsListener(destination = "document-ingestion")
    @SneakyThrows
    public void onMessage(String json) {
        var message = jsonMapper.readValue(json, IngestionJmsMessage.class);
        var documentId = message.documentId();
        var workspaceId = message.workspaceId();
        var fileName = message.fileName();
        log.info("Starting ingestion for document {} ({})", documentId, fileName);

        var filePath = fileStorageService.resolvePath(workspaceId, documentId, message.extension());

        var workspace = workspaceService.findById(workspaceId);

        var text = tikaService.extract(filePath);
        if (text.isEmpty()) {
            throw new RuntimeException("No text extracted from " + fileName + " — Tesseract may not be installed (required for scanned PDFs)");
        }
        log.info("Document {} : {} characters extracted from {}", documentId, text.length(), fileName);

        var chunkTexts = chunkingService.chunk(text, workspace.getChunkSize(), workspace.getChunkOverlap());
        if (chunkTexts.isEmpty()) {
            throw new RuntimeException("No chunks generated from " + fileName);
        }
        log.info("Document {} : split into {} chunks", documentId, chunkTexts.size());

        var denseEmbeddings = embeddingService.embedDenseBatch(chunkTexts);
        var sparseEmbeddings = embeddingService.embedSparseBatch(chunkTexts);
        log.info("Document {} : {} dense + sparse embeddings computed", documentId, denseEmbeddings.size());

        var chunkEmbeddings = zipEmbeddings(denseEmbeddings, sparseEmbeddings);
        var chunks = toChunks(chunkTexts, workspaceId, documentId, fileName);
        qdrantService.store(chunks, chunkEmbeddings);
        log.info("Document {} : {} embedded chunks stored in Qdrant", documentId, chunks.size());

        documentRepository.findById(documentId).ifPresent(document -> {
            document.markIndexed(chunks.size());
            documentRepository.save(document);
        });
        log.info("Document {} indexed successfully", documentId);
    }

    @JmsListener(destination = "DLQ", selector = "_AMQ_ORIG_ADDRESS = 'document-ingestion'")
    @SneakyThrows
    public void onDlq(String json) {
        var message = jsonMapper.readValue(json, IngestionJmsMessage.class);
        log.error("Ingestion definitively failed for document {} after all retries", message.documentId());
        documentRepository.findById(message.documentId()).ifPresent(document -> {
            document.markError();
            documentRepository.save(document);
        });
    }

    private static List<ChunkEmbeddings> zipEmbeddings(List<DenseEmbedding> dense, List<SparseEmbedding> sparse) {
        var result = new ArrayList<ChunkEmbeddings>(dense.size());
        var denseIt = dense.iterator();
        var sparseIt = sparse.iterator();
        while (denseIt.hasNext()) {
            result.add(new ChunkEmbeddings(denseIt.next(), sparseIt.next()));
        }
        return result;
    }

    private ArrayList<Chunk> toChunks(List<String> chunkTexts, UUID workspaceId, UUID documentId, String fileName) {
        var chunks = new ArrayList<Chunk>(chunkTexts.size());
        var i = 0;
        for (var chunkText : chunkTexts) {
            chunks.add(new Chunk(chunkText, workspaceId, documentId, fileName, i));
            i++;
        }
        return chunks;
    }
}
