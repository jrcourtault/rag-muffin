package fr.drjeanjean.ragmuffin.infra.embedding;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class EmbeddingServiceMock implements EmbeddingService {

    @Override
    public DenseEmbedding embedDense(String text) {
        return new DenseEmbedding(new float[1024]);
    }

    @Override
    public List<DenseEmbedding> embedDenseBatch(List<String> texts) {
        return texts.stream()
                .map(_ -> new DenseEmbedding(new float[1024]))
                .toList();
    }

    @Override
    public SparseEmbedding embedSparse(String text) {
        return new SparseEmbedding(List.of(), List.of());
    }

    @Override
    public List<SparseEmbedding> embedSparseBatch(List<String> texts) {
        return texts.stream()
                .map(_ -> new SparseEmbedding(List.of(), List.of()))
                .toList();
    }
}
