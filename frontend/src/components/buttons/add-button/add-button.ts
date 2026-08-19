import { Component, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-add-button',
  templateUrl: './add-button.html',
  imports: [TranslocoPipe, ButtonModule],
})
export class AddButton {
  readonly disabled = input(false);
  readonly clicked = output<void>();
}
