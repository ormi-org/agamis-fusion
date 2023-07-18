import { TestBed } from '@angular/core/testing';
import { CoreModule } from '@core/core.module';
import { StoreModule } from '@ngrx/store';

import { SplashService } from './splash.service';

describe('SplashService', () => {
  let service: SplashService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        CoreModule,
        StoreModule.forRoot({})
      ]
    });
    service = TestBed.inject(SplashService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
