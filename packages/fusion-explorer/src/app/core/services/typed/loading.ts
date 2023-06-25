import { Subject, BehaviorSubject, Observable } from "rxjs";

export abstract class Loading {
    private nextStageSignalSubject: Subject<void> = new Subject();
    protected loadingSubject: Subject<boolean> = new Subject();

    $loading: Observable<boolean> = this.loadingSubject.asObservable();

    public getNextStageObserver(): Subject<void> {
        return this.nextStageSignalSubject;
    }

    // Reset loading state at beginning
    public reset(): void {
        this.loadingSubject.next(true);
    }

    public next(): void {
        this.nextStageSignalSubject.next();
    }

    public complete(): void {
        this.loadingSubject.next(false);
    }
}