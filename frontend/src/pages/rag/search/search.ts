import {Component, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslocoPipe} from '@jsverse/transloco';

import {InputText} from 'primeng/inputtext';
import {ToggleSwitch} from 'primeng/toggleswitch';

import {PageContent} from '@/components/page-content/page-content';
import {ChunkCard} from '@/components/chunk-card/chunk-card';
import {Spinner} from '@/components/spinner/spinner';
import {RagControllerService} from '@/api/backend/services/rag-controller.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {ToastService} from '@/services/toast.service';
import {SearchResponse} from '@/api/backend/models/search-response';
import {RAG_QUERY_REWRITING_DEFAULT} from '@/constants';
import {Subtitle} from '@/components/subtitle/subtitle';

@Component({
  selector: 'app-search',
  templateUrl: './search.html',
  imports: [
    FormsModule,
    TranslocoPipe,
    InputText,
    PageContent,
    ChunkCard,
    Spinner,
    ToggleSwitch,
    Subtitle,
  ],
})
export class Search {
  private ragController = inject(RagControllerService);
  private workspaceSelection = inject(WorkspaceSelectionService);
  private toast = inject(ToastService);

  query = signal('');
  response = signal<SearchResponse | null>(null);
  loading = signal(false);
  queryRewriting = signal(RAG_QUERY_REWRITING_DEFAULT);

  async search() {
    const q = this.query().trim();
    if (!q) return;

    const workspaceId = this.workspaceSelection.selectedId();
    if (!workspaceId) return;

    this.loading.set(true);
    this.response.set(null);
    try {
      this.response.set(await this.ragController.searchDocuments({
        workspaceId,
        body: {question: q, queryRewriting: this.queryRewriting()},
      }));
    } catch {
      this.toast.error();
    } finally {
      this.loading.set(false);
    }
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.search();
    }
  }
}
