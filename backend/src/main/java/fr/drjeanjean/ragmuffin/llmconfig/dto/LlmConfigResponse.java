package fr.drjeanjean.ragmuffin.llmconfig.dto;

public record LlmConfigResponse(String baseUrl, boolean apiKeyConfigured, String model) {
}
