package fr.drjeanjean.ragmuffin.infra.security;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

public final class JwtTestHelper {

    private JwtTestHelper() {
    }

    public static RequestPostProcessor jwt(String sub, String... keycloakRoles) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(
                jwt -> jwt
                        .claim("sub", sub)
                        .claim("realm_access", Map.of("roles", List.of(keycloakRoles))));
    }

    public static RequestPostProcessor anonymous() {
        return SecurityMockMvcRequestPostProcessors.anonymous();
    }
}
