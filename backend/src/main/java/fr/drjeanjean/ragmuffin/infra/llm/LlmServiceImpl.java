package fr.drjeanjean.ragmuffin.infra.llm;

import fr.drjeanjean.ragmuffin.infra.llm.dto.LlmConfig;
import fr.drjeanjean.ragmuffin.infra.llm.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@Profile("!test")
public class LlmServiceImpl implements LlmService {

    @Override
    public String chat(List<Message> messages, LlmConfig config) {
        var restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .defaultHeader("Authorization", "Bearer " + config.apiKey())
                .build();

        var request = new ChatCompletionRequest(config.model(),
                messages.stream()
                        .map(m -> new ChatCompletionRequest.Message(m.role(), m.content()))
                        .toList()
        );

        log.debug("LLM request: model={}, messages={}", config.model(), messages.size());

        try {
            var response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from LLM service.");
            }
            var answer = response.choices().getFirst().message().content();
            log.debug("LLM response length={}", answer.length());
            return answer;
        } catch (RestClientResponseException e) {
            var body = e.getResponseBodyAsString();
            if (body.contains("exceed_context_size_error")) {
                log.warn("LLM context size exceeded: {}", body);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Context exceeds LLM capacity. Reduce the number of documents or chunk size.");
            }
            log.error("LLM error {}: {}", e.getStatusCode(), body);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LLM service error.");
        }
    }

    record ChatCompletionRequest(String model, List<Message> messages) {
        record Message(String role, String content) {}
    }

    record ChatCompletionResponse(List<Choice> choices) {
        record Choice(Message message) {}
        record Message(String role, String content) {}
    }
}