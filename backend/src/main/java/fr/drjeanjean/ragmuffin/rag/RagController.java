package fr.drjeanjean.ragmuffin.rag;

import fr.drjeanjean.ragmuffin.rag.dto.AskRequest;
import fr.drjeanjean.ragmuffin.rag.dto.AskResponse;
import fr.drjeanjean.ragmuffin.rag.dto.SearchRequest;
import fr.drjeanjean.ragmuffin.rag.dto.SearchResponse;
import fr.drjeanjean.ragmuffin.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workspaces/{workspaceId}/rag", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @PostMapping("/ask")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "askQuestion")
    public AskResponse ask(@PathVariable UUID workspaceId, @Valid @RequestBody AskRequest request) {
        return ragService.ask(request.question(), workspaceId, request.queryRewriting());
    }

    @PostMapping("/search")
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER', 'EDITOR', 'VIEWER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "searchDocuments")
    public SearchResponse search(@PathVariable UUID workspaceId, @Valid @RequestBody SearchRequest request) {
        return ragService.search(request.question(), workspaceId, request.queryRewriting());
    }
}
