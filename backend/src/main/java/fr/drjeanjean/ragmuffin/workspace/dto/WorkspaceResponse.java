package fr.drjeanjean.ragmuffin.workspace.dto;

import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        UUID verticalId,
        boolean active,
        int chunkSize,
        int chunkOverlap,
        int prefetchSize,
        boolean rerank,
        int topK) {
}
