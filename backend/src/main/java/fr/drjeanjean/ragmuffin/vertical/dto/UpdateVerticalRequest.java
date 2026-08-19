package fr.drjeanjean.ragmuffin.vertical.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateVerticalRequest(
        @NotBlank String name,
        @NotBlank String queryRewritePrompt,
        @NotBlank String systemPrompt) {
}
