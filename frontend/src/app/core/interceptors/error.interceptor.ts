import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Client Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            // Unauthorized - logout user
            authService.logout();
            errorMessage = 'Unauthorized. Please login again.';
            break;
          case 403:
            errorMessage = 'Access forbidden.';
            router.navigate(['/unauthorized']);
            break;
          case 404:
            errorMessage = 'Resource not found.';
            break;
          case 500:
            errorMessage = 'Internal server error.';
            break;
          default:
            errorMessage = error.error?.message || `Server Error: ${error.status}`;
        }
      }

      console.error('HTTP Error:', errorMessage, error);

      return throwError(() => ({
        message: errorMessage,
        status: error.status,
        error: error.error
      }));
    })
  );
};
