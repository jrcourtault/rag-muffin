package fr.drjeanjean.ragmuffin.user.dto;

import fr.drjeanjean.ragmuffin.user.Langue;

public record OwnerResponse(
        String email,
        Langue langue,
        String firstName,
        String lastName) {
}
