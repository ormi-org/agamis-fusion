import { Injectable } from '@angular/core'
import { LocalStorageResult } from '@core/models/local-storage/local-storage-result.model'

@Injectable()
export class LocalStorageService {

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  set(localStorageKey: string, value: any): void {
    localStorage.setItem(localStorageKey, JSON.stringify(value))
  }

  get<T>(localStorageKey: string): LocalStorageResult<T> {
    return ((): LocalStorageResult<T> => {
      const res = localStorage.getItem(localStorageKey)
      if (res === null) {
        return {
          result: 'error',
          error: `could not find item with key:#${localStorageKey}`,
        }
      }
      let result;
      try {
        result = JSON.parse(res);
      } catch (error) {
        result = '' + res;
      }
      return {
        result: 'found',
        item: result,
      }
    })()
  }
}
