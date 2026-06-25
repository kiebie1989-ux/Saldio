import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { runInInjectionContext, EnvironmentInjector } from '@angular/core';
import { authGuard, rolleGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('auth.guard', () => {
  let loginAufgerufen: boolean;
  let injector: EnvironmentInjector;

  function setup(authenticated: boolean, hatRolle: boolean) {
    loginAufgerufen = false;
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: { createUrlTree: () => ({}) as UrlTree } },
        {
          provide: AuthService,
          useValue: {
            authenticated: () => authenticated,
            hasRole: () => hatRolle,
            login: () => {
              loginAufgerufen = true;
            },
          },
        },
      ],
    });
    injector = TestBed.inject(EnvironmentInjector);
  }

  it('authGuard lässt angemeldete Benutzer durch', () => {
    setup(true, true);
    expect(runInInjectionContext(injector, () => authGuard(null!, null!))).toBe(true);
    expect(loginAufgerufen).toBe(false);
  });

  it('authGuard löst Login aus, wenn nicht angemeldet', () => {
    setup(false, false);
    expect(runInInjectionContext(injector, () => authGuard(null!, null!))).toBe(false);
    expect(loginAufgerufen).toBe(true);
  });

  it('rolleGuard verweigert ohne passende Rolle (UrlTree-Redirect)', () => {
    setup(true, false);
    const ergebnis = runInInjectionContext(injector, () => rolleGuard('admin')(null!, null!));
    expect(ergebnis).not.toBe(true);
  });

  it('rolleGuard erlaubt mit passender Rolle', () => {
    setup(true, true);
    expect(runInInjectionContext(injector, () => rolleGuard('admin')(null!, null!))).toBe(true);
  });
});
