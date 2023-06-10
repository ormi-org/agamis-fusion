import { Observable } from "rxjs";
import Sorting from "./typed/sorting.interface";
import Filtering from "./typed/filtering.interface";

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
    load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void;
    /**
     * a loading observable to keep track of loading state of the source
     */
    $loading: Observable<boolean>;
}