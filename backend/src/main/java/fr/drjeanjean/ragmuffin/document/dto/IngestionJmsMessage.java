package fr.drjeanjean.ragmuffin.document.dto;

import java.util.UUID;

public record IngestionJmsMessage(UUID documentId, UUID workspaceId, String fileName, String extension) {
}
