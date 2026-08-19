import {Component, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslocoPipe} from '@jsverse/transloco';

import {InputText} from 'primeng/inputtext';
import {Card} from 'primeng/card';
import {ToggleSwitch} from 'primeng/toggleswitch';

import {PageContent} from '@/components/page-content/page-content';
import {Subtitle} from '@/components/subtitle/subtitle';
import {ChunkCard} from '@/components/chunk-card/chunk-card';
import {Spinner} from '@/components/spinner/spinner';
import {RagControllerService} from '@/api/backend/services/rag-controller.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {ToastService} from '@/services/toast.service';
import {AskResponse} from '@/api/backend/models/ask-response';
import {RAG_QUERY_REWRITING_DEFAULT} from '@/constants';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  imports: [
    FormsModule,
    TranslocoPipe,
    InputText,
    Card,
    PageContent,
    Subtitle,
    ChunkCard,
    Spinner,
    ToggleSwitch,
  ],
})
export class Chat {
  private ragController = inject(RagControllerService);
  private workspaceSelection = inject(WorkspaceSelectionService);
  private toast = inject(ToastService);

  question = signal('');
  response = signal<AskResponse | null>(null);
  loading = signal(false);
  queryRewriting = signal(RAG_QUERY_REWRITING_DEFAULT);

  async ask() {
    const q = this.question().trim();
    if (!q) return;

    const workspaceId = this.workspaceSelection.selectedId();
    if (!workspaceId) return;

    this.loading.set(true);
    this.response.set(null);
    try {
      const result = await this.ragController.askQuestion({
        workspaceId,
        body: {question: q, queryRewriting: this.queryRewriting()},
      });
      this.response.set(result);
    } catch {
      this.toast.error();
    } finally {
      this.loading.set(false);
    }
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.ask();
    }
  }

}
