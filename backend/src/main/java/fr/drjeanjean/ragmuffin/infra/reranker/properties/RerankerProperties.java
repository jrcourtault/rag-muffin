package fr.drjeanjean.ragmuffin.infra.reranker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.reranker")
public record RerankerProperties(String baseUrl, Duration timeout) {
}
