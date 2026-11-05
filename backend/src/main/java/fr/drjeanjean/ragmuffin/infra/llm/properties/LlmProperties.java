package fr.drjeanjean.ragmuffin.infra.llm.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.llm")
public record LlmProperties(Duration timeout) {
}
