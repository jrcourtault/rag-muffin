import { Component, input, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-edit-button',
  templateUrl: './edit-button.html',
  imports: [ButtonModule],
})
export class EditButton {
  readonly disabled = input(false);
  readonly clicked = output<void>();
}
