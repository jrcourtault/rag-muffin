import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { ToastService } from '@/services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error) => {
      toastService.error();
      return throwError(() => error);
    }),
  );
};
