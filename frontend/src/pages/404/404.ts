import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-page-404',
  templateUrl: './404.html',
  imports: [TranslocoPipe, RouterLink],
})
export class Page404 {}
