import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private pendingRequests = 0;
  readonly loading = signal(false);

  start() {
    this.pendingRequests++;
    this.loading.set(true);
  }

  stop() {
    this.pendingRequests = Math.max(0, this.pendingRequests - 1);
    if (this.pendingRequests === 0) {
      this.loading.set(false);
    }
  }
}
