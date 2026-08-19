package fr.drjeanjean.ragmuffin.document.service.tool;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

public interface FileStorageService {

    void store(MultipartFile file, UUID workspaceId, UUID documentId, String extension);

    Path resolveWorkspaceDir(UUID workspaceId);

    default Path resolvePath(UUID workspaceId, UUID documentId, String extension) {
        return resolveWorkspaceDir(workspaceId).resolve(documentId + "." + extension);
    }

    void delete(UUID workspaceId, UUID documentId, String extension);

    void deleteWorkspaceDirectory(UUID workspaceId);
}
