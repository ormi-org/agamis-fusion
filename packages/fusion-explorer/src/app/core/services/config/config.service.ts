import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { environment } from '@environments/environment';
import { Observable, catchError, of, retry, throwError } from 'rxjs';
import { AppConfig } from '../../models/app-config.model';

@Injectable({
  providedIn: CoreModule,
})
export class ConfigService {

  constructor(
    private http: HttpClient
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
      retry(2),
      catchError((error) => {
        if (error.status === 0) {
          console.warn('> ConfigService#load(AppConfig) >> an error occured on http request:', error.error);
        } else {
          console.warn('> ConfigService#load(AppConfig) >> server returned code %d with body:', error.status, error.error)
        }
        // load default config, if provided
        if (defaults !== undefined) {
          return of(defaults);
        }
        const err = new Error('An error occured while loading application local configuration');
        return throwError(() => err)
      })
    )
  }
}
