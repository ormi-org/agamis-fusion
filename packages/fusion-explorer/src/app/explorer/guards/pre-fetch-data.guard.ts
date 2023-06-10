import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn } from '@angular/router';
import { selectOrganization } from '@explorer/states/explorer-state/explorer-state.selectors';
import { select, Store } from '@ngrx/store';
import { of, skipWhile, switchMap, take } from 'rxjs';

export const preFetchDataGuard: CanActivateChildFn = (route, state) => {
  var store = inject(Store);
  return store.pipe(
    select(selectOrganization),
    skipWhile((org) => org?.id === undefined),
    take(1),
    switchMap(_ => {
      return of(true);
    })
  );
};
