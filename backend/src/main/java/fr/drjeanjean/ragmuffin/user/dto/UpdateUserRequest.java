package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.user.Langue;
import fr.drjeanjean.ragmuffin.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotNull UserRole role,
        @NotNull Langue langue,
        @NotBlank String firstName,
        @NotBlank String lastName) {
}
