import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

@Pipe({
  name: 'fileSize',
  pure: false,
})
export class FileSizePipe implements PipeTransform {
  private transloco = inject(TranslocoService);

  transform(bytes?: number): string {
    if (!bytes) return '—';
    if (bytes < 1024) return bytes + ' ' + this.transloco.translate('common.unit.bytes');
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' ' + this.transloco.translate('common.unit.kb');
    return (bytes / (1024 * 1024)).toFixed(1) + ' ' + this.transloco.translate('common.unit.mb');
  }
}
