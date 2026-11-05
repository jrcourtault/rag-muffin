package fr.drjeanjean.ragmuffin.infra.tika;

import fr.drjeanjean.ragmuffin.infra.tika.properties.TikaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Service
@Profile("!test")
public class TikaServiceImpl implements TikaService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient client;

    public TikaServiceImpl(TikaProperties properties) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
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
