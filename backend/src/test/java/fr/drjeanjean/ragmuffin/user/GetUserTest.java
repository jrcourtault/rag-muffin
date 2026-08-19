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
class GetUserTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String OWNER_USER_ID = "d4e5f6a7-b8c9-0123-defa-456789012345";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldGetUser() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users/{id}", WORKSPACE_ID, OWNER_USER_ID)
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idpId").value(SUBJECT))
                .andExpect(jsonPath("$.workspaceId").value(WORKSPACE_ID))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.email").value("jean.martin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Martin"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users/{id}", WORKSPACE_ID, "00000000-0000-0000-0000-000000000000")
                        .with(jwt(SUBJECT)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/workspaces/{workspaceId}/users/{id}", WORKSPACE_ID, OWNER_USER_ID))
                .andExpect(status().isUnauthorized());
    }
}
