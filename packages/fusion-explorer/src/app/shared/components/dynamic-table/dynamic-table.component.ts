import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Column } from './models/column.model';
import { Row } from './models/row.model';
import { DataSource } from './typed/data-source.interface';
import { Uniquely } from './typed/uniquely.interface';

@Component({
  selector: 'shared-dyntable',
  templateUrl: './dynamic-table.component.html',
  styleUrls: ['./dynamic-table.component.scss']
})
export class DynamicTableComponent<T extends Uniquely> implements OnInit, OnDestroy {
  @Input()
  datasource!: DataSource<T>;

  protected columns: Array<Column> = [];
  protected rows: Array<Row<T>> = [];

  ngOnInit(): void {
    this.datasource.connect().subscribe((updatedVal) => {
      this.rows = updatedVal.map((v, i) => {
        return new Row(v.uqId, i, v);
      });
    })
  }

  ngOnDestroy(): void {
    this.datasource.disconnect();
  }
}
