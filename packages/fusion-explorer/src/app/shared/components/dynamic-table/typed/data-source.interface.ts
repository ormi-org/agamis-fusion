import { Observable } from "rxjs";

export interface DataSource<T> {
    connect(): Observable<T[]>;
    disconnect(): void;
    $loading: Observable<boolean>;
}