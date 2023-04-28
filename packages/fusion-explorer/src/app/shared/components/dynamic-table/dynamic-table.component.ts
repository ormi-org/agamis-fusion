import {
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
import { DataSource } from './typed/data-source.interface';
import { Uniquely } from './typed/uniquely.interface';
import { HeadCellComponent } from './head-cell/head-cell.component';
import { HeadCellDefinition } from './typed/head-cell-definition.interface';
import { Ordering } from '@shared/constants/utils/ordering';

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss'],
})
export class DynamicTableComponent<T extends Uniquely>
  implements OnInit, OnDestroy, AfterViewInit
{
  @Input()
  datasource!: DataSource<T>;

  protected columns: Array<Column> = [];
  protected rows: Array<Row<T>> = [];

  @ViewChildren(HeadCellComponent)
  private headCellsElements!: QueryList<HeadCellComponent>;
  private headCells!: HeadCellDefinition[];

  ngOnInit(): void {
    this.datasource.connect().subscribe((updatedVal) => {
      this.rows = updatedVal.map((v, i) => {
        return new Row(v.uqId, i, v);
      });
    });
  }
  
  ngAfterViewInit(): void {
    this.headCellsElements.changes.subscribe((headCells) => {
      headCells.forEach((headCell) => {
        headCell.getOrdering().subscribe((updatedValue) => {
          if (updatedValue !== Ordering.NONE) {
            // If new value is different from NONE
            // Then broadcast a clearOrdering on other headCells
            headCells
            .filter(_ => _ !== headCell)
            .forEach(_ => _.clearOrdering())
          }
        });
      });
    });
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }
}
