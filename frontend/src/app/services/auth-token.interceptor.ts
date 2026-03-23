import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from './session.service';

interface AccountRestriction {
  type: 'locked' | 'deleted';
  message: string;
  supportEmail: string;
}

export const authTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const session = sessionService.get();

  const requestWithToken = (() => {
    if (!session?.token || req.headers.has('Authorization')) {
      return req;
    }

    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${session.token}`
      }
    });
  })();

  return next(requestWithToken).pipe(
    catchError((error) => {
      const payload = error?.error;
      const restriction = resolveAccountRestriction(error, payload, req.url, session?.role);
      if (restriction) {
        const onLoginRoute = router.url.startsWith('/login');

        sessionService.clear();
        sessionStorage.removeItem('registration-profile');
        if (!onLoginRoute) {
          router.navigate(['/login'], {
            queryParams: {
              accountRestricted: '1',
              restrictionType: restriction.type,
              restrictionMessage: restriction.message,
              supportEmail: restriction.supportEmail
            }
          });
        }
      }

      return throwError(() => error);
    })
  );
};

function resolveAccountRestriction(
  error: any,
  payload: any,
  requestUrl: string,
  sessionRole?: 'CUSTOMER' | 'OFFICER'
): AccountRestriction | null {
  const supportEmail =
    typeof payload?.supportEmail === 'string' && payload.supportEmail.trim().length > 0
      ? payload.supportEmail
      : 'support@pmslogistics.demo';

  if (error?.status === 423 || payload?.code === 'ACCOUNT_LOCKED') {
    return {
      type: 'locked',
      message:
        typeof payload?.error === 'string'
          ? payload.error
          : 'Your account has been locked. Please contact support for help.',
      supportEmail
    };
  }

  if (sessionRole !== 'CUSTOMER') {
    return null;
  }

  const message = typeof payload?.error === 'string' ? payload.error : '';
  const authProfileRequest = requestUrl.includes('/auth/profile/');
  const looksDeleted =
    payload?.code === 'ACCOUNT_DELETED' ||
    (/user profile not found/i.test(message) && authProfileRequest && (error?.status === 400 || error?.status === 404));

  if (!looksDeleted) {
    return null;
  }

  return {
    type: 'deleted',
    message: 'Your account has been deleted by admin. Please contact support for assistance.',
    supportEmail
  };
}
