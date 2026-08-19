package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.user.Langue;
import fr.drjeanjean.ragmuffin.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotNull Langue langue,
        @NotNull UserRole role,
        @NotBlank String firstName,
        @NotBlank String lastName) {
}
