package fr.drjeanjean.ragmuffin.user;

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
class CreateUserTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "nouveau@example.com", "langue": "fr", "role": "EDITOR", "firstName": "Jean", "lastName": "Dupont"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idpId").exists())
                .andExpect(jsonPath("$.workspaceId").value(WORKSPACE_ID))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.email").value("nouveau@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReject_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "pas-un-email", "langue": "fr", "role": "EDITOR", "firstName": "Jean", "lastName": "Dupont"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReject_whenEmailIsMissing() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"langue": "fr", "role": "EDITOR", "firstName": "Jean", "lastName": "Dupont"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReject_whenRoleIsMissing() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@example.com", "langue": "fr", "firstName": "Jean", "lastName": "Dupont"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReject_whenFirstNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@example.com", "langue": "fr", "role": "EDITOR", "firstName": "", "lastName": "Dupont"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReject_whenLastNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@example.com", "langue": "fr", "role": "EDITOR", "firstName": "Jean", "lastName": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/workspaces/{workspaceId}/users", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@example.com", "langue": "fr", "role": "EDITOR", "firstName": "Jean", "lastName": "Dupont"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
