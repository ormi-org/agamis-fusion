import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapitalizeFirstPipe } from './pipes/capitalize-first.pipe';
import { IconifyPipe } from './pipes/iconify.pipe';
import { DynamicTableComponent } from './components/dynamic-table/dynamic-table.component';
import { HeadCellComponent } from './components/dynamic-table/head-cell/head-cell.component';
import { CellComponent } from './components/dynamic-table/cell/cell.component';
import { RowComponent } from './components/dynamic-table/row/row.component';
import { ColumnComponent } from './components/dynamic-table/meta/column/column/column.component';

@NgModule({
  declarations: [
    CapitalizeFirstPipe,
    IconifyPipe,
    DynamicTableComponent,
    HeadCellComponent,
    CellComponent,
    RowComponent,
    ColumnComponent,
  ],
  imports: [CommonModule],
  exports: [
    CapitalizeFirstPipe,
    IconifyPipe,
    DynamicTableComponent,
    HeadCellComponent,
    CellComponent,
    RowComponent,
    ColumnComponent,
  ],
})
export class SharedModule {}
