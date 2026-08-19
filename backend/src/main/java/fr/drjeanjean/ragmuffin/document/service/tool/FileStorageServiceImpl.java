package fr.drjeanjean.ragmuffin.document.service.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@Profile("!test")
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @Override
    public void store(MultipartFile file, UUID workspaceId, UUID documentId, String extension) {
        try {
            var filePath = resolvePath(workspaceId, documentId, extension);
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save uploaded file", e);
        }
    }

    @Override
    public Path resolveWorkspaceDir(UUID workspaceId) {
        return Path.of(uploadDirectory, workspaceId.toString());
    }

    @Override
    public void delete(UUID workspaceId, UUID documentId, String extension) {
        try {
            Files.deleteIfExists(resolvePath(workspaceId, documentId, extension));
            var workspaceDir = resolveWorkspaceDir(workspaceId);
            if (isDirectoryEmpty(workspaceDir)) {
                Files.delete(workspaceDir);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file for document " + documentId, e);
        }
    }

    @Override
    public void deleteWorkspaceDirectory(UUID workspaceId) {
        try {
            FileSystemUtils.deleteRecursively(resolveWorkspaceDir(workspaceId));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete workspace directory for " + workspaceId, e);
        }
    }

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findFirst().isEmpty();
        }
    }
}
