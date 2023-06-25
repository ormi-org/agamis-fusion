import { Observable } from "rxjs";
import LoadingQuery from "./typed/loading-query.interface";

export default interface DataSource<T> {
    /**
     * connect the observable dataset to an observer destination
     */
    connect(): Observable<T[]>;
    /**
     * destroy the subscription
     */
    disconnect(): void;
    /** 
     * load data
     * 
     * @param filters a filters specification
     * @param sorting a sorting order
     * @param pageIndex index of the data page to get
     * @param pageSize number of elements inside the page
     */
    load(query: LoadingQuery): void;
    /**
     * a loading observable to keep track of loading state of the source
     * (be careful to use a BehaviorSubject to emit current value on subscribe)
     */
    $loading: Observable<boolean>;
}