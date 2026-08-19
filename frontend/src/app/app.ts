import {Component, inject} from '@angular/core';
import {Toast} from 'primeng/toast';
import {ProgressSpinner} from 'primeng/progressspinner';
import {PrimeNG} from 'primeng/config';
import {TranslocoService} from '@jsverse/transloco';

import {Template} from './template/template';
import {WorkspaceSelectionService} from '@/services/workspace-selection.service';

@Component({
  selector: 'app-root',
  imports: [Toast, ProgressSpinner, Template],
  templateUrl: './app.html',
})
export class App {
  readonly workspaceSelectionService = inject(WorkspaceSelectionService);
  private primeng = inject(PrimeNG);
  private transloco = inject(TranslocoService);

  constructor() {
    // Le selecteur de workspace
    this.workspaceSelectionService.load();

    // i18n de PrimeNG
    this.transloco.selectTranslate('common.noFileChosen').subscribe(() => {
      this.primeng.setTranslation({
        noFileChosenMessage: this.transloco.translate('common.noFileChosen'),
        fileChosenMessage: this.transloco.translate('common.fileChosen'),
      });
    });
  }
}
