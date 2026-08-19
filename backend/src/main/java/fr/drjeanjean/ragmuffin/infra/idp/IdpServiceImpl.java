package fr.drjeanjean.ragmuffin.infra.idp;

import fr.drjeanjean.ragmuffin.infra.idp.dto.IdpUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Profile("!test")
public class IdpServiceImpl implements IdpService {

    private final RestClient restClient;
    private final KeycloakProperties properties;

    public IdpServiceImpl(KeycloakProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public IdpUser getOrCreateUser(String email, String langue) {
        var token = getAdminToken();
        var existing = findUserByEmail(token, email);
        if (existing != null) {
            return existing;
        }
        return createUser(token, email, langue);
    }

    private String getAdminToken() {
        var response = restClient.post()
                .uri("/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=password&client_id=%s&username=%s&password=%s".formatted(
                        properties.adminClientId(), properties.adminUsername(), properties.adminPassword()))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        return (String) response.get("access_token");
    }

    private IdpUser findUserByEmail(String token, String email) {
        var users = restClient.get()
                .uri("/admin/realms/{realm}/users?email={email}&exact=true", properties.realm(), email)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (users == null || users.isEmpty()) {
            return null;
        }
        var user = users.getFirst();
        return new IdpUser(UUID.fromString((String) user.get("id")), (String) user.get("email"));
    }

    private IdpUser createUser(String token, String email, String langue) {
        var userRepresentation = Map.of(
                "email", email,
                "username", email,
                "enabled", true,
                "requiredActions", List.of("UPDATE_PASSWORD"),
                "attributes", Map.of("locale", List.of(langue))
        );
        var response = restClient.post()
                .uri("/admin/realms/{realm}/users", properties.realm())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRepresentation)
                .retrieve()
                .toBodilessEntity();
        var location = response.getHeaders().getLocation();
        var id = location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
        log.info("Created Keycloak user {} with email {}", id, email);
        sendVerifyEmail(token, id);
        return new IdpUser(UUID.fromString(id), email);
    }

    @Override
    public void updateUserLocale(UUID idpId, String langue) {
        var token = getAdminToken();
        // GET complet du user Keycloak (le PUT exige tous les champs obligatoires)
        var user = restClient.get()
                .uri("/admin/realms/{realm}/users/{userId}", properties.realm(), idpId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        // Modifier l'attribut locale
        var attributes = new java.util.HashMap<>((Map<String, Object>) user.getOrDefault("attributes", Map.of()));
        attributes.put("locale", List.of(langue));
        user.put("attributes", attributes);
        // PUT avec la représentation complète
        restClient.put()
                .uri("/admin/realms/{realm}/users/{userId}", properties.realm(), idpId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .retrieve()
                .toBodilessEntity();
        log.info("Updated locale to {} for Keycloak user {}", langue, idpId);
    }

    private void sendVerifyEmail(String token, String userId) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/{realm}/users/{userId}/send-verify-email")
                        .queryParam("client_id", properties.appClientId())
                        .queryParam("redirect_uri", properties.appRedirectUri())
                        .build(properties.realm(), userId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
        log.info("Sent verification email to Keycloak user {}", userId);
    }
}
