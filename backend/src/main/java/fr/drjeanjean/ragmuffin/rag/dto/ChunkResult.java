package fr.drjeanjean.ragmuffin.rag.dto;

import java.util.UUID;

public record ChunkResult(
        UUID documentId,
        String fileName,
        int chunkIndex,
        String text,
        double score) {
}
