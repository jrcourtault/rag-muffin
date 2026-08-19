package fr.drjeanjean.ragmuffin.workspace.service;

import fr.drjeanjean.ragmuffin.document.service.DocumentService;
import fr.drjeanjean.ragmuffin.llmconfig.dto.UpdateLlmConfigRequest;
import fr.drjeanjean.ragmuffin.llmconfig.properties.DefaultLlmConfigProperties;
import fr.drjeanjean.ragmuffin.user.service.UserService;
import fr.drjeanjean.ragmuffin.vertical.VerticalRepository;
import fr.drjeanjean.ragmuffin.workspace.Workspace;
import fr.drjeanjean.ragmuffin.workspace.WorkspaceRepository;
import fr.drjeanjean.ragmuffin.workspace.dto.WorkspaceMapper;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.CreateWorkspaceWithOwnerRequest;
import fr.drjeanjean.ragmuffin.workspace.dto.multi.UpdateWorkspaceWithOwnerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final VerticalRepository verticalRepository;
    private final UserService userService;
    private final DocumentService documentService;
    private final DefaultLlmConfigProperties defaultLlmConfigProperties;

    public Workspace create(CreateWorkspaceWithOwnerRequest request) {
        var vertical = verticalRepository.getReferenceById(request.createWorkspaceRequest().verticalId());
        var workspace = workspaceRepository.save(
                WorkspaceMapper.INSTANCE.toEntity(request.createWorkspaceRequest(), vertical, defaultLlmConfigProperties));
        userService.createOwner(workspace.getId(), request.createOwnerRequest());
        log.info("Created workspace {} ({})", workspace.getId(), workspace.getName());
        return workspace;
    }

    public Page<Workspace> getPage(Specification<Workspace> spec, Pageable pageable) {
        return workspaceRepository.findAll(spec, pageable);
    }

    public Workspace findById(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void update(Workspace workspace, UpdateWorkspaceWithOwnerRequest request) {
        var r = request.updateWorkspaceRequest();
        var vertical = verticalRepository.getReferenceById(r.verticalId());
        workspace.update(r.name(), vertical, r.active(),
                r.prefetchSize(), r.rerank(), r.topK());
        userService.updateOwner(workspace.getId(), request.updateOwnerRequest());
        log.info("Updated workspace {} ({})", workspace.getId(), workspace.getName());
    }

    public void updateLlmConfig(Workspace workspace, UpdateLlmConfigRequest request) {
        workspace.updateLlmConfig(request.baseUrl(), request.apiKey(), request.model());
        log.info("Updated LLM config for workspace {}", workspace.getId());
    }

    public void delete(Workspace workspace) {
        if (workspace.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete an active workspace");
        }
        documentService.deleteAllByWorkspaceId(workspace.getId());
        workspaceRepository.delete(workspace);
        log.info("Deleted workspace {} ({})", workspace.getId(), workspace.getName());
    }

    public List<Workspace> findActiveWorkspacesByUserIdpId(UUID idpId) {
        return workspaceRepository.findActiveByUserIdpId(idpId);
    }
}
