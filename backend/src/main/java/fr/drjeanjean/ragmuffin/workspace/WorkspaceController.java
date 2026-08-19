package fr.drjeanjean.ragmuffin.workspace;

import fr.drjeanjean.ragmuffin.infra.security.SecurityService;
import fr.drjeanjean.ragmuffin.user.dto.UserMapper;
import fr.drjeanjean.ragmuffin.user.service.UserService;
import fr.drjeanjean.ragmuffin.vertical.dto.VerticalMapper;
import fr.drjeanjean.ragmuffin.workspace.dto.WorkspaceMapper;
import fr.drjeanjean.ragmuffin.workspace.dto.WorkspaceResponse;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.CreateWorkspaceWithOwnerRequest;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.UpdateWorkspaceWithOwnerRequest;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.WorkspaceWithOwnerResponse;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.WorkspaceWithVerticalResponse;
import fr.drjeanjean.ragmuffin.workspace.service.WorkspaceService;
import fr.drjeanjean.ragmuffin.workspace.specification.WorkspaceSpecification;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/workspaces", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "createWorkspace")
    public WorkspaceResponse createWorkspace(@Valid @RequestBody CreateWorkspaceWithOwnerRequest request) {
        var workspace = workspaceService.create(request);
        return WorkspaceMapper.INSTANCE.toResponse(workspace);
    }

    @GetMapping
    @PreAuthorize("@securityService.isAdmin()")
    @Transactional(readOnly = true)
    @Operation(operationId = "listWorkspaces")
    public Page<WorkspaceWithVerticalResponse> listWorkspaces(@RequestParam(required = false) String name,
                                                              @RequestParam(required = false) UUID verticalId,
                                                              @RequestParam(required = false) Boolean active,
                                                              @ParameterObject Pageable pageable) {
        var spec = WorkspaceSpecification.withFilters(name, verticalId, active);
        var workspaces = workspaceService.getPage(spec, pageable);
        return workspaces.map(w -> new WorkspaceWithVerticalResponse(
                WorkspaceMapper.INSTANCE.toResponse(w),
                VerticalMapper.INSTANCE.toResponse(w.getVertical()))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.isAdmin()")
    @Transactional(readOnly = true)
    @Operation(operationId = "getWorkspace")
    public WorkspaceWithOwnerResponse getWorkspace(@PathVariable UUID id) {
        var workspace = workspaceService.findById(id);
        var owner = userService.findOwnerByWorkspaceId(workspace.getId());
        return new WorkspaceWithOwnerResponse(
                WorkspaceMapper.INSTANCE.toResponse(workspace),
                UserMapper.INSTANCE.toOwnerResponse(owner)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "updateWorkspace")
    public WorkspaceResponse updateWorkspace(@PathVariable UUID id,
                                             @Valid @RequestBody UpdateWorkspaceWithOwnerRequest request) {
        var workspace = workspaceService.findById(id);
        workspaceService.update(workspace, request);
        return WorkspaceMapper.INSTANCE.toResponse(workspace);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.isAdmin()")
    @Operation(operationId = "deleteWorkspace")
    public void deleteWorkspace(@PathVariable UUID id) {
        var workspace = workspaceService.findById(id);
        workspaceService.delete(workspace);
    }

    /// ////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    @Operation(operationId = "myWorkspaces")
    public List<WorkspaceResponse> myWorkspaces() {
        var workspaces = workspaceService.findActiveWorkspacesByUserIdpId(securityService.getIdpId());
        return WorkspaceMapper.INSTANCE.toResponse(workspaces);
    }
}
