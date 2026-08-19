package fr.drjeanjean.ragmuffin.infra.security;

import fr.drjeanjean.ragmuffin.infra.idp.IdpRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    public UUID getIdpId() {
        String sub = getJwt().getSubject();
        if (sub == null || sub.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing sub claim in JWT");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sub claim in JWT");
        }
    }

    public List<IdpRole> getIdpRoles() {
        return getRawRoles().stream()
                .map(IdpRole::valueOf)
                .toList();
    }

    public boolean hasIdpRole(IdpRole role) {
        return getRawRoles().contains(role.name());
    }

    @SuppressWarnings("unchecked")
    private List<String> getRawRoles() {
        var realmAccess = getJwt().getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        var roles = (List<String>) realmAccess.get("roles");
        return roles != null ? roles : List.of();
    }

    protected Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No JWT authentication found");
        }
        return jwtAuth.getToken();
    }

    protected String getClaim(String claimName) {
        String value = getJwt().getClaimAsString(claimName);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + claimName + " claim in JWT");
        }
        return value;
    }
}
