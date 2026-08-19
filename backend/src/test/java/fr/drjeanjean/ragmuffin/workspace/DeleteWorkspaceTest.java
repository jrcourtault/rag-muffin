package fr.drjeanjean.ragmuffin.workspace;

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
class DeleteWorkspaceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldDeleteInactiveWorkspace() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{id}", "c3d4e5f6-a7b8-9012-cdef-123456789012")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workspaces/{id}", "c3d4e5f6-a7b8-9012-cdef-123456789012")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReturn409_whenWorkspaceIsActive() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isConflict());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/workspaces/{id}", UUID.randomUUID())
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isNotFound());
    }
}
