import { TestBed } from '@angular/core/testing';

import { ConfigService } from './config.service';
import { CoreModule } from '@core/core.module';

describe('ConfigService', () => {
  let service: ConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        CoreModule
      ]
    });
    service = TestBed.inject(ConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
