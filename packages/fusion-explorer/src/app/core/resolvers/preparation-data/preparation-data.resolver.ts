import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ResolveFn, Router } from '@angular/router';
import { Organization } from '@core/models/data/organization.model';
import { LocalStorageSuccess } from '@core/models/local-storage/local-storage-result.model';
import { JwtAuthenticationService } from '@core/services/authentication/jwt/jwt-authentication.service';
import { ConfigService } from '@core/services/config/config.service';
import { OrganizationService } from '@core/services/organization/organization.service';
import { LocalStorageService } from '@core/services/utils/local-storage/local-storage.service';
import { SplashService } from '@core/services/utils/splash/splash.service';
import { loadConfig, loadUserInfo } from '@core/states/app-state/app-state.actions';
import { setOrganization } from '@explorer/states/explorer-state/explorer-state.actions';
import { Store } from '@ngrx/store';
import { catchError, throwError, tap, zip, of, Subscription } from 'rxjs';

const LS_ORG_ID_KEY = 'app.native.fusion.explorer.orgid';

export const preparationDataResolver: ResolveFn<Subscription | undefined> = (route) => {
  // injections
  const router = inject(Router);
  const store = inject(Store);
  const splashService = inject(SplashService);
  const jwtAuthService = inject(JwtAuthenticationService);
  const organizationService = inject(OrganizationService);
  const localStorageService = inject(LocalStorageService);
  const configService = inject(ConfigService);
  // 0. check if orgId is provided (or contained in localStorage); otherwise redirect to bad request
  const orgIdFromQueryParams = route.queryParamMap.get('orgId');
  const orgIdResultFromLocalStorage = localStorageService.get<string>(LS_ORG_ID_KEY);
  if (orgIdFromQueryParams === null && orgIdResultFromLocalStorage.result === 'error') {
    const msg = 'could not get organization id';
    console.error('>> ExplorerComponent#getOrgIdFromEitherQueryParamOrLocalStorage >', msg);
    router.navigateByUrl('/error', { state: { code: 400, title: 'bad request', text: $localize`:@@client.messages.errors.input.no-organization-id:no organization identifier provided` } })
    return;
  }
  const orgId: string = orgIdFromQueryParams || (<LocalStorageSuccess<string>>orgIdResultFromLocalStorage).item;
  // 1. Loading App Global Configuration configuration
  splashService.reset();
  return configService.load()
    .pipe(
      catchError((err) => {
        // nav to error page
        router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } );
        splashService.complete();
        return throwError(() => new Error(err));
      }),
      tap((fetchedConfig) => {
        store.dispatch(loadConfig({ config: fetchedConfig }));
      })
    )
    .subscribe(() => {
      zip(
        // 2. Authentication + Load profile data
        jwtAuthService.getUserInfo()
        .pipe(
          tap((userInfo) => {
            store.dispatch(loadUserInfo({ userInfo: userInfo }))
          })
        ),
        // 3. Load organization data
        organizationService.getOrganizationById(orgId)
        .pipe(
          tap((org: Organization) => {
            store.dispatch(setOrganization({ organization: org }));
            localStorageService.set(LS_ORG_ID_KEY, org.id);
          }),
          catchError((err: HttpErrorResponse) => {
            if (err.status === 404) {
              router.navigateByUrl('/error', { state: { code: 404, title: 'not found', text: $localize`:@@client.messages.errors.data.organization.not-found:organization was not found` } });
              splashService.complete();
              return of(undefined);
            }
            return throwError(() => err);
          })
        )
        // ...
      )
      .pipe(
        catchError((err) => {
          router.navigateByUrl('/error', { state: { title: 'fatal error', text: $localize`:@@client.messages.errors.generics.unrecoverable:an irrecoverable error occured` } } );
          splashService.complete();
          return throwError(() => new Error(err))
        })
      )
      .subscribe(() => {
        splashService.next();
        route.data = {
          orgId: orgId
        };
        // timeout the splash disapearance for smoothing loading bar animation
        setTimeout(() => {
          splashService.complete();
        }, 600);
      })
    });
};
