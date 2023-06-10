import { Injectable } from '@angular/core';
import { CoreModule } from '@core/core.module';
import { LocalStorageResult } from '@core/models/local-storage/local-storage-result.model';

@Injectable({
  providedIn: CoreModule,
})
export class LocalStorageService {
  constructor() {}

  set(localStorageKey: string, value: Object): void {
    localStorage.setItem(localStorageKey, JSON.stringify(value));
  }

  get<T extends Object>(localStorageKey: string): LocalStorageResult<T> {
    return ((): LocalStorageResult<T> => {
      const res = localStorage.getItem(localStorageKey);
      if (res === null) {
        return {
          result: 'error',
          error: `could not find item with key:#${localStorageKey}`,
        };
      }
      return {
        result: 'found',
        item: JSON.parse(res),
      };
    })();
  }
}
