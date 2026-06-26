import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideEchartsCore } from 'ngx-echarts';
import { provideOAuthClient } from 'angular-oauth2-oidc';

import { routes } from './app.routes';
import { AuthService } from './core/auth.service';
import { fehlerInterceptor } from './core/fehler.interceptor';

registerLocaleData(localeDe);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: 'de-DE' },
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptorsFromDi(), withInterceptors([fehlerInterceptor])),
    provideAnimationsAsync(),
    provideEchartsCore({ echarts: () => import('echarts') }),
    // OIDC: hängt das Access-Token automatisch an /api-Requests (Bearer).
    provideOAuthClient({ resourceServer: { allowedUrls: ['/api'], sendAccessToken: true } }),
    // Discovery + Session beim Start laden, bevor die App rendert.
    provideAppInitializer(() => inject(AuthService).init()),
  ],
};
