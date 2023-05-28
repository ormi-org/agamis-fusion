import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { Organization } from '@core/models/data/organization.model';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store } from '@ngrx/store';
import { Observable, catchError, retry, throwError } from 'rxjs';

@Injectable({
  providedIn: CoreModule
})
export class OrganizationService {

  private baseUrl!: string;

  constructor(
    private http: HttpClient,
    private readonly store: Store
  ) {
    this.store.select(selectAppConfig).subscribe((conf) => {
      this.baseUrl = '/'+(conf?.urls.rest.endpoints.ORGANIZATIONS);
    })
  }

  getOrganizationById(id: string): Observable<Organization> {
    return this.http.get<Organization>(this.baseUrl + '/' + id)
    .pipe(
      retry(2),
      catchError((err: HttpErrorResponse) => {
        if (err.status === 0) {
          console.warn('> OrganizationService#getOrganizationById(string) >> an error occured on http request:', err.error);
        } else {
          console.warn('> OrganizationService#getOrganizationById(string) >> server returned code %d with body:', err.status, err.error);
        }
        return throwError(() => err);
      })
    )
  }
}
