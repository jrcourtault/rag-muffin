package fr.drjeanjean.ragmuffin.workspace;

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
class UpdateWorkspaceTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String GENERIC_VERTICAL_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUpdateWorkspace() throws Exception {
        mockMvc.perform(put("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateWorkspaceRequest": {
                                    "name": "Cabinet Martin Renommé",
                                    "verticalId": "%s",
                                    "active": false,
                                    "topK": 10,
                                    "rerank": false,
                                    "prefetchSize": 20
                                  },
                                  "updateOwnerRequest": {
                                    "email": "marie@example.com",
                                    "langue": "fr",
                                    "firstName": "Marie",
                                    "lastName": "Martin"
                                  }
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
                .andExpect(jsonPath("$.name").value("Cabinet Martin Renommé"))
                .andExpect(jsonPath("$.verticalId").value(GENERIC_VERTICAL_ID))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql"})
    void shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(put("/api/workspaces/{id}", UUID.randomUUID())
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateWorkspaceRequest": {
                                    "name": "Inconnu",
                                    "verticalId": "%s",
                                    "active": true,
                                    "topK": 5,
                                    "rerank": false,
                                    "prefetchSize": 20
                                  },
                                  "updateOwnerRequest": {
                                    "email": "test@example.com",
                                    "langue": "fr",
                                    "firstName": "Test",
                                    "lastName": "User"
                                  }
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReject_whenNameIsBlank() throws Exception {
        mockMvc.perform(put("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateWorkspaceRequest": {
                                    "name": "",
                                    "verticalId": "%s",
                                    "active": true
                                  },
                                  "updateOwnerRequest": {
                                    "email": "jean.martin@example.com",
                                    "langue": "fr",
                                    "firstName": "Jean",
                                    "lastName": "Martin"
                                  }
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReject_whenVerticalIdIsNull() throws Exception {
        mockMvc.perform(put("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateWorkspaceRequest": {
                                    "name": "Cabinet Martin",
                                    "active": true
                                  },
                                  "updateOwnerRequest": {
                                    "email": "jean.martin@example.com",
                                    "langue": "fr",
                                    "firstName": "Jean",
                                    "lastName": "Martin"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql"})
    void shouldReject_whenActiveIsNull() throws Exception {
        mockMvc.perform(put("/api/workspaces/{id}", "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                        .with(jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "updateWorkspaceRequest": {
                                    "name": "Cabinet Martin",
                                    "verticalId": "%s"
                                  },
                                  "updateOwnerRequest": {
                                    "email": "jean.martin@example.com",
                                    "langue": "fr",
                                    "firstName": "Jean",
                                    "lastName": "Martin"
                                  }
                                }
                                """.formatted(GENERIC_VERTICAL_ID)))
                .andExpect(status().isBadRequest());
    }
}
