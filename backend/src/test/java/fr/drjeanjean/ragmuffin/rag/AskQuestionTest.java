package fr.drjeanjean.ragmuffin.rag;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class AskQuestionTest {

    private static final String URL = "/api/workspaces/{workspaceId}/rag/ask";
    private static final String WORKSPACE_A = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String OWNER = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String VIEWER = "c3d4e5f6-a7b8-9012-cdef-345678901234";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldAnswer_whenOwner() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Quelles sont les obligations du bailleur ?", "queryRewriting": false}
                                """)
                        .with(jwt(OWNER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Mock response"))
                .andExpect(jsonPath("$.chunks").isArray());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldAnswer_whenViewer() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Quelles sont les obligations du bailleur ?", "queryRewriting": true}
                                """)
                        .with(jwt(VIEWER)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn403_whenUserIsNotMemberOfWorkspace() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Quelles sont les obligations du bailleur ?", "queryRewriting": false}
                                """)
                        .with(jwt(UUID.randomUUID().toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenQuestionIsBlank() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "   ", "queryRewriting": false}
                                """)
                        .with(jwt(OWNER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenQuestionIsTooLong() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "%s", "queryRewriting": false}
                                """.formatted("a".repeat(2001)))
                        .with(jwt(OWNER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenQueryRewritingIsMissing() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Quelles sont les obligations du bailleur ?"}
                                """)
                        .with(jwt(OWNER)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(post(URL, WORKSPACE_A)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question": "Quelles sont les obligations du bailleur ?", "queryRewriting": false}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
