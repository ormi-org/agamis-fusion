import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { preFetchDataGuard } from './pre-fetch-data.guard';

describe('preFetchDataGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => preFetchDataGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
