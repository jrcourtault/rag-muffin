import {Component, effect, inject, output, viewChild} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {Table, TableLazyLoadEvent, TableModule} from 'primeng/table';
import {Tag} from 'primeng/tag';

import {UserResponse} from '@/api/backend/models/user-response';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {DeleteButton} from '@/components/buttons/delete-button/delete-button';
import {EditButton} from '@/components/buttons/edit-button/edit-button';
import {TABLE_DEFAULT_PAGE_SIZE, TABLE_PAGE_SIZE_OPTIONS} from '@/constants';
import {UserPageStore} from '@/pages/rag/users/user-page.store';
import {buildSortFromEvent} from '@/utils/table';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.html',
  imports: [TranslocoPipe, TableModule, Tag, DeleteButton, EditButton],
})
export class UserTable {
  readonly store = inject(UserPageStore);
  private workspaceSelectionService = inject(WorkspaceSelectionService);

  readonly editRequested = output<UserResponse>();

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

  async onDelete(user: UserResponse) {
    await this.store.delete(user.id!);
  }
}
