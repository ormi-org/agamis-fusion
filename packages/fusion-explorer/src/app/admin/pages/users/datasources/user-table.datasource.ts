import { Profile } from "@core/models/data/profile.model";
import { ProfileService } from "@core/services/profile/profile.service";
import { selectOrganization } from "@explorer/states/explorer-state/explorer-state.selectors";
import { Store } from "@ngrx/store";
import DataSource from "@shared/components/dynamic-table/typed/data-source/data-source.interface";
import Filtering from "@shared/components/dynamic-table/typed/data-source/typed/filtering.interface";
import Sorting from "@shared/components/dynamic-table/typed/data-source/typed/sorting.interface";
import { BehaviorSubject, Observable } from "rxjs";

export class UserTableDatasource implements DataSource<Profile> {
    private orgId: string | undefined;
    private profilesSubject = new BehaviorSubject<Profile[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);

    public $loading: Observable<boolean> = this.loadingSubject.asObservable();

    constructor(
        private readonly store: Store,
        private profileService: ProfileService
    ) {
        this.store.select(selectOrganization).subscribe((org) => {
            this.orgId = org?.id;
        });
    }

    connect(): Observable<Profile[]> {
        return this.profilesSubject.asObservable();
    }
    disconnect(): void {
        this.profilesSubject.complete();
        this.loadingSubject.complete();
    }
    load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void {
        if (this.orgId === undefined) {
            this.profilesSubject.next([]);
            return;
        }
        this.loadingSubject.next(true);
        this.profileService.fetchUserProfilesFromOrganization(this.orgId, {
            filters, sorting, pageIndex, pageSize
        }).subscribe((profiles) => {
            this.profilesSubject.next(profiles);
        })
    }
}