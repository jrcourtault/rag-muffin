package fr.drjeanjean.ragmuffin.infra.reranker;

import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("test")
public class RerankerServiceMock implements RerankerService {

    @Override
    public List<ScoredChunk> rerank(String query, List<ScoredChunk> chunks) {
        return chunks;
    }
}
