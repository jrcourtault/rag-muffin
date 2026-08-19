package fr.drjeanjean.ragmuffin.infra.qdrant.dto;

import java.util.UUID;

public record Chunk(
        String text,
        UUID workspaceId,
        UUID documentId,
        String fileName,
        int chunkIndex) {
}
