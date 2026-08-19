package fr.drjeanjean.ragmuffin.infra.reranker.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reranker")
public record RerankerProperties(String baseUrl) {
}
