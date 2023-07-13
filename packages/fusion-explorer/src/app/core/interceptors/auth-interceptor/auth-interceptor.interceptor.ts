import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { Observable, catchError, retry, switchMap, throwError } from 'rxjs';

const TOKEN_HEADER_KEY = 'Authorization';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // injections
  const jwtAuthenticationService = inject(JwtAuthenticationService)
  const router = inject(Router);
  const authReq = req;
  return jwtAuthenticationService.getToken()
  .pipe(
    switchMap((token) => {
      // inject token
      return next(authReq.clone({ headers: authReq.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) }))
      .pipe(
        catchError((err) => {
          // catching 401 on authenticated routes
          if (err instanceof HttpErrorResponse && err.status === 401) {
            // instantiate a new or get refresh running request
            let refresh: Observable<string>
            if (!jwtAuthenticationService.isRefreshing()) {
              refresh = jwtAuthenticationService.refreshToken()
              jwtAuthenticationService.setRefreshRequest(refresh)
            } else {
              refresh = jwtAuthenticationService.getRefreshRequest()
            }
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            return refresh.pipe(
                retry(2),
                catchError((err) => {
                  // navigate to err page if refresh failed
                  router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } )
                  return throwError(() => err)
                }),
                switchMap((token) => {
                  // retry with refreshed token
                  return next(authReq.clone({ headers: authReq.headers.set(TOKEN_HEADER_KEY, 'Bearer ' + token) }))
                })
              )
          }
          return throwError(() => err);
        })
      )
    })
  )
};
