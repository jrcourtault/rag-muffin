package fr.drjeanjean.ragmuffin.infra.idp;

import java.util.UUID;

import fr.drjeanjean.ragmuffin.infra.idp.dto.IdpUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class IdpServiceMock implements IdpService {

    @Override
    public IdpUser getOrCreateUser(String email, String langue) {
        return new IdpUser(UUID.randomUUID(), email);
    }

    @Override
    public void updateUserLocale(UUID idpId, String langue) {
        // no-op in tests
    }
}
