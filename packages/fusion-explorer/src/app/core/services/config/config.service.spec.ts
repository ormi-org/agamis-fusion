import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CoreModule } from '@core/core.module';
import { AppConfig } from '@core/models/app-config.model';
import { AppState } from '@core/models/states/app-state.model';
import { loadConfig } from '@core/states/app-state/app-state.actions';
import { selectAppConfig } from '@core/states/app-state/app-state.selectors';
import { select, Store, StoreModule } from '@ngrx/store';
import { filter } from 'rxjs';
import { ConfigService } from './config.service';

const DEFAULT_ENDPOINTS = {
  AUTH: '/auth',
  ORGANIZATIONS: '',
  PROFILES: '',
  USERS: '',
}

describe('ConfigService', () => {
  let service: ConfigService;
  let store: Store<AppState>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
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
            ...DEFAULT_ENDPOINTS,
            AN_ENDPOINT: "/endpoint"
          }
        }
      }
    }
    service.load().subscribe(() => {
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
            ...DEFAULT_ENDPOINTS,
            A_DEFAULT_ENDPOINT: "/default"
          }
        }
      }
    }
    service.load().subscribe(() => {
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
