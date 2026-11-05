package fr.drjeanjean.ragmuffin.document.jms;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.document.DocumentRepository;
import fr.drjeanjean.ragmuffin.document.DocumentStatus;
import fr.drjeanjean.ragmuffin.document.service.tool.ChunkingService;
import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.embedding.EmbeddingService;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.DenseEmbedding;
import fr.drjeanjean.ragmuffin.infra.embedding.dto.SparseEmbedding;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import fr.drjeanjean.ragmuffin.infra.tika.TikaService;
import fr.drjeanjean.ragmuffin.workspace.Workspace;
import fr.drjeanjean.ragmuffin.workspace.service.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionJmsListenerTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-345678901234");
    private static final UUID WORKSPACE_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String MESSAGE = """
            {
              "documentId": "c3d4e5f6-a7b8-9012-cdef-345678901234",
              "workspaceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
              "fileName": "contrat.pdf",
              "extension": "pdf"
            }
            """;

    @Mock
    private TikaService tikaService;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private QdrantService qdrantService;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private DocumentRepository documentRepository;

    private final Document document = Document.builder()
            .name("contrat")
            .extension("pdf")
            .contentType("application/pdf")
            .sizeBytes(10)
            .build();

    private DocumentIngestionJmsListener listener;

    @BeforeEach
    void setUp() {
        listener = new DocumentIngestionJmsListener(tikaService, new ChunkingService(), fileStorageService,
                embeddingService, qdrantService, workspaceService, documentRepository, JsonMapper.builder().build());
    }

    @Test
    void shouldMarkDocumentIndexed() {
        givenWorkspaceWithChunking();
        givenSuccessfulEmbeddings();
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        listener.onMessage(MESSAGE);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.INDEXED);
        assertThat(document.getChunkCount()).isEqualTo(1);
        verify(qdrantService).store(any(), any());
        verify(qdrantService, never()).deleteByDocumentId(any());
    }

    @Test
    void shouldMarkErrorWithoutRetry_whenNoTextIsExtracted() {
        when(workspaceService.findById(WORKSPACE_ID)).thenReturn(mock(Workspace.class));
        when(tikaService.extract(any())).thenReturn("");
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        listener.onMessage(MESSAGE);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ERROR);
        verifyNoInteractions(embeddingService, qdrantService);
    }

    @Test
    void shouldLetBrokerRetry_whenFailureIsTransient() {
        givenWorkspaceWithChunking();
        when(tikaService.extract(any())).thenReturn("Un document avec du texte");
        when(embeddingService.embedDenseBatch(any())).thenThrow(new RuntimeException("Embedding server down"));

        assertThatThrownBy(() -> listener.onMessage(MESSAGE)).isInstanceOf(RuntimeException.class);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING);
        verifyNoInteractions(qdrantService);
    }

    @Test
    void shouldRemoveVectors_whenDocumentIsDeletedDuringIngestion() {
        givenWorkspaceWithChunking();
        givenSuccessfulEmbeddings();
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.empty());

        listener.onMessage(MESSAGE);

        verify(qdrantService).deleteByDocumentId(DOCUMENT_ID);
    }

    @Test
    void shouldMarkError_whenMessageReachesDlq() {
        when(documentRepository.findById(DOCUMENT_ID)).thenReturn(Optional.of(document));

        listener.onDlq(MESSAGE);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ERROR);
        verify(documentRepository).save(document);
    }

    private void givenWorkspaceWithChunking() {
        var workspace = mock(Workspace.class);
        when(workspace.getChunkSize()).thenReturn(100);
        when(workspace.getChunkOverlap()).thenReturn(0);
        when(workspaceService.findById(WORKSPACE_ID)).thenReturn(workspace);
    }

    private void givenSuccessfulEmbeddings() {
        when(tikaService.extract(any())).thenReturn("Un document avec du texte");
        when(embeddingService.embedDenseBatch(any())).thenReturn(List.of(new DenseEmbedding(new float[]{1f})));
        when(embeddingService.embedSparseBatch(any())).thenReturn(List.of(new SparseEmbedding(List.of(1), List.of(1f))));
    }
}
