package fr.drjeanjean.ragmuffin.infra.embedding.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.embedding")
public record EmbeddingProperties(String baseUrl) {
}
