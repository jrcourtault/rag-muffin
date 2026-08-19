import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

import { AuthService } from '@/services/auth.service';
import { WorkspaceSelectionService } from '@/services/workspace-selection.service';
import { PATHS } from '@/app/app.routes';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  imports: [TranslocoPipe],
})
export class Home {
  private router = inject(Router);

  constructor() {
    const authService = inject(AuthService);
    const workspaceSelectionService = inject(WorkspaceSelectionService);

    if (authService.isAdmin) {
      this.router.navigate(['/', PATHS.VERTICALS]);
    } else if (workspaceSelectionService.hasWorkspaces()) {
      this.router.navigate(['/', PATHS.CHAT]);
    }
  }
}
