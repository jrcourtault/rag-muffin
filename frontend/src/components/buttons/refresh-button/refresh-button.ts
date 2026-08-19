import { Component, output, input } from '@angular/core';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-refresh-button',
  templateUrl: './refresh-button.html',
  imports: [ButtonModule],
})
export class RefreshButton {
  readonly loading = input(false);
  readonly clicked = output<void>();
}
