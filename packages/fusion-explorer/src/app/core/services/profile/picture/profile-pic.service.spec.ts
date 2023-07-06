import { TestBed } from '@angular/core/testing';

import { ProfilePicService } from './profile-pic.service';

describe('ProfilePicService', () => {
  let service: ProfilePicService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProfilePicService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
