package fr.drjeanjean.ragmuffin.infra.qdrant.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.qdrant")
public record QdrantProperties(String host, int port, String apiKey, String collectionName) {
}
