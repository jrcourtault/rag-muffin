package fr.drjeanjean.ragmuffin.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UploadDocumentRequest(
        @NotBlank @Size(max = 255) String name) {
}
