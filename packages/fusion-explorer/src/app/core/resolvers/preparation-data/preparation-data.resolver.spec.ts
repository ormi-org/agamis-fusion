import { TestBed } from '@angular/core/testing';
import { ResolveFn } from '@angular/router';

import { preparationDataResolver } from './preparation-data.resolver';

describe('preparationDataResolver', () => {
  const executeResolver: ResolveFn<boolean> = (...resolverParameters) => 
      TestBed.runInInjectionContext(() => preparationDataResolver(...resolverParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeResolver).toBeTruthy();
  });
});
