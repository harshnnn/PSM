import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const authApi = inject(AuthApiService);
  const session = sessionService.get();

  if (!session) {
    return router.createUrlTree(['/login']);
  }

  // Officer account is validated at login time; customer session must still exist in auth-service after restarts.
  if (session.role === 'OFFICER') {
    return true;
  }

  return authApi.profile(session.username).pipe(
    map(() => true),
    catchError(() => {
      sessionService.clear();
      sessionStorage.removeItem('registration-profile');
      return of(router.createUrlTree(['/login'], { queryParams: { sessionExpired: '1' } }));
    })
  );
};

export const officerGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const session = sessionService.get();

  if (session?.role === 'OFFICER') {
    return true;
  }

  return router.createUrlTree(['/home']);
};

export const customerGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const session = sessionService.get();

  if (session?.role === 'CUSTOMER') {
    return true;
  }

  return router.createUrlTree(['/admin']);
};

// If a user is already logged in, block access to login/register and redirect to their landing page.
export const redirectIfLoggedInGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
  const session = sessionService.get();

  if (session) {
    return router.createUrlTree([session.role === 'OFFICER' ? '/admin' : '/home']);
  }

  return true;
};
