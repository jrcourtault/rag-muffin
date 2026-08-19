package fr.drjeanjean.ragmuffin.rag.service;

import fr.drjeanjean.ragmuffin.rag.dto.AskResponse;
import fr.drjeanjean.ragmuffin.rag.dto.SearchResponse;

import java.util.UUID;

public interface RagService {

    AskResponse ask(String question, UUID workspaceId, boolean queryRewriting);

    SearchResponse search(String question, UUID workspaceId, boolean queryRewriting);
}
