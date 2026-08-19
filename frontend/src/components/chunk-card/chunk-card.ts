import {Component, inject, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';

import {Tag} from 'primeng/tag';
import {Button} from 'primeng/button';
import {Panel} from 'primeng/panel';
import {DocumentControllerService} from '@/api/backend/services/document-controller.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {formatScore} from '@/utils/format';
import {ChunkResult} from '@/api/backend/models/chunk-result';

@Component({
  selector: 'app-chunk-card',
  templateUrl: './chunk-card.html',
  imports: [
    TranslocoPipe,
    Tag,
    Button,
    Panel,
  ],
})
export class ChunkCard {
  private documentController = inject(DocumentControllerService);
  private workspaceSelection = inject(WorkspaceSelectionService);

  readonly chunk = input.required<ChunkResult>();
  readonly index = input(0);
  readonly formatScore = formatScore;

  async download() {
    const c = this.chunk();
    const workspaceId = this.workspaceSelection.selectedId()!;
    const blob = await this.documentController.downloadDocument({workspaceId, id: c.documentId!});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = c.fileName ?? 'download';
    a.click();
    URL.revokeObjectURL(url);
  }
}
