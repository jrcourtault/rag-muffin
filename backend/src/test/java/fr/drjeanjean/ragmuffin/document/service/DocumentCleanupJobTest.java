package fr.drjeanjean.ragmuffin.document.service;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.document.DocumentRepository;
import fr.drjeanjean.ragmuffin.document.DocumentStatus;
import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class DocumentCleanupJobTest {

    private static final UUID INDEXED_DOC_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-345678901234");
    private static final UUID PENDING_DOC_ID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-456789012345");
    private static final UUID ERROR_DOC_ID = UUID.fromString("e5f6a7b8-c9d0-1234-efab-567890123456");
    private static final String AGE_ALL_DOCUMENTS =
            "UPDATE documents SET created_at = now() - interval '7 hours', modified_at = now() - interval '7 hours'";
    private static final String AGE_ALL_DOCUMENTS_BEYOND_RETENTION =
            "UPDATE documents SET modified_at = now() - interval '8 days'";

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private final QdrantService qdrantService = mock(QdrantService.class);

    private DocumentCleanupJob job;

    @BeforeEach
    void setUp() {
        var properties = new CleanupProperties(7, "unused", Duration.ofHours(6), "unused");
        job = new DocumentCleanupJob(documentRepository, fileStorageService, qdrantService, properties);
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"},
            statements = AGE_ALL_DOCUMENTS_BEYOND_RETENTION)
    void shouldDeleteErrorDocumentAndItsVectors_whenOlderThanRetention() {
        job.cleanupErrorDocuments();

        assertThat(documentRepository.findById(ERROR_DOC_ID)).isEmpty();
        assertThat(documentRepository.findById(INDEXED_DOC_ID)).isPresent();
        verify(qdrantService).deleteByDocumentId(ERROR_DOC_ID);
        verifyNoMoreInteractions(qdrantService);
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"},
            statements = AGE_ALL_DOCUMENTS)
    void shouldMarkPendingDocumentAsError_whenOlderThanTimeout() {
        var before = OffsetDateTime.now();

        job.markStalePendingDocumentsAsError();

        var document = find(PENDING_DOC_ID);
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ERROR);
        assertThat(document.getModifiedAt()).isAfterOrEqualTo(before.minusSeconds(1));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"},
            statements = AGE_ALL_DOCUMENTS)
    void shouldNotTouchIndexedDocument_whenOlderThanTimeout() {
        job.markStalePendingDocumentsAsError();

        assertThat(find(INDEXED_DOC_ID).getStatus()).isEqualTo(DocumentStatus.INDEXED);
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldKeepPendingDocument_whenYoungerThanTimeout() {
        job.markStalePendingDocumentsAsError();

        assertThat(find(PENDING_DOC_ID).getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    private Document find(UUID id) {
        return documentRepository.findById(id).orElseThrow();
    }
}
