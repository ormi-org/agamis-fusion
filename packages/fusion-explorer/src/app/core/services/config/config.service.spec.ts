import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CoreModule } from '@core/core.module';
import { AppState } from '@core/models/states/app-state.model';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store, StoreModule, combineReducers, select } from '@ngrx/store';
import { filter } from 'rxjs';
import { ConfigService } from './config.service';
import * as fromFeature from '@core/states/app-state/app-state.reducers';
import { loadConfig } from '@core/states/app-state/app-state.actions';

describe('ConfigService', () => {
  let service: ConfigService;
  let store: Store<AppState>;
  let httpMock: HttpTestingController;
  const initialState: AppState = {}

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({
          feature: combineReducers(fromFeature.appStateReducer)
        }),
        CoreModule,
        HttpClientTestingModule,
      ]
    });
    store = TestBed.inject(Store);
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(ConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load conf file to store when file is found', () => {
    service.load().then(() => {
      store
      .pipe(
        select(selectAppConfig),
        filter(val => val !== undefined)
      )
      .subscribe((conf) => {
        expect(conf).toBeTruthy();
      })
    }, (err) => {
      console.log(err);
    });
    const mockRequest = httpMock.expectOne('./assets/config/dev.conf.json');
    mockRequest.flush({});
    const dispatchSpy = jest.spyOn(store, 'dispatch');
    expect(dispatchSpy).toHaveBeenCalledWith(loadConfig);
  });
});
