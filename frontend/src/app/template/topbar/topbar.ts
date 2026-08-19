import {Component, inject, output, signal} from '@angular/core';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {Popover} from 'primeng/popover';
import {Toolbar} from 'primeng/toolbar';
import {ButtonModule} from 'primeng/button';
import {Select} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {AuthService} from '@/services/auth.service';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';
import {PATHS} from '@/app/app.routes';

interface Lang {
  code: string;
  flag: string;
}

@Component({
  standalone: true,
  selector: 'app-topbar',
  imports: [Popover, Toolbar, ButtonModule, Select, FormsModule, TranslocoPipe, RouterLink],
  templateUrl: './topbar.html',
})
export class Topbar {
  readonly PATHS = PATHS;
  private transloco = inject(TranslocoService);
  readonly workspaceSelectionService = inject(WorkspaceSelectionService);
  readonly authService = inject(AuthService);
  readonly menuToggled = output<void>();

  readonly langs: Lang[] = [
    { code: 'fr', flag: '\uD83C\uDDEB\uD83C\uDDF7' },
    { code: 'en', flag: '\uD83C\uDDEC\uD83C\uDDE7' },
  ];
  selectedLang = signal(this.langs[0]);

  onLangChange(lang: Lang) {
    this.selectedLang.set(lang);
    this.transloco.setActiveLang(lang.code);
  }
}
