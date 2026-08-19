import {Component, inject, output} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {InputText} from 'primeng/inputtext';
import {Select} from 'primeng/select';
import {Toolbar} from 'primeng/toolbar';
import {AddButton} from '@/components/buttons/add-button/add-button';
import {RefreshButton} from '@/components/buttons/refresh-button/refresh-button';
import {USER_ROLE_OPTIONS, UserRole} from '@/models/enum-options';
import {UserFilters, UserPageStore} from '@/pages/rag/users/user-page.store';

@Component({
  selector: 'app-user-toolbar',
  templateUrl: './user-toolbar.html',
  imports: [TranslocoPipe, InputText, Select, Toolbar, AddButton, RefreshButton],
})
export class UserToolbar {
  readonly store = inject(UserPageStore);

  readonly createRequested = output<void>();

  readonly roleOptions = USER_ROLE_OPTIONS;

  private filters: Partial<UserFilters> = {};

  onFilterName(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.filters = {...this.filters, name: value || undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterEmail(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.filters = {...this.filters, email: value || undefined};
    this.store.changeFilter(this.filters);
  }

  onFilterRole(value: UserRole | null) {
    this.filters = {...this.filters, role: value ?? undefined};
    this.store.changeFilter(this.filters);
  }
}
