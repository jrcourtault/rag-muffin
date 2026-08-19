import {Component, computed, inject, output} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {TranslocoPipe} from '@jsverse/transloco';

import {PATHS} from '@/app/app.routes';
import {AuthService} from '@/services/auth.service';
import {SecurityService} from '@/services/security.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';

interface NavLink {
  path: string;
  icon: string;
  label: string;
}

interface NavGroup {
  title: string;
  links: NavLink[];
  adminOnly?: boolean;
  ownerOnly?: boolean;
}

@Component({
  standalone: true,
  selector: 'app-sidebar-nav',
  imports: [RouterLink, RouterLinkActive, TranslocoPipe],
  templateUrl: './sidebar-nav.html',
})
export class SidebarNav {
  private authService = inject(AuthService);
  private securityService = inject(SecurityService);
  private workspaceSelectionService = inject(WorkspaceSelectionService);
  readonly linkClicked = output<void>();

  private readonly allGroups: NavGroup[] = [
    {
      title: 'nav.group.admin',
      adminOnly: true,
      links: [
        {path: PATHS.VERTICALS, icon: 'pi pi-th-large', label: 'nav.verticals'},
        {path: PATHS.WORKSPACES, icon: 'pi pi-building', label: 'nav.workspaces'},
      ],
    },
    {
      title: 'nav.group.configuration',
      ownerOnly: true,
      links: [
        {path: PATHS.USERS, icon: 'pi pi-users', label: 'nav.users'},
        {path: PATHS.LLM_CONFIG, icon: 'pi pi-microchip-ai', label: 'nav.llmConfig'},
      ],
    },
    {
      title: 'nav.group.rag',
      links: [
        {path: PATHS.DOCUMENTS, icon: 'pi pi-file', label: 'nav.documents'},
        {path: PATHS.CHAT, icon: 'pi pi-comments', label: 'nav.chat'},
        {path: PATHS.SEARCH, icon: 'pi pi-search', label: 'nav.search'},
      ],
    },
  ];

  readonly menu = computed(() => {
    const hasWorkspaces = this.workspaceSelectionService.hasWorkspaces();
    return this.allGroups.filter(g => {
      if (g.adminOnly) return this.authService.isAdmin;
      if (g.ownerOnly) return hasWorkspaces && this.securityService.isOwner();
      return hasWorkspaces;
    });
  });

  protected clickMenu() {
    this.linkClicked.emit();
  }
}
