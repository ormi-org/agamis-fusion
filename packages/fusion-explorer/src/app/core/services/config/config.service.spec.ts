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
  let httpTest: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        CoreModule,
        HttpClientTestingModule,
      ]
    });
    store = TestBed.inject(Store);
    httpTest = TestBed.inject(HttpTestingController);
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

      const mockRequest = httpTest.expectOne('/assets/config/dev.conf.json');
      expect(mockRequest.request.method).toEqual('GET');
      mockRequest.flush(expected);

      httpTest.verify();

      store
      .pipe(
        select(selectAppConfig),
        filter(val => val !== undefined)
      )
      .subscribe((conf) => {
        expect(conf).toBe(expected);
      });
    });
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
      const mockRequest = httpTest.expectOne('/assets/config/dev.conf.json');
      expect(mockRequest.request.method).toEqual('GET');
      mockRequest.flush({}, {
        status: 404,
        statusText: "Not Found"
      });

      httpTest.verify();

      store
      .pipe(
        select(selectAppConfig),
        filter(val => val !== undefined)
      )
      .subscribe((conf) => {
        expect(conf).toBe(expected)
      });
    });
  });
});
