package fr.drjeanjean.ragmuffin.llmconfig;

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

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.anonymous;
import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class GetLlmConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // a1b2... est OWNER du workspace a1b2... (Cabinet Martin)
    private static final String OWNER_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    // c3d4... est VIEWER du workspace a1b2... (Cabinet Martin)
    private static final String VIEWER_ID = "c3d4e5f6-a7b8-9012-cdef-345678901234";

    // a1b2... est EDITOR du workspace b2c3... (Club Para Bordeaux), pas OWNER
    private static final String EDITOR_WORKSPACE_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturnLlmConfig_whenOwner() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseUrl").value("http://localhost:12434/engines/v1"))
                .andExpect(jsonPath("$.apiKeyConfigured").isBoolean())
                .andExpect(jsonPath("$.model").value("llama3.2:16k"))
                .andExpect(jsonPath("$.apiKey").doesNotExist());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenViewer() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(VIEWER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenEditorOnAnotherWorkspace() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/llm-config", EDITOR_WORKSPACE_ID)
                        .with(jwt(OWNER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenWorkspaceNotFound() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/llm-config", UUID.randomUUID())
                        .with(jwt(OWNER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }
}
