package fr.drjeanjean.ragmuffin.document.service.tool;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

@Service
@Profile("test")
public class FileStorageServiceMock implements FileStorageService {

    @Override
    public void store(MultipartFile file, UUID workspaceId, UUID documentId, String extension) {
        // No-op : pas de filesystem en test
    }

    @Override
    public Path resolveWorkspaceDir(UUID workspaceId) {
        return Path.of("/tmp/test-uploads", workspaceId.toString());
    }

    @Override
    public void delete(UUID workspaceId, UUID documentId, String extension) {
        // No-op : pas de filesystem en test
    }

    @Override
    public void deleteWorkspaceDirectory(UUID workspaceId) {
        // No-op : pas de filesystem en test
    }
}
