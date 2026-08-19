package fr.drjeanjean.ragmuffin.document.service.tool;

import fr.drjeanjean.ragmuffin.document.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class DocumentIngestionJmsPublisherMock implements DocumentIngestionJmsPublisher {

    @Override
    public void requestIngestion(Document document) {
        // No-op : pas de broker JMS en test
    }
}
