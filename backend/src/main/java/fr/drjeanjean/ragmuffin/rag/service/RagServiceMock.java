package fr.drjeanjean.ragmuffin.rag.service;

import fr.drjeanjean.ragmuffin.rag.dto.AskResponse;
import fr.drjeanjean.ragmuffin.rag.dto.SearchResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Profile("test")
public class RagServiceMock implements RagService {

    @Override
    public AskResponse ask(String question, UUID workspaceId, boolean queryRewriting) {
        return new AskResponse("Mock response", null, List.of());
    }

    @Override
    public SearchResponse search(String question, UUID workspaceId, boolean queryRewriting) {
        return new SearchResponse(null, List.of());
    }
}
