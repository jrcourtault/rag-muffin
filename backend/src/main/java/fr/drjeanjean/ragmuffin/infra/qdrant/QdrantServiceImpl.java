package fr.drjeanjean.ragmuffin.infra.qdrant;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.Chunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ChunkEmbeddings;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.properties.QdrantProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.CreateCollection;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.SparseVectorConfig;
import io.qdrant.client.grpc.Collections.SparseVectorParams;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Collections.VectorParamsMap;
import io.qdrant.client.grpc.Collections.VectorsConfig;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.PrefetchQuery;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.WithPayloadSelector;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.QueryFactory.fusion;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorFactory.vector;
import static io.qdrant.client.VectorsFactory.namedVectors;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class QdrantServiceImpl implements QdrantService {

    private static final String DENSE_VECTOR_NAME = "dense";
    private static final String SPARSE_VECTOR_NAME = "sparse";
    private static final int DENSE_VECTOR_SIZE = 1024; // BGE-M3

    private final QdrantClient qdrantClient;
    private final QdrantProperties qdrantProperties;

    @PostConstruct
    @SneakyThrows
    void ensureCollectionExists() {
        var collections = qdrantClient.listCollectionsAsync().get();
        if (collections.contains(qdrantProperties.collectionName())) {
            log.info("Qdrant collection '{}' already exists", qdrantProperties.collectionName());
            return;
        }

        log.info("Creating Qdrant collection '{}' with dense + sparse vectors", qdrantProperties.collectionName());
        qdrantClient.createCollectionAsync(
                CreateCollection.newBuilder()
                        .setCollectionName(qdrantProperties.collectionName())
                        .setVectorsConfig(
                                VectorsConfig.newBuilder()
                                        .setParamsMap(
                                                VectorParamsMap.newBuilder()
                                                        .putMap(DENSE_VECTOR_NAME,
                                                                VectorParams.newBuilder()
                                                                        .setSize(DENSE_VECTOR_SIZE)
                                                                        .setDistance(Distance.Cosine)
                                                                        .build())
                                                        .build())
                                        .build())
                        .setSparseVectorsConfig(
                                SparseVectorConfig.newBuilder()
                                        .putMap(SPARSE_VECTOR_NAME,
                                                SparseVectorParams.newBuilder()
                                                        .build())
                                        .build())
                        .build()
        ).get();
        log.info("Qdrant collection '{}' created", qdrantProperties.collectionName());
    }

    @Override
    @SneakyThrows
    public void store(List<Chunk> chunks, List<ChunkEmbeddings> embeddings) {
        if (chunks.isEmpty()) {
            return;
        }
        if (chunks.size() != embeddings.size()) {
            throw new RuntimeException("The number of chunks should be equal to the number of embeddings");
        }

        var points = new ArrayList<PointStruct>(chunks.size());
        var chunkIt = chunks.iterator();
        var embIt = embeddings.iterator();
        while (chunkIt.hasNext()) {
            var chunk = chunkIt.next();
            var emb = embIt.next();
            var dense = emb.dense();
            var sparse = emb.sparse();

            var point = PointStruct.newBuilder()
                    .setId(id(UUID.randomUUID()))
                    .setVectors(
                            namedVectors(Map.of(
                                    DENSE_VECTOR_NAME, vector(toFloatList(dense.vector())),
                                    SPARSE_VECTOR_NAME, vector(sparse.values(), sparse.indices())
                            )))
                    .putAllPayload(Map.of(
                            "text", value(chunk.text()),
                            "workspace_id", value(chunk.workspaceId().toString()),
                            "document_id", value(chunk.documentId().toString()),
                            "file_name", value(chunk.fileName()),
                            "chunk_index", value(chunk.chunkIndex())
                    ))
                    .build();
            points.add(point);
        }

        qdrantClient.upsertAsync(qdrantProperties.collectionName(), points).get();
    }

    @Override
    @SneakyThrows
    public List<ScoredChunk> search(DenseEmbedding denseVector, SparseEmbedding sparseVector, UUID workspaceId, int topK) {
        var filter = Filter.newBuilder()
                .addMust(matchKeyword("workspace_id", workspaceId.toString()))
                .build();

        var prefetchLimit = topK * 2;

        var queryRequest = QueryPoints.newBuilder()
                .setCollectionName(qdrantProperties.collectionName())
                .addPrefetch(PrefetchQuery.newBuilder()
                        .setQuery(nearest(toFloatList(denseVector.vector())))
                        .setUsing(DENSE_VECTOR_NAME)
                        .setFilter(filter)
                        .setLimit(prefetchLimit)
                        .build())
                .addPrefetch(PrefetchQuery.newBuilder()
                        .setQuery(nearest(sparseVector.values(), sparseVector.indices()))
                        .setUsing(SPARSE_VECTOR_NAME)
                        .setFilter(filter)
                        .setLimit(prefetchLimit)
                        .build())
                .setQuery(fusion(io.qdrant.client.grpc.Points.Fusion.RRF))
                .setFilter(filter)
                .setLimit(topK)
                .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        var results = qdrantClient.queryAsync(queryRequest).get();

        return results.stream()
                .map(QdrantServiceImpl::toScoredChunk)
                .toList();
    }

    @Override
    @SneakyThrows
    public void deleteByDocumentId(UUID documentId) {
        var filter = Filter.newBuilder()
                .addMust(matchKeyword("document_id", documentId.toString()))
                .build();

        qdrantClient.deleteAsync(qdrantProperties.collectionName(), filter).get();
    }

    @Override
    @SneakyThrows
    public void deleteByWorkspaceId(UUID workspaceId) {
        var filter = Filter.newBuilder()
                .addMust(matchKeyword("workspace_id", workspaceId.toString()))
                .build();

        qdrantClient.deleteAsync(qdrantProperties.collectionName(), filter).get();
    }

    private static ScoredChunk toScoredChunk(ScoredPoint point) {
        var payload = point.getPayloadMap();
        return new ScoredChunk(
                UUID.fromString(point.getId().getUuid()),
                payload.get("text").getStringValue(),
                point.getScore(),
                UUID.fromString(payload.get("document_id").getStringValue()),
                payload.get("file_name").getStringValue(),
                (int) payload.get("chunk_index").getIntegerValue(),
                UUID.fromString(payload.get("workspace_id").getStringValue())
        );
    }

    private static List<Float> toFloatList(float[] array) {
        var list = new ArrayList<Float>(array.length);
        for (var f : array) {
            list.add(f);
        }
        return list;
    }
}
