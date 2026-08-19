import {inject} from '@angular/core';
import {Routes, Router} from '@angular/router';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {AuthService} from '@/services/auth.service';
import {SecurityService} from '@/services/security.service';
import {BreadcrumbData} from '@/app/template/breadcrumb-nav/breadcrumb-nav';

export const PATHS = {
  HOME: 'home',
  VERTICALS: 'verticals',
  WORKSPACES: 'workspaces',
  USERS: 'users',
  LLM_CONFIG: 'llm-config',
  DOCUMENTS: 'documents',
  CHAT: 'chat',
  SEARCH: 'search',
} as const;

const workspaceGuard = () => {
  const workspaceSelectionService = inject(WorkspaceSelectionService);
  const router = inject(Router);
  if (workspaceSelectionService.hasWorkspaces()) {
    return true;
  }
  return router.createUrlTree(['/', PATHS.HOME]);
};

const ownerGuard = () => {
  const securityService = inject(SecurityService);
  const router = inject(Router);
  if (securityService.isOwner()) {
    return true;
  }
  return router.createUrlTree(['/', PATHS.DOCUMENTS]);
};

const adminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAdmin) {
    return true;
  }
  return router.createUrlTree(['/', PATHS.CHAT]);
};

export const routes: Routes = [
  {
    path: '',
    redirectTo: PATHS.HOME, pathMatch: 'full'
  },
  {
    path: PATHS.HOME,
    loadComponent: () =>
      import('@/pages/home/home').then((m) => m.Home),
  },
  {
    path: PATHS.VERTICALS,
    canActivate: [adminGuard],
    data: { breadcrumbKey: 'nav.verticals', breadcrumbGroup: 'admin' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/admin/verticals/verticals').then((m) => m.Verticals),
  },
  {
    path: PATHS.WORKSPACES,
    canActivate: [adminGuard],
    data: { breadcrumbKey: 'nav.workspaces', breadcrumbGroup: 'admin' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/admin/workspaces/workspaces').then((m) => m.Workspaces),
  },
  {
    path: PATHS.USERS,
    canActivate: [workspaceGuard, ownerGuard],
    data: { breadcrumbKey: 'nav.users', breadcrumbGroup: 'workspace' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/rag/users/users').then((m) => m.Users),
  },
  {
    path: PATHS.LLM_CONFIG,
    canActivate: [workspaceGuard, ownerGuard],
    data: { breadcrumbKey: 'nav.llmConfig', breadcrumbGroup: 'workspace' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/rag/llm-config/llm-config').then((m) => m.LlmConfig),
  },
  {
    path: PATHS.DOCUMENTS,
    canActivate: [workspaceGuard],
    data: { breadcrumbKey: 'nav.documents', breadcrumbGroup: 'workspace' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/rag/documents/documents').then((m) => m.Documents),
  },
  {
    path: PATHS.SEARCH,
    canActivate: [workspaceGuard],
    data: { breadcrumbKey: 'nav.search', breadcrumbGroup: 'workspace' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/rag/search/search').then((m) => m.Search),
  },
  {
    path: PATHS.CHAT,
    canActivate: [workspaceGuard],
    data: { breadcrumbKey: 'nav.chat', breadcrumbGroup: 'workspace' } satisfies BreadcrumbData,
    loadComponent: () =>
      import('@/pages/rag/chat/chat').then((m) => m.Chat),
  },
  {
    path: '**',
    loadComponent: () =>
      import('@/pages/404/404').then((m) => m.Page404),
  },
];
