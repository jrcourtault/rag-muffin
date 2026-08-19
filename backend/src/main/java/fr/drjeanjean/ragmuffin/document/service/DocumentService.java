package fr.drjeanjean.ragmuffin.document.service;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.document.DocumentRepository;
import fr.drjeanjean.ragmuffin.document.dto.UploadDocumentRequest;
import fr.drjeanjean.ragmuffin.document.service.tool.DocumentIngestionJmsPublisher;
import fr.drjeanjean.ragmuffin.document.service.tool.FileStorageService;
import fr.drjeanjean.ragmuffin.infra.qdrant.QdrantService;
import fr.drjeanjean.ragmuffin.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final WorkspaceRepository workspaceRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final DocumentIngestionJmsPublisher documentIngestionJmsPublisher;
    private final QdrantService qdrantService;

    @Value("${app.upload.allowed-extensions}")
    private String[] allowedExtensions;

    public Document upload(MultipartFile file, UploadDocumentRequest request, UUID workspaceId) {
        log.info("Uploading file {} in workspace {}", file.getOriginalFilename(), workspaceId);

        var workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        var filename = file.getOriginalFilename();
        var extension = extractAndValidateExtension(filename, Set.of(allowedExtensions));

        var document = Document.builder()
                .workspace(workspace)
                .name(request.name())
                .extension(extension)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();
        document = documentRepository.save(document);

        try {
            fileStorageService.store(file, workspaceId, document.getId(), extension);
            documentIngestionJmsPublisher.requestIngestion(document);
        } catch (Exception e) {
            log.error("Failed to store or request an ingestion for document {}", document.getId(), e);
            document.markError();
            tryDeleteFile(workspaceId, document.getId(), extension);
        }

        return document;
    }

    public Page<Document> getPage(Specification<Document> spec, Pageable pageable) {
        return documentRepository.findAll(spec, pageable);
    }

    public Document findById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void deleteAllByWorkspaceId(UUID workspaceId) {
        // On supprime tous les documents, meme ceux en PENDING !
        qdrantService.deleteByWorkspaceId(workspaceId);
        fileStorageService.deleteWorkspaceDirectory(workspaceId);
        documentRepository.deleteByWorkspaceId(workspaceId);
    }

    public void delete(Document document) {
        if (!document.isDeletable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This document is not deletable...");
        }
        removeDocument(document);
    }

    public Resource loadFileAsResource(Document document) {
        var path = fileStorageService.resolvePath(
                document.getWorkspace().getId(), document.getId(), document.getExtension());
        try {
            var resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    private void removeDocument(Document document) {
        var documentId = document.getId();
        var workspaceId = document.getWorkspace().getId();
        qdrantService.deleteByDocumentId(documentId);
        fileStorageService.delete(workspaceId, documentId, document.getExtension());
        documentRepository.delete(document);
        log.info("Deleted document {} of workspace {} ({})", documentId, workspaceId, document.getFileName());
    }

    private void tryDeleteFile(UUID workspaceId, UUID documentId, String extension) {
        try {
            fileStorageService.delete(workspaceId, documentId, extension);
        } catch (Exception ex) {
            log.error("Could not clean up file for document {} after upload failure", documentId, ex);
        }
    }

    private String extractAndValidateExtension(String filename, Set<String> allowedExtensions) {
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filename is required");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File has no extension");
        }
        var extension = filename.substring(dotIndex + 1).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File extension '." + extension + "' is not allowed");
        }
        return extension;
    }
}
