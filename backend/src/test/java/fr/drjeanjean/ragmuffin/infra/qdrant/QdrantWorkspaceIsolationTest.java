package fr.drjeanjean.ragmuffin.infra.qdrant;

import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.Chunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ChunkEmbeddings;
import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import fr.drjeanjean.ragmuffin.infra.qdrant.properties.QdrantProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Vérifie l'isolation entre workspaces sur un vrai Qdrant : tous les chunks ont les mêmes vecteurs,
 * donc seul le filtre workspace_id peut empêcher un chunk d'un autre client de remonter.
 */
class QdrantWorkspaceIsolationTest {

    private static final int GRPC_PORT = 6334;
    private static final int HTTP_PORT = 6333;
    private static final int DENSE_SIZE = 1024;
    private static final int TOP_K = 10;

    private static final GenericContainer<?> QDRANT = new GenericContainer<>(DockerImageName.parse("qdrant/qdrant:v1.17.0"))
            .withExposedPorts(GRPC_PORT, HTTP_PORT)
            .waitingFor(Wait.forHttp("/readyz").forPort(HTTP_PORT));

    private static final DenseEmbedding DENSE = denseVector();
    private static final SparseEmbedding SPARSE = new SparseEmbedding(List.of(1, 2), List.of(1f, 1f));

    private static QdrantClient client;
    private static QdrantServiceImpl qdrantService;

    @BeforeAll
    static void startQdrant() {
        QDRANT.start();
        var host = QDRANT.getHost();
        var port = QDRANT.getMappedPort(GRPC_PORT);
        client = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
        var properties = new QdrantProperties(host, port, "", "documents", Duration.ofSeconds(30));
        qdrantService = new QdrantServiceImpl(client, properties);
        qdrantService.initCollection();
    }

    @AfterAll
    static void stopQdrant() {
        client.close();
        QDRANT.stop();
    }

    @Test
    void shouldOnlyReturnChunksOfSearchedWorkspace() {
        var workspaceA = UUID.randomUUID();
        var workspaceB = UUID.randomUUID();
        var documentA = store(workspaceA, "secret-dupont.pdf");
        var documentB = store(workspaceB, "secret-martin.pdf");

        assertThat(search(workspaceA)).singleElement().satisfies(chunk -> {
            assertThat(chunk.workspaceId()).isEqualTo(workspaceA);
            assertThat(chunk.documentId()).isEqualTo(documentA);
        });
        assertThat(search(workspaceB)).singleElement().satisfies(chunk -> {
            assertThat(chunk.workspaceId()).isEqualTo(workspaceB);
            assertThat(chunk.documentId()).isEqualTo(documentB);
        });
    }

    @Test
    void shouldReturnNothing_whenWorkspaceHasNoDocument() {
        store(UUID.randomUUID(), "contrat.pdf");

        assertThat(search(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldOnlyDeleteChunksOfGivenWorkspace() {
        var workspaceA = UUID.randomUUID();
        var workspaceB = UUID.randomUUID();
        store(workspaceA, "contrat-a.pdf");
        store(workspaceB, "contrat-b.pdf");

        qdrantService.deleteByWorkspaceId(workspaceA);

        assertThat(search(workspaceA)).isEmpty();
        assertThat(search(workspaceB)).hasSize(1);
    }

    @Test
    void shouldOnlyDeleteChunksOfGivenDocument() {
        var workspace = UUID.randomUUID();
        var deletedDocument = store(workspace, "supprime.pdf");
        var keptDocument = store(workspace, "conserve.pdf");

        qdrantService.deleteByDocumentId(deletedDocument);

        assertThat(search(workspace)).singleElement()
                .satisfies(chunk -> assertThat(chunk.documentId()).isEqualTo(keptDocument));
    }

    @Test
    void shouldRenameChunks_ofGivenDocument() {
        var workspace = UUID.randomUUID();
        var document = store(workspace, "ancien-nom.pdf");

        qdrantService.updateFileName(document, workspace, "nouveau-nom.pdf");

        assertThat(search(workspace)).singleElement()
                .satisfies(chunk -> assertThat(chunk.fileName()).isEqualTo("nouveau-nom.pdf"));
    }

    @Test
    void shouldNotRenameChunks_throughAnotherWorkspace() {
        var workspaceA = UUID.randomUUID();
        var workspaceB = UUID.randomUUID();
        var documentA = store(workspaceA, "nom-origine.pdf");

        qdrantService.updateFileName(documentA, workspaceB, "pirate.pdf");

        assertThat(search(workspaceA)).singleElement()
                .satisfies(chunk -> assertThat(chunk.fileName()).isEqualTo("nom-origine.pdf"));
    }

    @Test
    void shouldStartAgain_whenCollectionAndIndexesAlreadyExist() {
        assertThatCode(() -> qdrantService.initCollection()).doesNotThrowAnyException();
    }

    private static UUID store(UUID workspaceId, String fileName) {
        var documentId = UUID.randomUUID();
        var chunk = new Chunk("Le bailleur doit réparer", workspaceId, documentId, fileName, 0);
        qdrantService.store(List.of(chunk), List.of(new ChunkEmbeddings(DENSE, SPARSE)));
        return documentId;
    }

    private static List<ScoredChunk> search(UUID workspaceId) {
        return qdrantService.search(DENSE, SPARSE, workspaceId, TOP_K);
    }

    private static DenseEmbedding denseVector() {
        var vector = new float[DENSE_SIZE];
        Arrays.fill(vector, 1f);
        return new DenseEmbedding(vector);
    }
}
