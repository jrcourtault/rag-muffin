package fr.drjeanjean.ragmuffin.infra.qdrant;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.Chunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ChunkEmbeddings;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Profile("test")
public class QdrantServiceMock implements QdrantService {

    @Override
    public void store(List<Chunk> chunks, List<ChunkEmbeddings> embeddings) {
        // No-op
    }

    @Override
    public List<ScoredChunk> search(DenseEmbedding denseVector, SparseEmbedding sparseVector, UUID workspaceId, int topK) {
        return List.of();
    }

    @Override
    public void deleteByDocumentId(UUID documentId) {
        // No-op
    }

    @Override
    public void deleteByWorkspaceId(UUID workspaceId) {
        // No-op
    }
}
