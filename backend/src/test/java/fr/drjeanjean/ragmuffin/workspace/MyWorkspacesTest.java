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
class MyWorkspacesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturnOnlyMyActiveWorkspaces() throws Exception {
        // Marie Dupont (c3d4...) is VIEWER of workspace 1 only
        mockMvc.perform(get("/api/workspaces/mine")
                        .with(jwt("c3d4e5f6-a7b8-9012-cdef-345678901234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cabinet Martin"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturnMultipleWorkspaces_whenUserHasMany() throws Exception {
        // Jean Martin (a1b2...) is OWNER of workspace 1 and EDITOR of workspace 2
        mockMvc.perform(get("/api/workspaces/mine")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReturnEmptyList_whenNoUserAssociation() throws Exception {
        mockMvc.perform(get("/api/workspaces/mine")
                        .with(jwt("99999999-9999-9999-9999-999999999999")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
