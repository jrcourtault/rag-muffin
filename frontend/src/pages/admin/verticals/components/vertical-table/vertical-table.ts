import {Component, inject, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {TableModule} from 'primeng/table';

import {DeleteButton} from '@/components/buttons/delete-button/delete-button';
import {EditButton} from '@/components/buttons/edit-button/edit-button';
import {VerticalStore} from '@/pages/admin/verticals/vertical.store';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-vertical-table',
  templateUrl: './vertical-table.html',
  imports: [TranslocoPipe, TableModule, DeleteButton, EditButton],
})
export class VerticalTable {
  readonly store = inject(VerticalStore);
  readonly editRequested = output<VerticalResponse>();

  async onDelete(vertical: VerticalResponse) {
    await this.store.delete(vertical.id!);
  }
}
