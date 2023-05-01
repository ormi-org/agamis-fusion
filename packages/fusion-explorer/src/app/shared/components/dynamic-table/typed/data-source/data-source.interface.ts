import { Observable } from "rxjs";
import Sorting from "./typed/sorting.interface";
import Filtering from "./typed/filtering.interface";

export default interface DataSource<T> {
    connect(): Observable<T[]>;
    disconnect(): void;
    load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void;
    $loading: Observable<boolean>;
}