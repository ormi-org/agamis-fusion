import DataSource from "@shared/components/dynamic-table/typed/data-source/data-source.interface";
import { User } from "@core/models/data/user.model";
import Filtering from "@shared/components/dynamic-table/typed/data-source/typed/filtering.interface";
import Sorting from "@shared/components/dynamic-table/typed/data-source/typed/sorting.interface";
import { BehaviorSubject, Observable } from "rxjs";
import { UserService } from "@core/services/user/user.service";

export class UserDatasource implements DataSource<User> {
    private userService: UserService;
    private users: User[] = [];
    private usersSubject = new BehaviorSubject<User[]>([]);
    private loadingSubject = new BehaviorSubject<boolean>(false);

    public $loading: Observable<boolean> = this.loadingSubject.asObservable();

    constructor(userService: UserService) {
        this.userService = userService;
    }

    connect(): Observable<User[]> {
        return this.usersSubject.asObservable();
    }
    disconnect(): void {
        this.usersSubject.complete();
        this.loadingSubject.complete();
    }
    load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void {
        throw new Error("> UserDatasource#load >> Method not implemented.");
    }
}