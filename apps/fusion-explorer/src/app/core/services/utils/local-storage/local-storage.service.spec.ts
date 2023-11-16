import { TestBed } from '@angular/core/testing';

import { CoreModule } from '@core/core.module';
import { StoreModule } from '@ngrx/store';
import { LocalStorageService } from './local-storage.service';

type Dummy = {
  field1: string,
  field2: number
}

const EXPECTED_RESULT: Dummy = {
  field1: 'test1',
  field2: 1
};

describe('LocalStorageService', () => {
  let service: LocalStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CoreModule, StoreModule.forRoot({})]
    });
    service = TestBed.inject(LocalStorageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get item when key is set AND object has correct format', () => {
    const mockFn = (key: string) => {
      if (key === 'test') {
        return JSON.stringify(EXPECTED_RESULT);
      } else {
        return null;
      }
    };
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(mockFn);
    const savedState = service.get<Dummy>('test');
    expect(savedState).toEqual({
      result: 'found',
      item: EXPECTED_RESULT
    });
  });

  it('should return error when key is not found', () => {
    const mockFn = () => {
      return null;
    };
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(mockFn);
    const savedState = service.get<Dummy>('test');
    expect(savedState).toEqual({
      result: 'error',
      error: 'could not find item with key:#test'
    });
  });
});
