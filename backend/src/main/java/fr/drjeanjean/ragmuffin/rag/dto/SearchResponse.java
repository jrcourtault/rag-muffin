package fr.drjeanjean.ragmuffin.rag.dto;

import java.util.List;

public record SearchResponse(
        String rewrittenQuestion,
        List<ChunkResult> chunks) {
}
