import {computed, effect, inject, Injectable, signal, untracked} from '@angular/core';

import {UserControllerService} from '@/api/backend/services/user-controller.service';
import {UserResponse} from '@/api/backend/models/user-response';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';

@Injectable({providedIn: 'root'})
export class SecurityService {
  private userController = inject(UserControllerService);
  private workspaceSelection = inject(WorkspaceSelectionService);

  readonly currentUser = signal<UserResponse | null>(null);
  readonly currentRole = computed(() => this.currentUser()?.role ?? null);
  readonly isOwner = computed(() => this.currentRole() === 'OWNER');
  readonly isEditor = computed(() => this.currentRole() === 'EDITOR');
  readonly isViewer = computed(() => this.currentRole() === 'VIEWER');

  constructor() {
    effect(() => {
      const workspaceId = this.workspaceSelection.selectedId();
      untracked(() => this.loadCurrentUser(workspaceId));
    });
  }

  private async loadCurrentUser(workspaceId: string | undefined) {
    if (!workspaceId) {
      this.currentUser.set(null);
      return;
    }
    const me = await this.userController.getMe({workspaceId});
    this.currentUser.set(me);
  }
}