import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SplashService {

  private nextStageSignalSubject: Subject<void> = new Subject();
  private $loading: BehaviorSubject<boolean> = new BehaviorSubject(true);

  // constructor() {
  //   this.next();
  // }

  public getNextStageObserver(): Subject<void> {
    return this.nextStageSignalSubject;
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
