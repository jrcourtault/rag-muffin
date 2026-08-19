package fr.drjeanjean.ragmuffin.vertical.dto;

import java.util.UUID;

public record VerticalResponse(
        UUID id,
        String name,
        String queryRewritePrompt,
        String systemPrompt,
        boolean locked) {
}
