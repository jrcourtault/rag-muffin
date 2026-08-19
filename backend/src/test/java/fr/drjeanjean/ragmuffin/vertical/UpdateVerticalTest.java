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

import java.util.UUID;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class UpdateVerticalTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GENERIC_VERTICAL_ID = "00000000-0000-0000-0000-000000000001";
    private static final String DELETABLE_VERTICAL_ID = "aaaaaaaa-0000-0000-0000-000000000001";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/verticals.sql"})
    void shouldUpdateVertical() throws Exception {
        mockMvc.perform(put("/api/verticals/{id}", DELETABLE_VERTICAL_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Juridique Modifié",
                                  "queryRewritePrompt": "Nouveau prompt.",
                                  "systemPrompt": "Nouveau système."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Juridique Modifié"))
                .andExpect(jsonPath("$.queryRewritePrompt").value("Nouveau prompt."))
                .andExpect(jsonPath("$.systemPrompt").value("Nouveau système."));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldUpdateGenericVertical() throws Exception {
        mockMvc.perform(put("/api/verticals/{id}", GENERIC_VERTICAL_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Generic",
                                  "queryRewritePrompt": "Reformule.",
                                  "systemPrompt": "Assistant."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(true));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(put("/api/verticals/{id}", UUID.randomUUID())
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test",
                                  "queryRewritePrompt": "Reformule.",
                                  "systemPrompt": "Prompt."
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
