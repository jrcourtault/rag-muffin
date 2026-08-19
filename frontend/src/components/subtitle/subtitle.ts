import {Component, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';

@Component({
  selector: 'app-subtitle',
  templateUrl: './subtitle.html',
  imports: [TranslocoPipe],
})
export class Subtitle {
  readonly title = input.required<string>();
}