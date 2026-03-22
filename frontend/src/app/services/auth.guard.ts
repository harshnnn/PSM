import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from './session.service';

export const authGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
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
