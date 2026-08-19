import {Component, inject, viewChild} from '@angular/core';

import {PageContent} from '@/components/page-content/page-content';
import {VerticalStore} from '@/pages/admin/verticals/vertical.store';
import {VerticalForm} from '@/pages/admin/verticals/components/vertical-form/vertical-form';
import {VerticalTable} from '@/pages/admin/verticals/components/vertical-table/vertical-table';
import {VerticalToolbar} from '@/pages/admin/verticals/components/vertical-toolbar/vertical-toolbar';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-verticals',
  imports: [PageContent, VerticalTable, VerticalToolbar, VerticalForm],
  templateUrl: './verticals.html',
  providers: [VerticalStore],
})
export class Verticals {
  private store = inject(VerticalStore);
  readonly formComponent = viewChild.required(VerticalForm);

  constructor() {
    this.store.load();
  }

  onCreate() {
    this.formComponent().open();
  }

  onEdit(vertical: VerticalResponse) {
    this.formComponent().open(vertical);
  }
}
