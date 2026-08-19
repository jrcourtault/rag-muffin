package fr.drjeanjean.ragmuffin.infra.idp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(String baseUrl, String realm, String adminClientId, String adminUsername,
                                 String adminPassword, String appClientId, String appRedirectUri) {
}
