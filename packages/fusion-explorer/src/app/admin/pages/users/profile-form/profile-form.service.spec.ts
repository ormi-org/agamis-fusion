import { TestBed } from '@angular/core/testing';

import { ProfileFormService } from './profile-form.service';
import { AdminModule } from '@admin/admin.module';

describe('ProfileFormService', () => {
  let service: ProfileFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AdminModule]
    });
    service = TestBed.inject(ProfileFormService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
