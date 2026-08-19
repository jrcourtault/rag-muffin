package fr.drjeanjean.ragmuffin.workspace.dto.multi;

import fr.drjeanjean.ragmuffin.user.dto.CreateOwnerRequest;
import fr.drjeanjean.ragmuffin.workspace.dto.CreateWorkspaceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateWorkspaceWithOwnerRequest(
        @NotNull @Valid CreateWorkspaceRequest createWorkspaceRequest,
        @NotNull @Valid CreateOwnerRequest createOwnerRequest
) {
}
