import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoadingService } from '@/services/loading.service';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  // Ne tracker que les appels API, pas les fichiers statiques (traductions, assets…)
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  const loadingService = inject(LoadingService);
  loadingService.start();
  return next(req).pipe(finalize(() => loadingService.stop()));
};
