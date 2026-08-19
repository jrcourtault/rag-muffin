package fr.drjeanjean.ragmuffin.llmconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.default-llm-config")
public record DefaultLlmConfigProperties(String baseUrl, String apiKey, String model) {
}
