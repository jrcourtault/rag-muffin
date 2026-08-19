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

import java.util.UUID;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class DeleteDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_A = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String WORKSPACE_B = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String INDEXED_DOC_ID = "c3d4e5f6-a7b8-9012-cdef-345678901234";   // workspace A
    private static final String PENDING_DOC_ID = "d4e5f6a7-b8c9-0123-defa-456789012345";   // workspace B

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldDeleteDocument_whenIndexed() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldNotDeleteDocument_whenPending() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_B, PENDING_DOC_ID)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, UUID.randomUUID())
                        .with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID))
                .andExpect(status().isUnauthorized());
    }
}
