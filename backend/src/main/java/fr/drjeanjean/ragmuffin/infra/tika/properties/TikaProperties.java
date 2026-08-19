package fr.drjeanjean.ragmuffin.infra.tika.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tika")
public record TikaProperties(String baseUrl) {
}
