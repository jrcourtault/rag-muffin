package fr.drjeanjean.ragmuffin.vertical;

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
class CreateVerticalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldCreateVertical() throws Exception {
        mockMvc.perform(post("/api/verticals")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Juridique",
                                  "queryRewritePrompt": "Reformule la question.",
                                  "systemPrompt": "Tu es un assistant juridique."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Juridique"))
                .andExpect(jsonPath("$.locked").value(false));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReject_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/verticals")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "queryRewritePrompt": "Reformule.",
                                  "systemPrompt": "Prompt."
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/verticals")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Juridique",
                                  "queryRewritePrompt": "Reformule.",
                                  "systemPrompt": "Prompt."
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}