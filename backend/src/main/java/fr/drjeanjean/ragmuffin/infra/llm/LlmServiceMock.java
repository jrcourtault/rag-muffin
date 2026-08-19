package fr.drjeanjean.ragmuffin.infra.llm;

import fr.drjeanjean.ragmuffin.infra.llm.dto.LlmConfig;
import fr.drjeanjean.ragmuffin.infra.llm.dto.Message;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class LlmServiceMock implements LlmService {

    @Override
    public String chat(List<Message> messages, LlmConfig config) {
        return "Mock response";
    }
}