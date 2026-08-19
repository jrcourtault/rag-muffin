import {Component, viewChild} from '@angular/core';

import {PageContent} from '@/components/page-content/page-content';
import {UserForm} from '@/pages/rag/users/components/user-form/user-form';
import {UserTable} from './components/user-table/user-table';
import {UserToolbar} from './components/user-toolbar/user-toolbar';
import {UserPageStore} from '@/pages/rag/users/user-page.store';
import {UserResponse} from '@/api/backend/models/user-response';

@Component({
  selector: 'app-users',
  imports: [UserTable, UserToolbar, UserForm, PageContent],
  templateUrl: './users.html',
  providers: [UserPageStore],
})
export class Users {
  readonly formComponent = viewChild.required(UserForm);

  onCreate() {
    this.formComponent().open();
  }

  onEdit(user: UserResponse) {
    this.formComponent().open(user);
  }
}
