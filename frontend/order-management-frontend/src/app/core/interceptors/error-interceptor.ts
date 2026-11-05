import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn =
  (req, next) => {
    return next(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_username');
          window.location.href = '/login';
        }

        const errorMessage =
          error.error instanceof ErrorEvent
            ? error.error.message
            : error.error && typeof error.error === 'object' &&
                'message' in error.error
              ? String(error.error.message)
              : error.message || 'Unknown error occurred';

        console.error('HTTP Error:', errorMessage, error);
        return throwError(() => error);
      })
    );
  };
