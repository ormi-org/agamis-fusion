import { Profile } from "@core/models/data/profile.model"
import { ProfileService } from "@core/services/profile/profile.service"
import { IncludableProfileFields } from "@core/services/profile/types/profile-query.model"
import { selectOrganization } from "@explorer/states/explorer-state/explorer-state.selectors"
import { Store } from "@ngrx/store"
import DataSource from "@shared/components/dynamic-table/typed/data-source/data-source.interface"
import LoadingQuery from "@shared/components/dynamic-table/typed/data-source/typed/loading-query.interface"
import { BehaviorSubject, combineLatest, map, Observable, ReplaySubject, skipWhile, Subject, Subscription, take } from "rxjs"

export class UserTableDatasource implements DataSource<Profile> {
    private orgIdSub: ReplaySubject<string | undefined> = new ReplaySubject(1)
    private resetSubject = new Subject<void>()
    private profilesSubject = new BehaviorSubject<Profile[]>([])
    private loadingSubject = new ReplaySubject<boolean>(1)
    private unitaryRefresher?: Subscription

    $loading: Observable<boolean> = this.loadingSubject.asObservable()

    constructor(
        private readonly store: Store,
        private profileService: ProfileService
    ) {
        // attach subject to observable orgId
        this.store
            .select(selectOrganization)
            .pipe(
                skipWhile(_ => !_),
                map((org) => {
                    return org?.id
                })
            )
            .subscribe(this.orgIdSub)
    }

    connect(): Observable<Profile[]> {
        return this.profilesSubject.asObservable()
    }
    
    disconnect(): void {
        this.profilesSubject.complete()
        this.loadingSubject.complete()
    }

    load(query: LoadingQuery, stack: boolean): void {
        // wait for orgId to be populated from store
        this.orgIdSub.subscribe(orgId => {
            if (orgId === undefined) {
                // return empty set if orgId is not defined
                this.profilesSubject.next([])
                return
            }
            // extract query elements
            const {
                filters, sorting, pageIndex, pageSize
            } = query
            // put subject into loading state
            this.loadingSubject.next(true)
            const fetchProfileSub = this.profileService.fetchUserProfilesFromOrganization(orgId, {
                filters,
                include: [IncludableProfileFields.USER],
                sorting, 
                offset: (pageIndex - 1) * pageSize,
                limit: pageIndex * pageSize
            })

            const errorCallback = (err: Error) => {
                console.error(err.message)
                this.loadingSubject.next(false)
                // TODO: display error message
            }
            // if stack, stack up new result with old
            if (stack) {
                combineLatest([
                    this.profilesSubject,
                    fetchProfileSub
                ])
                .pipe(
                    // Prevents repeat on profilesSubject
                    take(1)
                )
                .subscribe({
                    next: ([current, fetched]) => {
                        this.loadingSubject.next(false)
                        this.profilesSubject.next(current.concat(fetched))
                    },
                    error: errorCallback
                })
            // else reset and replace current values
            } else {
                // reset
                this.resetSubject.next()
                fetchProfileSub.subscribe({
                    next: (profiles) => {
                        this.loadingSubject.next(false)
                        this.profilesSubject.next(profiles)
                    },
                    error: errorCallback
                })
            }
        })
    }

    getResetEvent(): Observable<void> {
        return this.resetSubject.asObservable()
    }

    bindUnitaryRefresher(observable: Observable<Profile>): void {
        this.unitaryRefresher?.unsubscribe()
        this.unitaryRefresher = 
            observable.subscribe((update) => {
                const currentProfiles = this.profilesSubject.getValue()
                const indexToUpdate = currentProfiles.findIndex((p) => p.id === update.id)
                const finalProfiles = currentProfiles
                finalProfiles[indexToUpdate] = update
                this.profilesSubject.next(finalProfiles)
            })
    }
}