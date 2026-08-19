package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.user.Langue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOwnerRequest(
        @NotBlank @Email String email,
        @NotNull Langue langue,
        @NotBlank String firstName,
        @NotBlank String lastName) {
}
