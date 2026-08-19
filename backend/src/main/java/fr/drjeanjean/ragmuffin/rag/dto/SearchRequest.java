package fr.drjeanjean.ragmuffin.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SearchRequest(
        @NotBlank @Size(max = 2000) String question,
        @NotNull Boolean queryRewriting) {
}
