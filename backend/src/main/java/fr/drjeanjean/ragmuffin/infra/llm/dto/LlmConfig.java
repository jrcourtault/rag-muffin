package fr.drjeanjean.ragmuffin.infra.llm.dto;

public record LlmConfig(String baseUrl, String apiKey, String model) {
}