package fr.drjeanjean.ragmuffin.infra.tika;

import fr.drjeanjean.ragmuffin.infra.tika.properties.TikaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;

@Slf4j
@Service
@Profile("!test")
public class TikaServiceImpl implements TikaService {

    private final RestClient client;

    public TikaServiceImpl(TikaProperties properties) {
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public String extract(Path filePath) {
        log.debug("Sending {} to Tika Server for text extraction", filePath.getFileName());

        var text = client.put()
                .uri("/tika")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .accept(MediaType.TEXT_PLAIN)
                .header("X-Tika-OCRLanguage", "fra+eng")
                .body(new FileSystemResource(filePath))
                .retrieve()
                .body(String.class);

        return text == null ? "" : text.strip();
    }
}
