import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Drawer } from 'primeng/drawer';
import { ProgressBar } from 'primeng/progressbar';
import { TranslocoPipe } from '@jsverse/transloco';

import { BreadcrumbNav } from './breadcrumb-nav/breadcrumb-nav';
import { SidebarNav } from './sidebar-nav/sidebar-nav';
import { Topbar } from './topbar/topbar';
import { LoadingService } from '@/services/loading.service';

@Component({
  selector: 'app-template',
  templateUrl: './template.html',
  imports: [RouterOutlet, Drawer, ProgressBar, BreadcrumbNav, SidebarNav, Topbar, TranslocoPipe],
})
export class Template {
  readonly loadingService = inject(LoadingService);
  readonly drawerVisible = signal(false);

  protected toggleDrawer() {
    this.drawerVisible.update((v) => !v);
  }
}
