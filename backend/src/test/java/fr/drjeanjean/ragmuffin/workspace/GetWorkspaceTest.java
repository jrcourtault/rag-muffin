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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class GetWorkspaceTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GENERIC_VERTICAL_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturnWorkspace_whenExists() throws Exception {
        mockMvc.perform(get("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspace.id").value("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .andExpect(jsonPath("$.workspace.name").value("Cabinet Martin"))
                .andExpect(jsonPath("$.workspace.verticalId").value(GENERIC_VERTICAL_ID))
                .andExpect(jsonPath("$.workspace.active").value(true))
                .andExpect(jsonPath("$.owner.email").value("jean.martin@example.com"))
                .andExpect(jsonPath("$.owner.firstName").value("Jean"))
                .andExpect(jsonPath("$.owner.lastName").value("Martin"))
                .andExpect(jsonPath("$.workspace.createdAt").doesNotExist())
                .andExpect(jsonPath("$.workspace.modifiedAt").doesNotExist());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/workspaces/{id}", UUID.randomUUID())
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isNotFound());
    }
}
