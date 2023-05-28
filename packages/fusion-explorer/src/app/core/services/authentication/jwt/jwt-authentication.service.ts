import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { UserInfo } from '@core/models/user-info.model';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store } from '@ngrx/store';
import { Observable, catchError, retry, throwError } from 'rxjs';

@Injectable({
  providedIn: CoreModule
})
export class JwtAuthenticationService {

  private baseUrl!: string;

  constructor(
    private http: HttpClient,
    private readonly store: Store,
  ) {
    this.store.select(selectAppConfig).subscribe((conf) => {
      this.baseUrl = '/'+(conf?.urls.rest.endpoints.AUTH);
    });
  }

  getUserInfo(): Observable<UserInfo> {
    return this.http.get<UserInfo>(this.baseUrl + '/userinfo')
    .pipe(
      retry(2),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 0) {
          console.warn('> JwtAuthenticationService#userInfo() >> an error occured on http request:', error.error);
        } else {
          console.warn('> JwtAuthenticationService#userInfo() >> server returned code %d with body:', error.status, error.error);
        }
        const err = new Error('An error occured while verifying token');
        return throwError(() => err);
      })
    )
  }

}
