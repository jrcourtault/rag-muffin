package fr.drjeanjean.ragmuffin.llmconfig;

import fr.drjeanjean.ragmuffin.llmconfig.dto.LlmConfigMapper;
import fr.drjeanjean.ragmuffin.llmconfig.dto.LlmConfigResponse;
import fr.drjeanjean.ragmuffin.llmconfig.dto.UpdateLlmConfigRequest;
import fr.drjeanjean.ragmuffin.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workspaces/{workspaceId}/llm-config", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class LlmConfigController {

    private final WorkspaceService workspaceService;

    @GetMapping
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Transactional(readOnly = true)
    @Operation(operationId = "getLlmConfig")
    public LlmConfigResponse getLlmConfig(@PathVariable UUID workspaceId) {
        var workspace = workspaceService.findById(workspaceId);
        return LlmConfigMapper.INSTANCE.toResponse(workspace);
    }

    @PutMapping
    @PreAuthorize("@securityService.workspaceIsEnableAndHasRole(#workspaceId, 'OWNER')")
    @Operation(operationId = "updateLlmConfig")
    public LlmConfigResponse updateLlmConfig(@PathVariable UUID workspaceId,
                                             @Valid @RequestBody UpdateLlmConfigRequest request) {
        var workspace = workspaceService.findById(workspaceId);
        workspaceService.updateLlmConfig(workspace, request);
        return LlmConfigMapper.INSTANCE.toResponse(workspace);
    }
}
