import { Ordering } from '@shared/constants/utils/ordering';
import { BehaviorSubject, Observable, startWith } from 'rxjs';
import DataSource from '../typed/data-source/data-source.interface';
import Filtering from '../typed/data-source/typed/filtering.interface';
import Sorting from '../typed/data-source/typed/sorting.interface';

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
    first: 10,
    second: 'The row 10',
    third: 'A dummy for row 10',
    fourth: 'This is a test',
  },
  {
    first: 2,
    second: 'The row 2',
    third: 'A dummy for row 2',
    fourth: 'This is a test',
  },
  {
    first: 3,
    second: 'The row 3',
    third: 'A dummy for row 3',
    fourth: 'This is a test',
  },
  {
    first: 4,
    second: 'The row 4',
    third: 'A dummy for row 4',
    fourth: 'This is a test',
  },
];

export class DummyDatasource implements DataSource<Dummy> {
  private dummies: Dummy[] = [];
  private dummiesSubject = new BehaviorSubject<Dummy[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public $loading: Observable<boolean> = this.loadingSubject.asObservable();

  // static factory
  static asSourceOf(sourceData: Dummy[]): DummyDatasource {
    let ds = new DummyDatasource();
    ds.dummies = sourceData;
    ds.load(
      [],
      {
        direction: Ordering.DESC,
        field: ''
      },
      1,
      5
    );
    return ds;
  }

  public connect(): Observable<Dummy[]> {
    return this.dummiesSubject.asObservable();
  }

  load(filters: Filtering[], sorting: Sorting, pageIndex: number, pageSize: number): void {
    // A dummy way to simulate loading from backend
    const seen: Set<number> = new Set();
    const filtered = 
      filters.length > 0 ?
      filters.reduce((acc, f) => {
        let filtered: Dummy[] = 
          this.dummies.filter((d) => {
            return d[f.field].toString().includes(f.value);
          });
        filtered.forEach((value) => {
          acc.push(value);
          seen.add(value.first);
        });
        return acc;
      }, <Dummy[]>[])
      :
      this.dummies;
    this.dummiesSubject.next(
      filtered.sort((a, b) => {
        if (a[sorting.field] > b[sorting.field]) {
          return 1 * sorting.direction;
        }
        if (a[sorting.field] < b[sorting.field]) {
          return -1 * sorting.direction;
        }
        return 0;
      })
    );
  }

  public disconnect(): void {
    // storybook need to keep subjects in case of reload
    // this.dummiesSubject.complete();
    // this.loadingSubject.complete();
  }
}
