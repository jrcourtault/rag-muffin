package fr.drjeanjean.ragmuffin.document.service;

import fr.drjeanjean.ragmuffin.document.DocumentRepository;
import fr.drjeanjean.ragmuffin.document.DocumentStatus;
import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DocumentCleanupJob {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final QdrantService qdrantService;
    private final CleanupProperties cleanupProperties;

    @Scheduled(cron = "${app.cleanup.cron}")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupErrorDocuments() {
        log.info("Starting error document cleanup");
        var cutoff = OffsetDateTime.now().minusDays(cleanupProperties.errorRetentionDays());
        var docs = documentRepository.findByStatusAndModifiedAtBefore(DocumentStatus.ERROR, cutoff);

        if (docs.isEmpty()) return;

        docs.forEach(doc -> qdrantService.deleteByDocumentId(doc.getId()));
        documentRepository.deleteAllInBatch(docs);
        docs.forEach(doc -> fileStorageService.delete(doc.getWorkspace().getId(), doc.getId(), doc.getExtension()));
        log.info("Cleanup : {} document(s) en erreur supprimés (rétention {} j)", docs.size(), cleanupProperties.errorRetentionDays());
    }

    @Scheduled(cron = "${app.cleanup.pending-cron}")
    @Transactional(rollbackFor = Exception.class)
    public void markStalePendingDocumentsAsError() {
        var now = OffsetDateTime.now();
        var cutoff = now.minus(cleanupProperties.pendingTimeout());
        var count = documentRepository.markPendingAsErrorCreatedBefore(cutoff, now);
        if (count > 0) {
            log.warn("{} document(s) stuck in PENDING for more than {} marked as ERROR",
                    count, cleanupProperties.pendingTimeout());
        }
    }
}
