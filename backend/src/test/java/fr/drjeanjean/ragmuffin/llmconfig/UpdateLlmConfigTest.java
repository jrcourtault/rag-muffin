package fr.drjeanjean.ragmuffin.llmconfig;

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
class UpdateLlmConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // a1b2... est OWNER du workspace a1b2... (Cabinet Martin)
    private static final String OWNER_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    // c3d4... est VIEWER du workspace a1b2... (Cabinet Martin)
    private static final String VIEWER_ID = "c3d4e5f6-a7b8-9012-cdef-345678901234";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUpdateLlmConfig_whenOwner() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseUrl": "https://api.mistral.ai/v1",
                                  "apiKey": "sk-new-key",
                                  "model": "mistral-small-latest"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseUrl").value("https://api.mistral.ai/v1"))
                .andExpect(jsonPath("$.apiKeyConfigured").value(true))
                .andExpect(jsonPath("$.model").value("mistral-small-latest"))
                .andExpect(jsonPath("$.apiKey").doesNotExist());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUpdateLlmConfig_withoutApiKey() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseUrl": "http://localhost:12434/engines/v1",
                                  "model": "llama3.2:16k"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseUrl").value("http://localhost:12434/engines/v1"))
                .andExpect(jsonPath("$.model").value("llama3.2:16k"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenViewer() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(VIEWER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseUrl": "https://api.mistral.ai/v1",
                                  "model": "mistral-small-latest"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenBaseUrlIsBlank() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "model": "mistral-small-latest"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenModelIsBlank() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", WORKSPACE_ID)
                        .with(jwt(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseUrl": "https://api.mistral.ai/v1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenWorkspaceNotFound() throws Exception {
        mockMvc.perform(put("/api/workspaces/{workspaceId}/llm-config", UUID.randomUUID())
                        .with(jwt(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "baseUrl": "https://api.mistral.ai/v1",
                                  "model": "mistral-small-latest"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
