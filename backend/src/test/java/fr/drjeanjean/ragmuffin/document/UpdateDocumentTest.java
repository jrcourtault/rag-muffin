package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class UpdateDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_A = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String INDEXED_DOC_ID = "c3d4e5f6-a7b8-9012-cdef-345678901234";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldUpdateDocumentName() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Nouveau nom du document"}
                                """)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(INDEXED_DOC_ID))
                .andExpect(jsonPath("$.name").value("Nouveau nom du document"))
                .andExpect(jsonPath("$.fileName").value("Nouveau nom du document.pdf"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": ""}
                                """)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql", "/sql/documents.sql"})
    void shouldReturn400_whenNameIsMissing() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn404_whenDocumentNotFound() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test"}
                                """)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/documents/{id}", WORKSPACE_A, INDEXED_DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
