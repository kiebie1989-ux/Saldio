import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Rolle } from './auth.service';

/** Erfordert eine gültige Anmeldung; leitet sonst zum Keycloak-Login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.authenticated()) {
    return true;
  }
  auth.login();
  return false;
};

/** Erfordert eine bestimmte Rolle; leitet sonst zum Dashboard zurück. */
export function rolleGuard(rolle: Rolle): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.authenticated()) {
      auth.login();
      return false;
    }
    return auth.hasRole(rolle) ? true : router.createUrlTree(['/dashboard']);
  };
}
