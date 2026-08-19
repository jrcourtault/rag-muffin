package fr.drjeanjean.ragmuffin.infra.embedding;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;

import java.util.List;

public interface EmbeddingService {

    DenseEmbedding embedDense(String text);

    List<DenseEmbedding> embedDenseBatch(List<String> texts);

    SparseEmbedding embedSparse(String text);

    List<SparseEmbedding> embedSparseBatch(List<String> texts);
}
