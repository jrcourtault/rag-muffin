package fr.drjeanjean.ragmuffin.workspace.specification;

import fr.drjeanjean.ragmuffin.workspace.Workspace;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class WorkspaceSpecification {

    private WorkspaceSpecification() {
    }

    private static Specification<Workspace> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Workspace> hasVertical(UUID verticalId) {
        return (root, query, cb) ->
                cb.equal(root.get("vertical").get("id"), verticalId);
    }

    private static Specification<Workspace> isActive(Boolean active) {
        return (root, query, cb) ->
                cb.equal(root.get("active"), active);
    }

    public static Specification<Workspace> withFilters(String name, UUID verticalId, Boolean active) {
        Specification<Workspace> spec = Specification.unrestricted();
        if (name != null) spec = spec.and(hasName(name));
        if (verticalId != null) spec = spec.and(hasVertical(verticalId));
        if (active != null) spec = spec.and(isActive(active));
        return spec;
    }
}