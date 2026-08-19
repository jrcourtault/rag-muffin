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

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class ListWorkspacesTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GENERIC_VERTICAL_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReturnAllWorkspaces() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].workspace.id").exists())
                .andExpect(jsonPath("$.content[0].workspace.name").exists())
                .andExpect(jsonPath("$.content[0].workspace.verticalId").exists())
                .andExpect(jsonPath("$.content[0].workspace.active").exists())
                .andExpect(jsonPath("$.content[0].vertical.id").exists())
                .andExpect(jsonPath("$.content[0].vertical.name").exists())
                .andExpect(jsonPath("$.content[0].workspace.createdAt").doesNotExist())
                .andExpect(jsonPath("$.content[0].workspace.modifiedAt").doesNotExist())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldFilterByName() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .param("name", "cabinet")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldFilterByActive() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .param("active", "false")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].workspace.name").value("Ancien Cabinet"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldFilterByVertical() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .param("verticalId", GENERIC_VERTICAL_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldCombineFilters() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .param("name", "cabinet")
                        .param("active", "true")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].workspace.name").value("Cabinet Martin"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .with(jwt("c3d4e5f6-a7b8-9012-cdef-345678901234")))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturnEmptyList_whenNoWorkspaces() throws Exception {
        mockMvc.perform(get("/api/workspaces")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }
}
