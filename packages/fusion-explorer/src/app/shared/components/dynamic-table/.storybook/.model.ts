import { BehaviorSubject, Observable, startWith } from 'rxjs';
import { DataSource } from '../typed/data-source/data-source.interface';

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

export class DummyDatasource<T> implements DataSource<T> {
  load(): void {
    this.dummiesSubject.next(this.dummies);
  }
  private dummies: T[] = [];
  private dummiesSubject = new BehaviorSubject<T[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public $loading: Observable<boolean> = this.loadingSubject.asObservable();

  // static factory
  static asSourceOf<T>(sourceData: T[]): DummyDatasource<T> {
    let ds = new DummyDatasource<T>();
    ds.dummies = sourceData;
    return ds;
  }

  public connect(): Observable<T[]> {
    return this.dummiesSubject.asObservable();
  }

  public disconnect(): void {
    // storybook need to keep subjects in case of reload
    // this.dummiesSubject.complete();
    // this.loadingSubject.complete();
  }
}
