package fr.drjeanjean.ragmuffin.llmconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config LLM copiée dans chaque nouveau workspace (voir WorkspaceMapper).
 * <p>
 * Sécurité : {@code api-key} ne doit jamais être renseignée en prod. Sinon, un owner peut récupérer la clef en:
 * - saisissant un basUrl à lui
 * - en interrogeant le RAG, qui appel le LLM
 * - l'appel au LLM va appeler le baseUrl avec "Authorization Bearer+apiKey"
 * - le OWNER récupère la requête et donc l'apiKey'
 */
@ConfigurationProperties("app.default-llm-config")
public record DefaultLlmConfigProperties(String baseUrl, String apiKey, String model) {
}
