import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { UserInfo } from '@core/models/user-info.model'
import { LocalStorageService } from '@core/services/utils/local-storage/local-storage.service'
import { selectAppConfig } from '@core/states/app-state/app-state.selectors'
import { Store } from '@ngrx/store'
import { Observable, catchError, map, of, retry, switchMap, tap, throwError } from 'rxjs'

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json'
  })
}

@Injectable()
export class JwtAuthenticationService {

  private baseUrl!: string
  private refreshRequest?: Observable<string>

  constructor(
    private http: HttpClient,
    private readonly store: Store,
    private readonly localStorageService: LocalStorageService
  ) {
    this.store.select(selectAppConfig).subscribe((conf) => {
      this.baseUrl = '/'+(conf?.urls.rest.endpoints.AUTH)
    })
  }

  isRefreshing(): boolean {
    return this.refreshRequest !== undefined;
  }

  getRefreshRequest(): Observable<string> {
    if (this.refreshRequest === undefined) {
      return throwError(() => new Error("No running refresh request"))
    }
    return this.refreshRequest;
  }

  setRefreshRequest(req: Observable<string>): void {
    this.refreshRequest = req;
    this.refreshRequest.pipe(
      tap(() => {
        this.refreshRequest = undefined
      })
    )
  }

  getToken(): Observable<string> {
    const token = this.localStorageService.get("token")
    switch(token.result) {
      case "found":
        return of(<string>token.item)
      default:
        return throwError(() => new Error("Unable to find token in local storage"))
    }
  }

  getRefreshToken(): Observable<string> {
    const token = this.localStorageService.get("refresh-token")
    switch(token.result) {
      case "found":
        return of(<string>token.item)
      default:
        return throwError(() => new Error("Unable to find refresh token in local storage"))
    }
  }

  refreshToken(): Observable<string> {
    return this.getRefreshToken()
    .pipe(
      switchMap((refreshToken) => {
        return this.http.post<{
          accessToken: string
        }>(
          this.baseUrl + '/refreshtoken',
          {
            refreshToken: refreshToken
          },
          httpOptions
        ).pipe(
          map((result) => {
            // save access token
            this.localStorageService.set('token', result.accessToken)
            return result.accessToken
          })
        )
      })
    )
  }

  getUserInfo(): Observable<UserInfo> {
    return this.getToken()
    .pipe(
      switchMap((token) => {
        return this.http.get<UserInfo>(
          this.baseUrl + '/userinfo',
          httpOptions
        )
        .pipe(
          retry(2),
          catchError((error: HttpErrorResponse) => {
            if (error.status === 0) {
              console.warn('> JwtAuthenticationService#userInfo() >> an error occured on http request:', error.error)
            } else {
              console.warn('> JwtAuthenticationService#userInfo() >> server returned code %d with body:', error.status, error.error)
            }
            const err = new Error('An error occured while verifying token')
            return throwError(() => err)
          })
        )
      })
    )
  }

}
