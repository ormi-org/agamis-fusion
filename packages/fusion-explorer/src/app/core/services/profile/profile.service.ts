import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http'
import { Injectable } from '@angular/core'
import { CoreModule } from '@core/core.module'
import { Profile } from '@core/models/data/profile.model'
import { selectAppConfig } from '@core/states/app-state/app-state.selectors'
import { Store } from '@ngrx/store'
import { Observable, catchError, map, of, retry, throwError } from 'rxjs'
import { ProfileQuery } from './types/profile-query.model'

@Injectable({
  providedIn: CoreModule,
})
export class ProfileService {
  private baseUrl!: string
  private orgBaseUrl!: string

  constructor(
    private http: HttpClient,
    private readonly store: Store
  ) {
    this.store.select(selectAppConfig).subscribe((conf) => {
      this.baseUrl = '/' + conf?.urls.rest.endpoints.USERS
      this.orgBaseUrl = '/' + conf?.urls.rest.endpoints.ORGANIZATIONS
    })
  }

  /** Fetch profiles with their users from a specific organization
   *
   * @param orgId the parent organization
   * @param query the fetching query on profiles
   */
  fetchUserProfilesFromOrganization(
    orgId: string,
    query: ProfileQuery
  ): Observable<Profile[]> {
    if (this.orgBaseUrl === undefined) {
      console.warn(
        '> ProfileService#fetchUserProfilesFromOrganization(string, ProfileQuery) >> base url is not defined'
      )
      return of([])
    }
    return this.http
      .get<Profile[]>(`${this.orgBaseUrl}/${orgId}/profiles`, {
        params: new HttpParams().appendAll(
          query.filters.reduce(
            (acc, filter) => {
              return { ...acc, [filter.field]: filter.value }
            },
            {
              offset: query.offset,
              limit: query.limit,
              order_by: [query.sorting.field, query.sorting.direction].join(
                ','
              ),
              include: query.include.join(','),
            }
          )
        ),
      })
      .pipe(
        retry(2),
        map((data) => {
          return data.map((p) => {
            p.lastLogin = new Date(p.lastLogin)
            p.updatedAt = new Date(p.updatedAt)
            p.createdAt = new Date(p.createdAt)
            return p
          })
        }),
        catchError((err: HttpErrorResponse) => {
          if (err.status === 0) {
            console.warn(
              '< ProfileService#fetchUserProfilesFromOrganization(string, ProfileQuery) << an error occured on http request:',
              err.error
            )
          } else {
            console.warn(
              '< ProfileService#fetchUserProfilesFromOrganization(string, ProfileQuery) << server returned code %d with body:',
              err.status,
              err.error
            )
          }
          return throwError(() => new Error(err.message))
        })
      )
  }

  fetchProfileById(
    orgId: string,
    profileId: string
  ): Observable<Profile> {
    if (this.orgBaseUrl === undefined) {
      console.warn(
        '> ProfileService#fetchProfileById(string, string) >> base url is not defined'
      )
      return of()
    }
    return this.http
      .get<Profile>(`${this.orgBaseUrl}/${orgId}/profile/${profileId}`)
      .pipe(
        retry(2),
        map((p) => {
          p.lastLogin = new Date(p.lastLogin)
          p.updatedAt = new Date(p.updatedAt)
          p.createdAt = new Date(p.createdAt)
          return p
        }),
        catchError((err: HttpErrorResponse) => {
          if (err.status === 0) {
            console.warn(
              '< ProfileService#fetchProfileById(string, string) << an error occured on http request:',
              err.error
            )
          } else {
            console.warn(
              '< ProfileService#fetchProfileById(string, string) << server returned code %d with body:',
              err.status,
              err.error
            )
          }
          return throwError(() => err)
        })
      )
  }

  updateProfile(orgId: string, profile: Profile): Observable<Profile> {
    if (this.orgBaseUrl === undefined) {
      console.warn(
        '> ProfileService#updateProfile(string, Profile) >> base url is not defined'
      )
      return of()
    }
    return this.http
      .put<Profile>(`${this.orgBaseUrl}/${orgId}/profile/${profile.id}`, {
        alias: profile.alias,
        lastName: profile.lastName,
        firstName: profile.firstName,
        isActive: profile.isActive
      }).pipe(
        retry(2),
        map((p) => {
          p.lastLogin = new Date(p.lastLogin)
          p.updatedAt = new Date(p.updatedAt)
          p.createdAt = new Date(p.createdAt)
          return p
        }),
        catchError((err: HttpErrorResponse) => {
          if (err.status === 0) {
            console.warn(
              '< ProfileService#updateProfile(string, Profile) << an error occured on http request:',
              err.error
            )
          } else {
            console.warn(
              '< ProfileService#updateProfile(string, Profile) << server returned code %d with body:',
              err.status,
              err.error
            )
          }
          return throwError(() => err)
        })
      )
  }
}
