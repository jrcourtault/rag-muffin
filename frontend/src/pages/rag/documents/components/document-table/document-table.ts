import {Component, effect, inject, output, viewChild} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {TranslocoDatePipe} from '@jsverse/transloco-locale';
import {Table, TableLazyLoadEvent, TableModule} from 'primeng/table';
import {Tag} from 'primeng/tag';

import {DocumentResponse} from '@/api/backend/models/document-response';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {FileSizePipe} from '@/pipes/file-size.pipe';
import {DeleteButton} from '@/components/buttons/delete-button/delete-button';
import {EditButton} from '@/components/buttons/edit-button/edit-button';
import {TABLE_DEFAULT_PAGE_SIZE, TABLE_PAGE_SIZE_OPTIONS} from '@/constants';
import {DocumentPageStore} from '@/pages/rag/documents/document-page.store';
import {SecurityService} from '@/services/security.service';
import {buildSortFromEvent} from '@/utils/table';

@Component({
  selector: 'app-document-table',
  templateUrl: './document-table.html',
  imports: [TranslocoPipe, TranslocoDatePipe, TableModule, Tag, FileSizePipe, DeleteButton, EditButton],
})
export class DocumentTable {
  readonly store = inject(DocumentPageStore);
  readonly security = inject(SecurityService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);

  readonly editRequested = output<DocumentResponse>();

  private table = viewChild.required(Table);

  readonly defaultPageSize = TABLE_DEFAULT_PAGE_SIZE;
  readonly pageSizeOptions = TABLE_PAGE_SIZE_OPTIONS;

  constructor() {
    effect(() => {
      // On reset le tableau quand on change de workspace
      this.workspaceSelectionService.selectedId();
      this.table().reset();
    });
  }

  async onLoadPage(event: TableLazyLoadEvent) {
    const rows = event.rows ?? TABLE_DEFAULT_PAGE_SIZE;
    const page = Math.floor((event.first ?? 0) / rows);
    await this.store.changePage(page, rows, buildSortFromEvent(event));
  }

  async onDownload(doc: DocumentResponse) {
    await this.store.download(doc);
  }

  async onDelete(doc: DocumentResponse) {
    await this.store.delete(doc.id!);
  }
}
