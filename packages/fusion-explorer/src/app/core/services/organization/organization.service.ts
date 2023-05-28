import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Organization } from '@core/models/data/organization.model';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
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
  }
}
