import { Subject, BehaviorSubject, Observable } from "rxjs";

export abstract class Loading {
    private nextStageSignalSubject: Subject<void> = new Subject();
    private $loading: BehaviorSubject<boolean> = new BehaviorSubject(false);

    public getNextStageObserver(): Subject<void> {
        return this.nextStageSignalSubject;
    }

    public reset(): void {
        this.$loading.next(true);
    }

    public next(): void {
        this.nextStageSignalSubject.next();
    }

    public complete(): void {
        this.$loading.next(false);
    }

    public isLoading(): Observable<boolean> {
        return this.$loading.asObservable();
    }
}