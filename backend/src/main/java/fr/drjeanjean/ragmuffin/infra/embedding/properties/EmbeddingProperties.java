package fr.drjeanjean.ragmuffin.infra.embedding.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.embedding")
public record EmbeddingProperties(String baseUrl, Duration timeout) {
}
