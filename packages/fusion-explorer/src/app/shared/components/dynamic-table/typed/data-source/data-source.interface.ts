import { Observable } from "rxjs";

export interface DataSource<T> {
    connect(): Observable<T[]>;
    disconnect(): void;
    load(): void;
    $loading: Observable<boolean>;
}