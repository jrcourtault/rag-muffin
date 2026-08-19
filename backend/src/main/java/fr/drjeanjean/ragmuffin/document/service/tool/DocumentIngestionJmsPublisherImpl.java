package fr.drjeanjean.ragmuffin.document.service.tool;

import fr.drjeanjean.ragmuffin.document.Document;
import fr.drjeanjean.ragmuffin.document.dto.IngestionJmsMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class DocumentIngestionJmsPublisherImpl implements DocumentIngestionJmsPublisher {

    private final JmsTemplate jmsTemplate;
    private final JsonMapper jsonMapper;

    @Override
    @SneakyThrows
    public void requestIngestion(Document document) {
        var message = new IngestionJmsMessage(
                document.getId(),
                document.getWorkspace().getId(),
                document.getFileName(),
                document.getExtension()
        );
        jmsTemplate.convertAndSend("document-ingestion", jsonMapper.writeValueAsString(message));
    }
}
