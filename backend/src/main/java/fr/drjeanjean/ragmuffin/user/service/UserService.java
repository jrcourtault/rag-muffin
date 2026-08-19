package fr.drjeanjean.ragmuffin.user.service;

import fr.drjeanjean.ragmuffin.infra.idp.IdpService;
import fr.drjeanjean.ragmuffin.user.User;
import fr.drjeanjean.ragmuffin.user.UserRepository;
import fr.drjeanjean.ragmuffin.user.UserRole;
import fr.drjeanjean.ragmuffin.user.dto.*;
import fr.drjeanjean.ragmuffin.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final IdpService idpService;

    public User create(UUID workspaceId, CreateUserRequest request) {
        if (request.role() == UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create a user with role OWNER");
        }
        var idpUser = idpService.getOrCreateUser(normalizeEmail(request.email()), request.langue().name());
        var workspace = workspaceRepository.getReferenceById(workspaceId);
        var user = UserMapper.INSTANCE.toEntity(request, idpUser, workspace);
        return userRepository.save(user);
    }

    public void createOwner(UUID workspaceId, CreateOwnerRequest request) {
        var idpUser = idpService.getOrCreateUser(normalizeEmail(request.email()), request.langue().name());
        var workspace = workspaceRepository.getReferenceById(workspaceId);
        var user = UserMapper.INSTANCE.toEntity(request, idpUser, workspace);
        userRepository.save(user);
    }

    public void update(User user, UpdateUserRequest request) {
        user.update(request.role(), request.firstName(), request.lastName(), request.langue());
        idpService.updateUserLocale(user.getIdpId(), request.langue().name());
    }

    public void updateOwner(UUID workspaceId, UpdateOwnerRequest request) {
        var owner = findOwnerByWorkspaceId(workspaceId);
        var idpOwner = idpService.getOrCreateUser(normalizeEmail(request.email()), request.langue().name());

        if (!owner.getIdpId().equals(idpOwner.id())) {
            // On change de owner (car l'email a été modifié)
            // 1) L'ancien owner devient VIEWER
            owner.downgradeRole();
            userRepository.flush(); // flush en bdd. Sans cela : l'ancien owner est toujours owner, et createOwner en étape 2 lève une exception à cause de la contrainte en bdd (1 seul owner par workspace)
            // 2) On cherche si le nouveau owner est un user existant
            var newOwnerOpt = userRepository.findByEmailAndWorkspaceId(normalizeEmail(request.email()), workspaceId);
            if (newOwnerOpt.isPresent()) {
                // Oui : on upgrade le role du nouvel owner
                var newOwner = newOwnerOpt.get();
                newOwner.update(UserRole.OWNER, request.firstName(), request.lastName(), request.langue());
                idpService.updateUserLocale(newOwner.getIdpId(), request.langue().name());
            } else {
                // Non : on crée le owner
                createOwner(workspaceId, UserMapper.INSTANCE.toCreateRequest(request));
            }
        } else {
            // Update des infos du owner courant
            owner.update(request.firstName(), request.lastName(), request.langue());
            idpService.updateUserLocale(owner.getIdpId(), request.langue().name());
        }
    }

    public Page<User> getPage(Specification<User> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    public User findOwnerByWorkspaceId(UUID workspaceId) {
        return userRepository.findByWorkspaceIdAndRole(workspaceId, UserRole.OWNER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
    }

    public User findByIdpIdAndWorkspaceId(UUID idpId, UUID workspaceId) {
        return userRepository.findByIdpIdAndWorkspaceIdAndWorkspaceActiveTrue(idpId, workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void delete(User user) {
        if (user.getRole() == UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a user with role OWNER");
        }
        userRepository.delete(user);
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase().strip();
    }
}
