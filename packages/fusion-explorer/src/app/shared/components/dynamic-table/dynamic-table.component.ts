import {
  AfterContentInit,
  AfterViewInit,
  Component,
  ContentChildren, ElementRef, Input, OnDestroy, OnInit, QueryList, Renderer2, ViewChildren
} from '@angular/core';
import { Ordering } from '@shared/constants/utils/ordering';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { HeadCellComponent } from './head-cell/head-cell.component';
import { ColumnDirective } from './meta/column/column.directive';
import { Column } from './models/column.model';
import { Row } from './models/row.model';
import { RowComponent } from './row/row.component';
import DataSource from './typed/data-source/data-source.interface';
import Filtering from './typed/data-source/typed/filtering.interface';
import Sorting from './typed/data-source/typed/sorting.interface';
import { Uniquely } from './typed/uniquely.interface';

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss'],
})
export class DynamicTableComponent<T extends Uniquely>
  implements OnInit, OnDestroy, AfterViewInit, AfterContentInit
{
  private sortEvent: Subject<Sorting> = new Subject();

  @Input()
  datasource!: DataSource<T>;
  @Input()
  emptyHint: string = $localize`:@@ui.classic.shared.dynamic-table.empty-hint.default:No data`;
  @Input()
  filters: Filtering[] = [];
  @Input()
  minWidth!: number;

  private width!: number;

  protected columns: Array<Column> = [];
  protected getColumnsWidthsAsync: () => [string, BehaviorSubject<number>][] =
    () => this.columns.map((col) => [col.key, col.widthSubject]);
  protected rows: Array<Row<T>> = [];

  private selectedEntitySubject: Subject<T> = new Subject();

  @ContentChildren(ColumnDirective)
  private columnsDef!: QueryList<ColumnDirective>;

  @ViewChildren(HeadCellComponent)
  private headCellsElements!: QueryList<HeadCellComponent>;

  @ViewChildren(RowComponent)
  private rowElements!: QueryList<RowComponent<T>>;

  constructor(
    private host: ElementRef
  ) {}

  ngOnInit(): void {
    // Set initial width
    this.width = Math.max(this.host.nativeElement.offsetWidth, this.minWidth);
  }

  ngAfterContentInit(): void {
    const colDefs = this.columnsDef.toArray();
    const orderedCol = colDefs.find((c) => {
      return c.getOrder() !== Ordering.NONE;
    });
    if (orderedCol === undefined) {
      // Throw an error if no column is ordered
      let msg = `Table cannot instiate without at least one ordered column`;
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
          colDefs.map((colcomp) => colcomp.getKey()),
          v
        );
      });
    });
  }

  ngAfterViewInit(): void {
    // init elements reactivity and init again on element changes
    this.initHeadCellsReactivity();
    this.initRowsReactivity();
    this.headCellsElements.changes.subscribe(_ => {
      this.initHeadCellsReactivity();
    });
    this.rowElements.changes.subscribe(_ => {
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
          this.selectedEntitySubject.next(updatedValue[1]);
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
        // if new width is not below set minimum
        if (this.width + updatedValue[2] >= this.minWidth) {
          // update corresponding column width
          this.columns
          .find(_ => _.key === updatedValue[0])
          ?.widthSubject.next(updatedValue[1]);
        }
      })
    });
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }

  public getSelectEvent(): Subject<T> {
    return this.selectedEntitySubject;
  }
}
