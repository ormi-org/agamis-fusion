import { TestBed } from '@angular/core/testing';
import { ExplorerModule } from '@explorer/explorer.module';
import { StoreModule } from '@ngrx/store';

import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        ExplorerModule
      ]
    });
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
