package fr.drjeanjean.ragmuffin.infra.llm;

import fr.drjeanjean.ragmuffin.infra.llm.dto.LlmConfig;
import fr.drjeanjean.ragmuffin.infra.llm.dto.Message;

import java.util.List;

public interface LlmService {

    String chat(List<Message> messages, LlmConfig config);
}