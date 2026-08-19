package fr.drjeanjean.ragmuffin.workspace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID>, JpaSpecificationExecutor<Workspace> {

    @EntityGraph(attributePaths = {"vertical"})
    Page<Workspace> findAll(Specification<Workspace> spec, Pageable pageable);

    @Query("SELECT u.workspace FROM User u WHERE u.idpId = :idpId AND u.workspace.active = true")
    List<Workspace> findActiveByUserIdpId(UUID idpId);

    boolean existsByVerticalId(UUID verticalId);

}
