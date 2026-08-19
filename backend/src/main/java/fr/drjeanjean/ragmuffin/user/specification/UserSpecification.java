package fr.drjeanjean.ragmuffin.user.specification;

import fr.drjeanjean.ragmuffin.user.User;
import fr.drjeanjean.ragmuffin.user.UserRole;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.UUID;

public final class UserSpecification {

    private UserSpecification() {
    }

    private static Specification<User> belongsToWorkspace(UUID workspaceId) {
        return (root, query, cb) ->
                cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    private static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    private static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) ->
                cb.equal(root.get("role"), role);
    }

    private static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            var pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    public static Specification<User> withFilters(UUID workspaceId, String email, UserRole role, String name) {
        Objects.requireNonNull(workspaceId, "workspaceId must not be null");
        Specification<User> spec = Specification.where(belongsToWorkspace(workspaceId));
        if (email != null) spec = spec.and(hasEmail(email));
        if (role != null) spec = spec.and(hasRole(role));
        if (name != null) spec = spec.and(hasName(name));
        return spec;
    }
}
