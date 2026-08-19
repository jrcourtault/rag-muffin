package fr.drjeanjean.ragmuffin.workspace.dto.multi;

import fr.drjeanjean.ragmuffin.user.dto.UpdateOwnerRequest;
import fr.drjeanjean.ragmuffin.workspace.dto.UpdateWorkspaceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkspaceWithOwnerRequest(
        @NotNull @Valid UpdateWorkspaceRequest updateWorkspaceRequest,
        @NotNull @Valid UpdateOwnerRequest updateOwnerRequest
) {
}
