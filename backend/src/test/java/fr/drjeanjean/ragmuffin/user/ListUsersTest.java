package fr.drjeanjean.ragmuffin.user;

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
class ListUsersTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldListUsers() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].idpId").exists())
                .andExpect(jsonPath("$.content[0].workspaceId").value(WORKSPACE_ID))
                .andExpect(jsonPath("$.content[0].role").exists())
                .andExpect(jsonPath("$.content[0].email").exists())
                .andExpect(jsonPath("$.content[0].firstName").exists())
                .andExpect(jsonPath("$.content[0].lastName").exists());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldFilterByEmail() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .param("email", "marie")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("marie.dupont@example.com"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldFilterByRole() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .param("role", "OWNER")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("jean.martin@example.com"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldFilterByName_matchingLastName() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .param("name", "dupont")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].lastName").value("Dupont"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldFilterByName_matchingFirstName() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .param("name", "jean")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Jean"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldCombineFilters() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .param("email", "jean")
                        .param("role", "OWNER")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("jean.martin@example.com"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn403_whenWorkspaceNotFound() throws Exception {
        mockMvc.perform(get("/api/workspaces/00000000-0000-0000-0000-000000000000/users")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users", WORKSPACE_ID))
                .andExpect(status().isUnauthorized());
    }
}
