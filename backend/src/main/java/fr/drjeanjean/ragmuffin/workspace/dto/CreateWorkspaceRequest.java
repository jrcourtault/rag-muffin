package fr.drjeanjean.ragmuffin.workspace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWorkspaceRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull UUID verticalId,
        @NotNull Boolean active,
        @NotNull @Min(1) Integer chunkSize,
        @NotNull @Min(0) Integer chunkOverlap,
        @NotNull @Min(1) Integer prefetchSize,
        @NotNull Boolean rerank,
        @NotNull @Min(1) Integer topK) {
}
