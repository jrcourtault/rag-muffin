package fr.drjeanjean.ragmuffin.llmconfig.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param allowedPattern regex que doit respecter le baseUrl saisi par un OWNER (protection SSRF).
 *                       Limite connue : sans résolution DNS, un domaine pointant vers une IP interne passe.
 */
@ConfigurationProperties(prefix = "app.llm-url")
public record LlmUrlProperties(String allowedPattern) {
}
