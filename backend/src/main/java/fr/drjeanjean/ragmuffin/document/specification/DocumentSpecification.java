package fr.drjeanjean.ragmuffin.document.specification;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.document.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.UUID;

public final class DocumentSpecification {

    private DocumentSpecification() {
    }

    private static Specification<Document> belongsToWorkspace(UUID workspaceId) {
        return (root, query, cb) ->
                cb.equal(root.get("workspace").get("id"), workspaceId);
    }

    private static Specification<Document> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private static Specification<Document> hasExtension(String extension) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("extension")), extension.toLowerCase());
    }

    private static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    private static Specification<Document> hasContentType(String contentType) {
        return (root, query, cb) ->
                cb.equal(root.get("contentType"), contentType);
    }

    public static Specification<Document> withFilters(UUID workspaceId, String name, String extension, DocumentStatus status, String contentType) {
        Objects.requireNonNull(workspaceId, "workspaceId must not be null");
        Specification<Document> spec = Specification.where(belongsToWorkspace(workspaceId));
        if (name != null) spec = spec.and(hasName(name));
        if (extension != null) spec = spec.and(hasExtension(extension));
        if (status != null) spec = spec.and(hasStatus(status));
        if (contentType != null) spec = spec.and(hasContentType(contentType));
        return spec;
    }
}
