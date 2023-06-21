import { Profile } from "@core/models/data/profile.model";
import { ProfileService } from "@core/services/profile/profile.service";
import { IncludableProfileFields } from "@core/services/profile/types/profile-query.model";
import { selectOrganization } from "@explorer/states/explorer-state/explorer-state.selectors";
import { Store } from "@ngrx/store";
import DataSource from "@shared/components/dynamic-table/typed/data-source/data-source.interface";
import Filtering from "@shared/components/dynamic-table/typed/data-source/typed/filtering.interface";
import Sorting from "@shared/components/dynamic-table/typed/data-source/typed/sorting.interface";
import { BehaviorSubject, map, Observable, ReplaySubject, skipWhile, Subject } from "rxjs";

export class UserTableDatasource implements DataSource<Profile> {
    private orgIdSub: ReplaySubject<string | undefined> = new ReplaySubject();
    private profilesSubject = new BehaviorSubject<Profile[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);

    public $loading: Observable<boolean> = this.loadingSubject.asObservable();

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
            .subscribe(this.orgIdSub);
    }

    connect(): Observable<Profile[]> {
        return this.profilesSubject.asObservable();
    }
    
    disconnect(): void {
        this.profilesSubject.complete();
        this.loadingSubject.complete();
    }

    load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void {
        // wait for orgId to be populated from store
        this.orgIdSub.subscribe(orgId => {
            if (orgId === undefined) {
                // return empty set if orgId is not defined
                this.profilesSubject.next([]);
                return;
            }
            this.loadingSubject.next(true);
            this.profileService.fetchUserProfilesFromOrganization(orgId, {
                filters,
                include: [IncludableProfileFields.USER],
                sorting, 
                offset: (pageIndex - 1) * pageSize,
                limit: pageIndex * pageSize
            }).subscribe((profiles) => {
                this.profilesSubject.next(profiles);
            });
        });
    }
}