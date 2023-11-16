import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CoreModule } from '@core/core.module';
import { JwtAuthenticationService } from './jwt-authentication.service';
import * as fromFeature from '@core/states/app-state/app-state.reducers';
import { StoreModule, combineReducers } from '@ngrx/store';

describe('JwtAuthenticationService', () => {
  let service: JwtAuthenticationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        CoreModule,
        StoreModule.forRoot({
          feature: combineReducers(fromFeature.appStateReducer)
        }),
      ]
    });
    service = TestBed.inject(JwtAuthenticationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
