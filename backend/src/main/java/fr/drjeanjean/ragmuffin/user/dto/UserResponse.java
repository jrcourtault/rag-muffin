package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.user.UserRole;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID idpId,
        UUID workspaceId,
        UserRole role,
        String email,
        String firstName,
        String lastName) {
}
