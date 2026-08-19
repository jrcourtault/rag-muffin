package fr.drjeanjean.ragmuffin.workspace.dto.multi;

import fr.drjeanjean.ragmuffin.vertical.dto.VerticalResponse;
import fr.drjeanjean.ragmuffin.workspace.dto.WorkspaceResponse;

public record WorkspaceWithVerticalResponse(
        WorkspaceResponse workspace,
        VerticalResponse vertical
) {
}
