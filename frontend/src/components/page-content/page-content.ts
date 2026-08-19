import {Component, input} from '@angular/core';
import {TranslocoPipe} from '@jsverse/transloco';
import {Card} from 'primeng/card';

@Component({
  selector: 'app-page-content',
  templateUrl: './page-content.html',
  imports: [TranslocoPipe, Card],
})
export class PageContent {
  readonly title = input.required<string>();
}
