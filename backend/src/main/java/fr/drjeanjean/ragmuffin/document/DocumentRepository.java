package fr.drjeanjean.ragmuffin.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {

    @Modifying
    @Query("DELETE FROM Document d WHERE d.workspace.id = :workspaceId")
    void deleteByWorkspaceId(UUID workspaceId);

    List<Document> findByStatusAndModifiedAtBefore(DocumentStatus status, OffsetDateTime cutoff);

    @Modifying
    @Query("""
            UPDATE Document d
            SET d.status = fr.drjeanjean.ragmuffin.document.DocumentStatus.ERROR, d.modifiedAt = :now
            WHERE d.status = fr.drjeanjean.ragmuffin.document.DocumentStatus.PENDING AND d.createdAt < :cutoff""")
    int markPendingAsErrorCreatedBefore(OffsetDateTime cutoff, OffsetDateTime now);
}
