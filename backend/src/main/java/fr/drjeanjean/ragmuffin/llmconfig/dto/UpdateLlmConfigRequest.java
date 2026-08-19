package fr.drjeanjean.ragmuffin.llmconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLlmConfigRequest(
        @NotBlank @Size(max = 255) String baseUrl,
        @Size(max = 255) String apiKey,
        @NotBlank @Size(max = 255) String model) {
}
