package fr.drjeanjean.ragmuffin.document;

import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static fr.drjeanjean.ragmuffin.infra.security.JwtTestHelper.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class UploadDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WORKSPACE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String SUBJECT = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    private static MockMultipartFile requestPart(String name) {
        return new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE,
                ("""
                        {"name": "%s"}
                        """.formatted(name)).getBytes());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUploadPdf() throws Exception {
        var file = new MockMultipartFile(
                "file", "contrat.pdf", "application/pdf", "fake pdf content".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(file)
                        .file(requestPart("Contrat de bail"))
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Contrat de bail"))
                .andExpect(jsonPath("$.extension").value("pdf"))
                .andExpect(jsonPath("$.fileName").value("Contrat de bail.pdf"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUploadDocx() throws Exception {
        var file = new MockMultipartFile(
                "file", "rapport.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "fake docx content".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(file)
                        .file(requestPart("Rapport annuel"))
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Rapport annuel"))
                .andExpect(jsonPath("$.extension").value("docx"))
                .andExpect(jsonPath("$.fileName").value("Rapport annuel.docx"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldUploadTxt() throws Exception {
        var file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "some text content".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(file)
                        .file(requestPart("Notes de reunion"))
                        .with(jwt(SUBJECT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Notes de reunion"))
                .andExpect(jsonPath("$.extension").value("txt"))
                .andExpect(jsonPath("$.fileName").value("Notes de reunion.txt"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        var file = new MockMultipartFile(
                "file", "contrat.pdf", "application/pdf", "fake pdf content".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenExtensionNotAllowed() throws Exception {
        var file = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", "bad content".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(file)
                        .file(requestPart("Malware"))
                        .with(jwt(SUBJECT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"/sql/clean.sql", "/sql/workspaces.sql", "/sql/users.sql"})
    void shouldReturn400_whenNoFile() throws Exception {
        mockMvc.perform(multipart("/api/workspaces/{workspaceId}/documents", WORKSPACE_ID)
                        .file(requestPart("Test"))
                        .with(jwt(SUBJECT)))
                .andExpect(status().isBadRequest());
    }
}
