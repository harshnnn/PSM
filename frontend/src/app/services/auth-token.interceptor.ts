import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from './session.service';

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const session = sessionService.get();

  if (!session?.token) {
    return next(req);
  }

  if (req.headers.has('Authorization')) {
    return next(req);
  }

  const authenticatedRequest = req.clone({
    setHeaders: {
      Authorization: `Bearer ${session.token}`
    }
  });

  return next(authenticatedRequest);
};
