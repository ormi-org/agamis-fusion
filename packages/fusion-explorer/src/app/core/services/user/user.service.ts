import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store } from '@ngrx/store';

@Injectable({
  providedIn: CoreModule
})
export class UserService {

  private baseUrl!: string;

  constructor(private http: HttpClient, private readonly store: Store) {
    this.store.select(selectAppConfig).subscribe((conf) => {
      this.baseUrl = '/'+(conf?.urls.rest.endpoints.USERS);
    })
  }
}
