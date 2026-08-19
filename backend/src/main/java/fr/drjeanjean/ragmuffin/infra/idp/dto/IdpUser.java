package fr.drjeanjean.ragmuffin.infra.idp.dto;

import java.util.UUID;

public record IdpUser(UUID id, String email) {
}
