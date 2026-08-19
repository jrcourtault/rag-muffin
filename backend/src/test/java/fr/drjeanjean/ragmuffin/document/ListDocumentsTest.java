package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class ListDocumentsTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_A = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String WORKSPACE_B = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturnDocumentsForWorkspace() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].fileName").exists())
                .andExpect(jsonPath("$.content[0].contentType").exists())
                .andExpect(jsonPath("$.content[0].sizeBytes").exists())
                .andExpect(jsonPath("$.content[0].status").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.content[0].modifiedAt").doesNotExist());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturnOnlyDocumentsOfRequestedWorkspace() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_B)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fileName").value("Manuel parachute.pdf"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturnEmptyList_whenNoDocuments() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldFilterByExtension() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .param("extension", "pdf")
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].extension").value("pdf"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .param("status", "ERROR")
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fileName").value("Reglement copropriete.docx"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldFilterByContentType() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .param("contentType", "application/pdf")
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fileName").value("Contrat de bail.pdf"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldCombineFilters() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A)
                        .param("extension", "pdf")
                        .param("status", "INDEXED")
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].fileName").value("Contrat de bail.pdf"));
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents", WORKSPACE_A))
                .andExpect(status().isUnauthorized());
    }
}
