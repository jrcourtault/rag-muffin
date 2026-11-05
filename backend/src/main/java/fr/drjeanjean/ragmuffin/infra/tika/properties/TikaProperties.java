package fr.drjeanjean.ragmuffin.infra.tika.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.tika")
public record TikaProperties(String baseUrl, Duration timeout) {
}
