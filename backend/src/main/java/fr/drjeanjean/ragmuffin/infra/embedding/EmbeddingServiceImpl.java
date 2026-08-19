package fr.drjeanjean.ragmuffin.infra.embedding;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.properties.EmbeddingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.http.client.JdkClientHttpRequestFactory;

@Slf4j
@Service
@Profile("!test")
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int MAX_BATCH_SIZE = 32;

    private final RestClient client;

    public EmbeddingServiceImpl(EmbeddingProperties properties) {
        // HTTP/1.1 forcé : le HttpClient JDK envoie par défaut un Upgrade h2c
        // que les serveurs ASGI (uvicorn, hypercorn) ne gèrent pas correctement
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    // ── Dense embeddings ──

    @Override
    public DenseEmbedding embedDense(String text) {
        return embedDenseBatch(List.of(text)).getFirst();
    }

    @Override
    public List<DenseEmbedding> embedDenseBatch(List<String> texts) {
        return embedInBatches(texts, this::doDenseEmbed);
    }

    private List<DenseEmbedding> doDenseEmbed(List<String> texts) {
        var request = new EmbedRequest(texts);

        log.debug("Dense embedding request: texts={}", texts.size());

        // BGE-M3 /embed_dense returns List<List<Float>> — one vector per input text
        var response = client.post()
                .uri("/embed_dense")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<float[]>>() {});

        log.debug("Dense embedding response: {} vectors", response.size());

        return response.stream()
                .map(DenseEmbedding::new)
                .toList();
    }

    // ── Sparse embeddings ──

    @Override
    public SparseEmbedding embedSparse(String text) {
        return embedSparseBatch(List.of(text)).getFirst();
    }

    @Override
    public List<SparseEmbedding> embedSparseBatch(List<String> texts) {
        return embedInBatches(texts, this::doSparseEmbed);
    }

    private List<SparseEmbedding> doSparseEmbed(List<String> texts) {
        var request = new EmbedRequest(texts);

        log.debug("Sparse embedding request: texts={}", texts.size());

        // BGE-M3 /embed_sparse returns List<List<SparseEntry>> — one list of entries per input text
        var response = client.post()
                .uri("/embed_sparse")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<List<SparseEntry>>>() {});

        log.debug("Sparse embedding response: {} vectors", response.size());

        return response.stream()
                .map(EmbeddingServiceImpl::toSparseEmbedding)
                .toList();
    }

    private static SparseEmbedding toSparseEmbedding(List<SparseEntry> entries) {
        var indices = new ArrayList<Integer>(entries.size());
        var values = new ArrayList<Float>(entries.size());
        for (var entry : entries) {
            indices.add(entry.index());
            values.add(entry.value());
        }
        return new SparseEmbedding(indices, values);
    }

    // ── Batching ──

    private <T> List<T> embedInBatches(List<String> texts, Function<List<String>, List<T>> embedFn) {
        if (texts.size() <= MAX_BATCH_SIZE) {
            return embedFn.apply(texts);
        }

        log.debug("Splitting {} texts into batches of {}", texts.size(), MAX_BATCH_SIZE);
        var result = new ArrayList<T>(texts.size());
        for (int i = 0; i < texts.size(); i += MAX_BATCH_SIZE) {
            var batch = texts.subList(i, Math.min(i + MAX_BATCH_SIZE, texts.size()));
            result.addAll(embedFn.apply(batch));
        }
        return result;
    }

    // ── Request/Response records (format serveur BGE-M3) ──

    record EmbedRequest(List<String> inputs) {}

    record SparseEntry(int index, float value) {}
}
