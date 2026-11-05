package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.util.UUID;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class DownloadDocumentTest {

    private static final String URL = "/api/workspaces/{workspaceId}/documents/{id}/download";
    private static final String WORKSPACE_A = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String INDEXED_DOC_ID = "c3d4e5f6-a7b8-9012-cdef-345678901234";   // workspace A
    private static final String PENDING_DOC_ID = "d4e5f6a7-b8c9-0123-defa-456789012345";   // workspace B

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldDownloadFile() throws Exception {
        var filePath = fileStorageService.resolvePath(
                UUID.fromString(WORKSPACE_A), UUID.fromString(INDEXED_DOC_ID), "pdf");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "contenu du pdf");

        try {
            mockMvc.perform(get(URL, WORKSPACE_A, INDEXED_DOC_ID).with(jwt(SUBJECT)))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''Contrat%20de%20bail.pdf"))
                    .andExpect(content().string("contenu du pdf"));
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturn404_whenFileIsMissingOnDisk() throws Exception {
        mockMvc.perform(get(URL, WORKSPACE_A, INDEXED_DOC_ID).with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturn404_whenDocumentBelongsToAnotherWorkspace() throws Exception {
        mockMvc.perform(get(URL, WORKSPACE_A, PENDING_DOC_ID).with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturn403_whenUserIsNotMemberOfWorkspace() throws Exception {
        mockMvc.perform(get(URL, WORKSPACE_A, INDEXED_DOC_ID).with(jwt(UUID.randomUUID().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get(URL, WORKSPACE_A, INDEXED_DOC_ID))
                .andExpect(status().isUnauthorized());
    }
}
