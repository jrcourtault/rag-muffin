import {Component, inject, input, output} from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { ButtonModule } from 'primeng/button';
import { ConfirmPopup } from 'primeng/confirmpopup';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-delete-button',
  templateUrl: './delete-button.html',
  imports: [ButtonModule, ConfirmPopup],
  providers: [ConfirmationService],
})
export class DeleteButton {
  private confirmationService = inject(ConfirmationService);
  private transloco = inject(TranslocoService);

  readonly disabled = input(false);
  readonly confirmed = output<void>();

  onDelete(event: Event) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: this.transloco.translate('common.confirm'),
      acceptLabel: this.transloco.translate('common.yes'),
      rejectLabel: this.transloco.translate('common.no'),
      acceptButtonStyleClass: 'p-button-danger p-button-sm',
      rejectButtonStyleClass: 'p-button-secondary p-button-text p-button-sm',
      accept: () => this.confirmed.emit(),
    });
  }
}
