package fr.drjeanjean.ragmuffin.workspace;

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

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class CreateWorkspaceTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GENERIC_VERTICAL_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldCreateWorkspace() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "createWorkspaceRequest": {
                                    "name": "Club Para Bordeaux",
                                    "verticalId": "%s",
                                    "active": true,
                                    "chunkSize": 512,
                                    "chunkOverlap": 77,
                                    "topK": 5,
                                    "rerank": false,
                                    "prefetchSize": 20
                                  },
                                  "createOwnerRequest": {
                                    "email": "jean@example.com",
                                    "langue": "fr",
                                    "firstName": "Jean",
                                    "lastName": "Dupont"
                                  }
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Club Para Bordeaux"))
                .andExpect(jsonPath("$.verticalId").value(GENERIC_VERTICAL_ID))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").doesNotExist())
                .andExpect(jsonPath("$.modifiedAt").doesNotExist());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReject_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "createWorkspaceRequest": {"name": "", "verticalId": "%s"},
                                  "createOwnerRequest": {"email": "a@b.com", "langue": "fr", "firstName": "A", "lastName": "B"}
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReject_whenVerticalIdIsNull() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "createWorkspaceRequest": {"name": "Cabinet Dupont"},
                                  "createOwnerRequest": {"email": "a@b.com", "langue": "fr", "firstName": "A", "lastName": "B"}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReject_whenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Club Para Bordeaux", "verticalId": "%s", "active": true}
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isUnauthorized());
    }
}
