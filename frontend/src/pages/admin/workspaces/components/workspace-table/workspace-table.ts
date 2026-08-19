import {Component, inject, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {TableLazyLoadEvent, TableModule} from 'primeng/table';
import {Tag} from 'primeng/tag';
import {DeleteButton} from '@/components/buttons/delete-button/delete-button';
import {EditButton} from '@/components/buttons/edit-button/edit-button';
import {TABLE_DEFAULT_PAGE_SIZE, TABLE_PAGE_SIZE_OPTIONS} from '@/constants';
import {WorkspacePageStore} from '@/pages/admin/workspaces/workspace-page.store';
import {buildSortFromEvent} from '@/utils/table';
import {WorkspaceResponse} from '@/api/backend/models/workspace-response';

@Component({
  selector: 'app-workspace-table',
  templateUrl: './workspace-table.html',
  imports: [TranslocoPipe, TableModule, Tag, DeleteButton, EditButton],
})
export class WorkspaceTable {
  readonly store = inject(WorkspacePageStore);

  readonly editRequested = output<WorkspaceResponse>();

  readonly defaultPageSize = TABLE_DEFAULT_PAGE_SIZE;
  readonly pageSizeOptions = TABLE_PAGE_SIZE_OPTIONS;

  async onLoadPage(event: TableLazyLoadEvent) {
    const rows = event.rows ?? TABLE_DEFAULT_PAGE_SIZE;
    const page = Math.floor((event.first ?? 0) / rows);
    await this.store.changePage(page, rows, buildSortFromEvent(event));
  }

  async onDelete(workspace: WorkspaceResponse) {
    await this.store.delete(workspace.id!);
  }
}
