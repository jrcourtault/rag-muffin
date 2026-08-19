package fr.drjeanjean.ragmuffin.rag.dto;

import java.util.List;

public record AskResponse(
        String answer,
        String rewrittenQuestion,
        List<ChunkResult> chunks) {
}
