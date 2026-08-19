package fr.drjeanjean.ragmuffin.infra.security;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.infra.idp.IdpRole;
import fr.drjeanjean.ragmuffin.user.User;
import fr.drjeanjean.ragmuffin.user.UserRepository;
import fr.drjeanjean.ragmuffin.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public UUID getIdpId() {
        return jwtService.getIdpId();
    }

    public boolean isAdmin() {
        return jwtService.hasIdpRole(IdpRole.ADMIN);
    }

    public boolean workspaceIsEnableAndHasRole(UUID workspaceId, String... allowedRoles) {
        var user = userRepository.findByIdpIdAndWorkspaceIdAndWorkspaceActiveTrue(getIdpId(), workspaceId);
        if (user.isEmpty()) {
            return false;
        }
        var roles = Arrays.stream(allowedRoles).map(UserRole::valueOf).toList();
        return roles.contains(user.get().getRole());
    }

    public void checkIsNotOwner(User user, String errorMessage) {
        if (user.getRole() == UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public void checkBelongsToWorkspace(User user, UUID workspaceId) {
        if (!user.getWorkspace().getId().equals(workspaceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void checkBelongsToWorkspace(Document document, UUID workspaceId) {
        if (!document.getWorkspace().getId().equals(workspaceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}
