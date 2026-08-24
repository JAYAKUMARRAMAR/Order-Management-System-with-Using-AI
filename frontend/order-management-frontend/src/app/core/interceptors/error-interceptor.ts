import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn =
  (req, next) => {
    return next(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred';

        if (error.status === 401) {
          errorMessage = 'Unauthorized: Please log in again';
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_username');
          window.location.href = '/login';
        } else if (error.status === 403) {
          errorMessage = 'Forbidden: You do not have access to this resource';
        } else if (error.status === 404) {
          errorMessage = 'Not found: The requested resource does not exist';
        } else if (error.status === 500) {
          errorMessage = 'Server error: Please try again later';
        } else if (error.error instanceof ErrorEvent) {
          errorMessage = error.error.message;
        } else {
          errorMessage = error.message || 'Unknown error occurred';
        }

        console.error('HTTP Error:', errorMessage, error);
        return throwError(() => new Error(errorMessage));
      })
    );
  };
