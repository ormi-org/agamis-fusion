import {
  AfterContentInit,
  AfterViewInit,
  Component,
  ContentChildren,
  Input,
  OnDestroy,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { Column } from './models/column.model';
import { Row } from './models/row.model';
import DataSource from './typed/data-source/data-source.interface';
import { Uniquely } from './typed/uniquely.interface';
import { HeadCellComponent } from './head-cell/head-cell.component';
import { Ordering } from '@shared/constants/utils/ordering';
import { ColumnDirective } from './meta/column/column.directive';
import Filtering from './typed/data-source/typed/filtering.interface';

const DEFAULT_PAGING_SIZE = 10;

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss'],
})
export class DynamicTableComponent<T extends Uniquely>
  implements OnDestroy, AfterViewInit, AfterContentInit
{
  @Input()
  private datasource!: DataSource<T>;
  @Input()
  protected emptyHint: string = 'No data';
  @Input()
  private filters: Filtering[] = [];

  protected columns: Array<Column> = [];
  protected rows: Array<Row<T>> = [];

  @ContentChildren(ColumnDirective)
  private columnsDef!: QueryList<ColumnDirective>;

  @ViewChildren(HeadCellComponent)
  private headCellsElements!: QueryList<HeadCellComponent>;

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
    this.columns = colDefs.map((c) => {
      return new Column(
        c.getKey(),
        c.getTitle(),
        c.isResizable(),
        c.getOrder()
      );
    });
    // Cols are defined now push datasource
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
    this.datasource.load(
      this.filters,
      {
        field: orderedCol.getKey(),
        direction: <Ordering.ASC | Ordering.DESC>orderedCol.getOrder(),
      },
      1,
      DEFAULT_PAGING_SIZE
    );
  }

  ngAfterViewInit(): void {
    this.headCellsElements.toArray().forEach((headCell) => {
      headCell.getOrdering().subscribe((updatedValue) => {
        if (updatedValue !== Ordering.NONE) {
          // If new value is different from NONE
          // Then broadcast a clearOrdering on other headCells
          this.headCellsElements
            .toArray()
            .filter((_) => _ !== headCell)
            .forEach((_) => _.clearOrdering());
          // Load with new order
          // TODO: add filtering save
          this.datasource.load(
            this.filters,
            {
              field: headCell.associatedColumn.key,
              direction: <Ordering.ASC | Ordering.DESC>(
                headCell.associatedColumn.ordering
              ),
            },
            1,
            DEFAULT_PAGING_SIZE
          );
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }

  public setEmptyHint(emptyHint: string) {
    this.emptyHint = emptyHint;
  }

  public setFilters(filters: Filtering[]) {
    this.filters = filters;
  }
}
