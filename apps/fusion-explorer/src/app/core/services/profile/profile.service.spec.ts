import { TestBed } from '@angular/core/testing';
import { CoreModule } from '@core/core.module';
import { StoreModule } from '@ngrx/store';

import { ProfileService } from './profile.service';

describe('ProfileService', () => {
  let service: ProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        CoreModule
      ]
    });
    service = TestBed.inject(ProfileService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
