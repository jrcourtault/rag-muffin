import {inject, Injectable} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {MessageService} from 'primeng/api';

@Injectable({providedIn: 'root'})
export class ToastService {
  private messageService = inject(MessageService);
  private transloco = inject(TranslocoService);

  success(message?: string) {
    const m = message ?? this.transloco.translate('common.success');
    this.messageService.add({severity: 'success', summary: m});
  }

  error(message?: string) {
    const m = message ?? this.transloco.translate('common.error');
    this.messageService.add({severity: 'error', summary: m});
  }
}
