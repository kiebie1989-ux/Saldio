import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/** Globale HTTP-Fehlerbehandlung: 401 → Login, 403/5xx/Netzfehler → Snackbar-Hinweis. */
export const fehlerInterceptor: HttpInterceptorFn = (req, next) => {
  const snack = inject(MatSnackBar);
  const auth = inject(AuthService);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        auth.login();
      } else if (err.status === 403) {
        melde(snack, 'Keine Berechtigung für diese Aktion.');
      } else if (err.status === 0) {
        melde(snack, 'Server nicht erreichbar.');
      } else if (err.status >= 500) {
        melde(snack, 'Serverfehler – bitte später erneut versuchen.');
      }
      return throwError(() => err);
    }),
  );
};

function melde(snack: MatSnackBar, text: string): void {
  snack.open(text, 'OK', { duration: 5000 });
}
