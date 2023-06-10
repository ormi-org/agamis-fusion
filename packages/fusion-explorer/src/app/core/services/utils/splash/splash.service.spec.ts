import { TestBed } from '@angular/core/testing';

import { SplashService } from './splash.service';

describe('SplashService', () => {
  let service: SplashService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SplashService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
