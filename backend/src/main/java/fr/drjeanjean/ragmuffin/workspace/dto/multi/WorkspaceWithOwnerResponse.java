package fr.drjeanjean.ragmuffin.workspace.dto.multi;

import fr.drjeanjean.ragmuffin.user.dto.OwnerResponse;
import fr.drjeanjean.ragmuffin.workspace.dto.WorkspaceResponse;

public record WorkspaceWithOwnerResponse(
        WorkspaceResponse workspace,
        OwnerResponse owner
) {
}
