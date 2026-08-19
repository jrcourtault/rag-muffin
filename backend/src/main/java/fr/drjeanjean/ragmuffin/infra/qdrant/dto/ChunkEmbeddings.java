package fr.drjeanjean.ragmuffin.infra.qdrant.dto;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;

public record ChunkEmbeddings(DenseEmbedding dense, SparseEmbedding sparse) {
}
