import {Component, inject, input, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {InputText} from 'primeng/inputtext';
import {Select} from 'primeng/select';
import {Toolbar} from 'primeng/toolbar';
import {AddButton} from '@/components/buttons/add-button/add-button';
import {RefreshButton} from '@/components/buttons/refresh-button/refresh-button';
import {ACTIVE_STATUS_OPTIONS} from '@/models/enum-options';
import {WorkspaceFilters, WorkspacePageStore} from '@/pages/admin/workspaces/workspace-page.store';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-workspace-toolbar',
  templateUrl: './workspace-toolbar.html',
  imports: [TranslocoPipe, InputText, Select, Toolbar, AddButton, RefreshButton],
})
export class WorkspaceToolbar {
  readonly store = inject(WorkspacePageStore);

  readonly verticals = input<VerticalResponse[]>([]);
  readonly createRequested = output<void>();

  readonly statusOptions = ACTIVE_STATUS_OPTIONS;

  private filters: Partial<WorkspaceFilters> = {};

  onFilterName(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.filters = {...this.filters, name: value || undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterVertical(value: string | null) {
    this.filters = {...this.filters, verticalId: value ?? undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterActive(value: boolean | null) {
    this.filters = {...this.filters, active: value ?? undefined};
    this.store.changeFilter(this.filters);
  }
}
