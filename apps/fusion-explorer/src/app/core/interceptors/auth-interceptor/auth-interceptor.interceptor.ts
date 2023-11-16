import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { Observable, catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // injections
  const jwtAuthenticationService = inject(JwtAuthenticationService)
  const router = inject(Router);
  // try with current token
  return next(req)
  .pipe(
    catchError((err) => {
      // catching 401 on authenticated routes
      if (err instanceof HttpErrorResponse && err.status === 401) {
        // instantiate a new or get refresh running request
        let refresh: Observable<string>
        if (!jwtAuthenticationService.isRefreshing()) {
          refresh = jwtAuthenticationService.refreshToken()
          refresh.pipe(
            catchError((err) => {
              // navigate to err page if refresh failed
              router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } )
              return throwError(() => err)
            })
          )
          jwtAuthenticationService.setRefreshRequest(refresh)
        } else {
          refresh = jwtAuthenticationService.getRefreshRequest()
        }
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        return refresh.pipe(
          switchMap(() => {
            // retry any with refreshed token
            return next(req)
          })
        )
      }
      return throwError(() => err);
    })
  )
};
