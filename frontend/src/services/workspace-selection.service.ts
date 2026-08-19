import {computed, inject, Injectable, signal} from '@angular/core';

import {WorkspaceControllerService} from '@/api/backend/services/workspace-controller.service';
import {WorkspaceResponse} from '@/api/backend/models/workspace-response';

@Injectable({providedIn: 'root'})
export class WorkspaceSelectionService {
  private workspaceController = inject(WorkspaceControllerService);

  readonly workspaces = signal<WorkspaceResponse[]>([]);
  readonly selected = signal<WorkspaceResponse | null>(null);
  readonly isInitializing = signal(true);

  readonly selectedId = computed(() => this.selected()?.id);
  readonly hasWorkspaces = computed(() => this.workspaces().length > 0);

  async load() {
    try {
      const workspaces = await this.workspaceController.myWorkspaces();
      workspaces.sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
      this.workspaces.set(workspaces);
      const currentId = this.selectedId();
      const stillExists = workspaces.find(t => t.id === currentId);
      this.selected.set(stillExists ?? workspaces[0] ?? null);
    } finally {
      this.isInitializing.set(false);
    }
  }
}
