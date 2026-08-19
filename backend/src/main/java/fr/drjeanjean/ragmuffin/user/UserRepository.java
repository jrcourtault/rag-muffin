package fr.drjeanjean.ragmuffin.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByWorkspaceIdAndRole(UUID workspaceId, UserRole role);

    Optional<User> findByIdpIdAndWorkspaceIdAndWorkspaceActiveTrue(UUID idpId, UUID workspaceId);

    Optional<User> findByEmailAndWorkspaceId(String email, UUID workspaceId);
}
