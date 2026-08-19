import {Component, inject, output} from '@angular/core';
import {Toolbar} from 'primeng/toolbar';

import {AddButton} from '@/components/buttons/add-button/add-button';
import {RefreshButton} from '@/components/buttons/refresh-button/refresh-button';
import {VerticalStore} from '@/pages/admin/verticals/vertical.store';

@Component({
  selector: 'app-vertical-toolbar',
  templateUrl: './vertical-toolbar.html',
  imports: [Toolbar, AddButton, RefreshButton],
})
export class VerticalToolbar {
  readonly store = inject(VerticalStore);
  readonly createRequested = output<void>();
}
