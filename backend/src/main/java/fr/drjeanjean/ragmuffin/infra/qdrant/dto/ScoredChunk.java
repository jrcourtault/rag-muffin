package fr.drjeanjean.ragmuffin.infra.qdrant.dto;

import java.util.UUID;

public record ScoredChunk(
        UUID id,
        String text,
        double score,
        UUID documentId,
        String fileName,
        int chunkIndex,
        UUID workspaceId) {
}
