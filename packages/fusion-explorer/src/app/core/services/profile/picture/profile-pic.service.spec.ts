import { TestBed } from '@angular/core/testing';

import { ProfilePicService } from './profile-pic.service';
import { CoreModule } from '@core/core.module';
import { StoreModule } from '@ngrx/store';

describe('ProfilePicService', () => {
  let service: ProfilePicService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CoreModule, StoreModule.forRoot({})]
    });
    service = TestBed.inject(ProfilePicService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
