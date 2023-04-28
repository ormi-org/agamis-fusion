import { BehaviorSubject, Observable } from 'rxjs';
import { DataSource } from '../typed/data-source.interface';

interface Dummy {
  first: number;
  second: string;
  third: string;
  fourth: string;
}

export const DUMMIES: Dummy[] = [
  {
    first: 0,
    second: 'The row 0',
    third: 'A dummy for row 0',
    fourth: 'This is a test',
  },
  {
    first: 1,
    second: 'The row 1',
    third: 'A dummy for row 1',
    fourth: 'This is a test',
  },
];

export class DummyDatasource implements DataSource<Dummy> {
  private dummiesSubject = new BehaviorSubject<Dummy[]>(DUMMIES);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public $loading: Observable<boolean> = this.loadingSubject.asObservable();

  connect(): Observable<Dummy[]> {
    return this.dummiesSubject.asObservable();
  }
  disconnect(): void {
    // this.dummiesSubject.complete();
    // this.loadingSubject.complete();
  }
}
