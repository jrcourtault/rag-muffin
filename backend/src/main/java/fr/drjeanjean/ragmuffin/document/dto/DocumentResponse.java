package fr.drjeanjean.ragmuffin.document.dto;

import fr.drjeanjean.ragmuffin.document.DocumentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String name,
        String extension,
        String fileName,
        String contentType,
        long sizeBytes,
        DocumentStatus status,
        Integer chunkCount,
        OffsetDateTime createdAt
) {
}
