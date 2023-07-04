import {
  AfterContentInit,
  AfterViewInit,
  Component,
  ContentChildren, ElementRef, Input, OnDestroy, QueryList, ViewChild, ViewChildren
} from '@angular/core';
import { Ordering } from '@shared/constants/utils/ordering';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { HeadCellComponent } from './head-cell/head-cell.component';
import { ColumnDirective } from './meta/column/column.directive';
import { Column } from './models/column.model';
import { Row } from './models/row.model';
import { RowComponent } from './row/row.component';
import DataSource from './typed/data-source/data-source.interface';
import Sorting from './typed/data-source/typed/sorting.interface';
import { Uniquely } from './typed/uniquely.interface';

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss'],
})
export class DynamicTableComponent<T extends Uniquely>
  implements OnDestroy, AfterViewInit, AfterContentInit
{
  private sortEvent: Subject<Sorting> = new Subject();
  private selectedEntityEvent: Subject<T> = new Subject();
  private bodyScrollEvent: Subject<void> = new Subject();

  @Input()
  datasource!: DataSource<T>;
  @Input()
  emptyHint: string = $localize`:@@ui.classic.shared.dynamic-table.empty-hint.default:No data`;

  protected columns: Array<Column> = [];
  protected getColumnsWidthsAsync: () => [string, BehaviorSubject<number>][] =
    () => this.columns.map((col) => [col.key, col.widthSubject]);
  protected rows: Array<Row<T>> = [];

  @ViewChild('tableBody', { read: ElementRef })
  private tableBody!: ElementRef;

  @ContentChildren(ColumnDirective)
  private columnsDef!: QueryList<ColumnDirective>;

  @ViewChildren(HeadCellComponent)
  private headCellsElements!: QueryList<HeadCellComponent>;

  @ViewChildren(RowComponent)
  private rowElements!: QueryList<RowComponent<T>>;

  ngAfterContentInit(): void {
    const colDefs = this.columnsDef.toArray();
    const orderedCol = colDefs.find((c) => {
      return c.getOrder() !== Ordering.NONE;
    });
    if (orderedCol === undefined) {
      // Throw an error if no column is ordered
      const msg = `Table cannot instiate without at least one ordered column`;
      throw new Error(msg);
    }
    // Push column definitions from angular html materialized content
    this.columns = colDefs.map(c => {
      return new Column(
        c.getKey(),
        c.getTitle(),
        c.isResizable(),
        c.getOrder(),
        new BehaviorSubject(c.getWidth())
      )
    });
    // Cols are defined; now push datasource
    this.datasource.connect().subscribe((updatedVal) => {
      this.rows = updatedVal.map((v, i) => {
        return new Row(
          v.uqId,
          i,
          colDefs.map((col) => ({ key: col.getKey(), compute: col.compute, template: col.template })),
          v
        );
      });
    });
  }

  ngAfterViewInit(): void {
    // reset scroll to 0 when datasource values are reset
    this.datasource.getResetEvent().subscribe(() => {
      (<HTMLElement>this.tableBody.nativeElement).scrollTo(0, 0);
    });
    // init elements reactivity and init again on element changes
    this.initHeadCellsReactivity();
    this.initRowsReactivity();
    this.headCellsElements.changes.subscribe(() => {
      this.initHeadCellsReactivity();
    });
    this.rowElements.changes.subscribe(() => {
      this.initRowsReactivity();
    });
  }

  private initRowsReactivity(): void {
    this.rowElements.toArray().forEach((row) => {
      // handling row selection
      row.isSelected().subscribe((updatedValue) => {
        if (updatedValue[0] === true) {
          this.rowElements
          .toArray()
          .filter(_ => _ !== row)
          .forEach(_ => _.clearSelect());
          this.selectedEntityEvent.next(updatedValue[1]);
        }
      });
    });
  }

  private initHeadCellsReactivity(): void {
    this.headCellsElements.toArray().forEach((headCell) => {
      // Handling ordering event firing
      headCell.getOrdering().subscribe((updatedValue) => {
          // If new value is different from NONE
          // Then broadcast a clearOrdering on other headCells
        this.headCellsElements
          .toArray()
          .filter((_) => _ !== headCell)
          .forEach((_) => _.clearOrdering());
        // fire sorting action event
        this.sortEvent.next({
          direction: updatedValue,
          field: headCell.associatedColumn.key
        });
      });
      // Handling resizing event firing
      headCell.getResizing().subscribe((updatedValue) => {
        // update corresponding column width
        this.columns
        .find(_ => _.key === updatedValue[0])
        ?.widthSubject.next(updatedValue[1]);
      })
    });
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }

  protected onBodyScroll(event: WheelEvent): void {
    const element = event.currentTarget as HTMLElement;
    if ((element.offsetHeight + Math.round(element.scrollTop)) >= element.scrollHeight
      && event.deltaY > 0) {
      // trigger scroll only if at bottom and scrolling down
      this.bodyScrollEvent.next();
    }
  }

  public getSortEvent(): Observable<Sorting> {
    return this.sortEvent.asObservable();
  }

  public getSelectEvent(): Observable<T> {
    return this.selectedEntityEvent.asObservable();
  }

  public getBodyScrollEvent(): Observable<void> {
    return this.bodyScrollEvent.asObservable();
  }
}
