import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppConfig } from '../../models/app-config.model';
import { CoreModule } from '@core/core.module';
import { Store } from '@ngrx/store';
import { environment } from '@environments/environment';
import { loadConfig } from '@core/states/app-state/app-state.actions';
import { Observable, catchError, of, retry, tap, throwError } from 'rxjs';

@Injectable({
  providedIn: CoreModule,
})
export class ConfigService {

  constructor(
    private http: HttpClient,
    private readonly store: Store
  ) {}

  load(defaults?: AppConfig): Observable<AppConfig> {
    let env;
    if (environment.production === true) {
      env = 'prod';
    } else {
      env = 'dev';
    }
    return this.http.get<AppConfig>('./assets/config/' + env + '.conf.json')
    .pipe(
      retry(3),
      catchError((error) => {
        if (error.status === 0) {
          console.error('> ConfigService#load(AppConfig) >> an error occured on http request:', error.error);
        } else {
          console.error('> ConfigService#load(AppConfig) >> server returned code %d with body:', error.status, error.error)
        }
        // load default config, if provided
        const err = new Error('An error occured while loading application local configuration')
        if (defaults !== undefined) {
          return of(defaults);
        }
        return throwError(() => err)
      }),
      tap((fetchedConfig) => {
        this.store.dispatch(loadConfig({ config: fetchedConfig }));
      })
    )
  }
}
