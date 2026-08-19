package fr.drjeanjean.ragmuffin.infra.idp;


import fr.drjeanjean.ragmuffin.infra.idp.dto.IdpUser;

import java.util.UUID;

public interface IdpService {

    IdpUser getOrCreateUser(String email, String langue);

    void updateUserLocale(UUID idpId, String langue);
}
