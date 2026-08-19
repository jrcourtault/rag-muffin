package fr.drjeanjean.ragmuffin.document.service.tool;

import fr.drjeanjean.ragmuffin.document.Document;

public interface DocumentIngestionJmsPublisher {

    void requestIngestion(Document document);
}
