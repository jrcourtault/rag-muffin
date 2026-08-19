import {Component, inject, signal, viewChild} from '@angular/core';

import {PageContent} from '@/components/page-content/page-content';
import {WorkspaceForm} from '@/pages/admin/workspaces/components/workspace-form/workspace-form';
import {WorkspaceTable} from './components/workspace-table/workspace-table';
import {WorkspaceToolbar} from './components/workspace-toolbar/workspace-toolbar';
import {WorkspacePageStore} from '@/pages/admin/workspaces/workspace-page.store';
import {WorkspaceResponse} from '@/api/backend/models/workspace-response';
import {VerticalControllerService} from '@/api/backend/services/vertical-controller.service';
import {VerticalResponse} from '@/api/backend/models/vertical-response';

@Component({
  selector: 'app-workspaces',
  imports: [WorkspaceTable, WorkspaceToolbar, WorkspaceForm, PageContent],
  templateUrl: './workspaces.html',
  providers: [WorkspacePageStore],
})
export class Workspaces {
  private verticalController = inject(VerticalControllerService);
  readonly formComponent = viewChild.required(WorkspaceForm);

  readonly verticals = signal<VerticalResponse[]>([]);

  constructor() {
    this.verticalController.listVerticals().then(v => this.verticals.set(v));
  }

  onCreate() {
    this.formComponent().open();
  }

  onEdit(workspace: WorkspaceResponse) {
    this.formComponent().open(workspace);
  }
}
