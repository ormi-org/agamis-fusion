import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CoreModule } from '@core/core.module';
import { AppState } from '@core/models/states/app-state.model';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { Store, StoreModule, combineReducers, select } from '@ngrx/store';
import { Observable, catchError, filter } from 'rxjs';
import { ConfigService } from './config.service';
import * as fromFeature from '@core/states/app-state/app-state.reducers';
import { loadConfig } from '@core/states/app-state/app-state.actions';
import { AppConfig } from '@core/models/app-config.model';

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
    const expected: AppConfig = {
      urls: {
        rest: {
          endpoints: {
            AN_ENDPOINT: "/endpoint"
          }
        }
      }
    }
    service.load().subscribe((_) => {
      const storeSpy = spyOn(store, 'dispatch');
      expect(storeSpy).toHaveBeenCalledWith(loadConfig({ config: expected }));

      store
      .pipe(
        select(selectAppConfig),
        filter(val => val !== undefined)
      )
      .subscribe((conf) => {
        expect(conf).toBe(expected);
      });
    });
    const mockRequest = httpMock.expectOne('./assets/config/dev.conf.json');
    mockRequest.flush(expected);
  });

  it('should load default conf to store when file is not found', () => {
    const expected: AppConfig = {
      urls: {
        rest: {
          endpoints: {
            A_DEFAULT_ENDPOINT: "/default"
          }
        }
      }
    }
    service.load().subscribe((_) => {
      store
      .pipe(
        select(selectAppConfig),
        filter(val => val !== undefined)
      )
      .subscribe((conf) => {
        expect(conf).toBe(expected)
      });
    });
    const mockRequest = httpMock.expectOne('./assets/config/dev.conf.json');
    mockRequest.flush({}, {
      status: 404,
      statusText: "Not Found"
    });
  });
});
