import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { MenuItem } from 'primeng/api';
import { Breadcrumb } from 'primeng/breadcrumb';
import { Card } from 'primeng/card';
import { TranslocoService } from '@jsverse/transloco';

import { WorkspaceSelectionService } from '@/services/workspace-selection.service';
import { PATHS } from '@/app/app.routes';

export interface BreadcrumbData {
  breadcrumbKey: string;
  breadcrumbGroup: 'workspace' | 'admin';
}

@Component({
  selector: 'app-breadcrumb-nav',
  templateUrl: './breadcrumb-nav.html',
  imports: [Breadcrumb, Card],
})
export class BreadcrumbNav {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly transloco = inject(TranslocoService);
  private readonly workspaceSelectionService = inject(WorkspaceSelectionService);

  private readonly activeLang = toSignal(this.transloco.langChanges$, {
    initialValue: this.transloco.getActiveLang(),
  });

  private readonly routeData = toSignal<BreadcrumbData | null>(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let route = this.activatedRoute.root;
        while (route.firstChild) route = route.firstChild;
        return (route.snapshot.data as BreadcrumbData) ?? null;
      })
    ),
    { initialValue: null }
  );

  readonly homeItem: MenuItem = { icon: 'pi pi-home', routerLink: '/' + PATHS.HOME };

  readonly breadcrumbItems = computed<MenuItem[]>(() => {
    const data = this.routeData();
    const workspace = this.workspaceSelectionService.selected();
    this.activeLang(); // réabonnement sur changement de langue

    if (!data?.breadcrumbKey) return [];

    const t = (key: string) => this.transloco.translate(key);
    const firstLabel = data.breadcrumbGroup === 'workspace'
      ? workspace?.name
      : t('nav.group.admin');

    return [{ label: firstLabel }, { label: t(data.breadcrumbKey) }];
  });
}
