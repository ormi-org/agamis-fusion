import {
  AfterContentInit,
  AfterViewInit,
  Component,
  ContentChildren,
  Input,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { Column } from './models/column.model';
import { Row } from './models/row.model';
import { DataSource } from './typed/data-source/data-source.interface';
import { Uniquely } from './typed/uniquely.interface';
import { HeadCellComponent } from './head-cell/head-cell.component';
import { Ordering } from '@shared/constants/utils/ordering';
import { ColumnComponent } from './meta/column/column/column.component';

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss'],
})
export class DynamicTableComponent<T extends Uniquely>
  implements OnInit, OnDestroy, AfterViewInit, AfterContentInit
{
  @Input()
  datasource!: DataSource<T>;
  // TODO : make a dummy component for column provisioning
  protected columns: Array<Column> = [];
  protected rows: Array<Row<T>> = [];

  @ContentChildren(ColumnComponent)
  private columnsDef!: QueryList<ColumnComponent>;

  @ViewChildren(HeadCellComponent)
  private headCellsElements!: QueryList<HeadCellComponent>;

  ngOnInit(): void {
    this.datasource.connect().subscribe((updatedVal) => {
      console.log(updatedVal);
      this.rows = updatedVal.map((v, i) => {
        return new Row(v.uqId, i, v);
      });
    });
    this.datasource.load();
  }

  ngAfterContentInit(): void {
    var colDefs = this.columnsDef.toArray();
    var orderedCols = colDefs.filter(c => c.getOrder() != Ordering.NONE);
    if (orderedCols.length > 1) {
      // Throw an error if more than one column is ordered
      let msg = `Table cannot instiate more than one ordered column : {
        ${orderedCols.map((c) => c.getKey()).join(", ")}
      }`;
      throw new Error(msg);
    }
    // Push column definitions from angular content
    this.columns = colDefs.map((c) => {
      return new Column(c.getKey(), c.getTitle(), c.isResizable(), c.getOrder());
    });
  }
  
  ngAfterViewInit(): void {
    this.headCellsElements.toArray().forEach((headCell) => {
      headCell.getOrdering().subscribe((updatedValue) => {
        if (updatedValue !== Ordering.NONE) {
          // If new value is different from NONE
          // Then broadcast a clearOrdering on other headCells
          this.headCellsElements.toArray()
          .filter(_ => _ !== headCell)
          .forEach(_ => _.clearOrdering())
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }
}
