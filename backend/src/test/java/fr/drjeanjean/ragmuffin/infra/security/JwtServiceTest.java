package fr.drjeanjean.ragmuffin.infra.security;

import fr.drjeanjean.ragmuffin.infra.idp.IdpRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetIdpId_whenSubIsValidUuid() {
        var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        setJwt(jwt -> jwt.subject(userId.toString()));

        assertEquals(userId, jwtService.getIdpId());
    }

    @Test
    void shouldThrow_whenSubIsAbsent() {
        setJwt(jwt -> {
        });

        assertThrows(ResponseStatusException.class, jwtService::getIdpId);
    }

    @Test
    void shouldThrow_whenSubIsBlank() {
        setJwt(jwt -> jwt.subject("   "));

        assertThrows(ResponseStatusException.class, jwtService::getIdpId);
    }

    @Test
    void shouldThrow_whenSubIsNotValidUuid() {
        setJwt(jwt -> jwt.subject("not-a-uuid"));

        assertThrows(ResponseStatusException.class, jwtService::getIdpId);
    }

    @Test
    void shouldThrow_whenNoAuthentication() {
        assertThrows(ResponseStatusException.class, jwtService::getIdpId);
    }

    @Test
    void shouldGetClaim_whenClaimIsPresent() {
        setJwt(jwt -> jwt.claim("custom_claim", "some-value"));

        assertEquals("some-value", jwtService.getClaim("custom_claim"));
    }

    @Test
    void shouldThrowOnGetClaim_whenClaimIsAbsent() {
        setJwt(jwt -> {
        });

        assertThrows(ResponseStatusException.class, () -> jwtService.getClaim("missing_claim"));
    }

    @Test
    void shouldThrowOnGetClaim_whenClaimIsBlank() {
        setJwt(jwt -> jwt.claim("blank_claim", "   "));

        assertThrows(ResponseStatusException.class, () -> jwtService.getClaim("blank_claim"));
    }

    @Test
    void shouldReturnRoles_whenRealmAccessPresent() {
        setJwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ADMIN"))));

        assertEquals(List.of(IdpRole.ADMIN), jwtService.getIdpRoles());
    }

    @Test
    void shouldThrow_whenUnknownRole() {
        setJwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("ADMIN", "unknown_role"))));

        assertThrows(IllegalArgumentException.class, jwtService::getIdpRoles);
    }

    @Test
    void shouldReturnEmptyList_whenNoRealmAccess() {
        setJwt(jwt -> {
        });

        assertEquals(List.of(), jwtService.getIdpRoles());
    }

    @Test
    void shouldReturnTrue_whenHasIdpRole() {
        setJwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("TRUC", "ADMIN"))));

        assertTrue(jwtService.hasIdpRole(IdpRole.ADMIN));
    }

    @Test
    void shouldReturnFalse_whenDoesNotHaveIdpRole_noRole() {
        setJwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of())));

        assertFalse(jwtService.hasIdpRole(IdpRole.ADMIN));
    }

    @Test
    void shouldReturnFalse_whenDoesNotHaveIdpRole_manyRoles() {
        setJwt(jwt -> jwt.claim("realm_access", Map.of("roles", List.of("BIDULE", "TRUC"))));

        assertFalse(jwtService.hasIdpRole(IdpRole.ADMIN));
    }

    @Test
    void shouldReturnFalse_whenNoRealmAccessForHasIdpRole() {
        setJwt(jwt -> {
        });

        assertFalse(jwtService.hasIdpRole(IdpRole.ADMIN));
    }

    private void setJwt(java.util.function.Consumer<Jwt.Builder> customizer) {
        var builder = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("sub", "test-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        customizer.accept(builder);
        var jwt = builder.build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
