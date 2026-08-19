package fr.drjeanjean.ragmuffin.infra.reranker;

import fr.drjeanjean.ragmuffin.infra.qdrant.dto.ScoredChunk;

import java.util.List;

public interface RerankerService {

    /**
     * Re-ranks the given chunks by relevance to the query.
     * Returns chunks sorted by reranker score (highest first).
     */
    List<ScoredChunk> rerank(String query, List<ScoredChunk> chunks);
}
