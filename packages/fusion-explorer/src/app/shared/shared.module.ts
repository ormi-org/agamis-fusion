import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapitalizeFirstPipe } from './pipes/capitalize-first.pipe';
import { IconifyPipe } from './pipes/iconify.pipe';
import { DynamicTableComponent } from './components/dynamic-table/dynamic-table.component';
import { HeadCellComponent } from './components/dynamic-table/head-cell/head-cell.component';
import { CellComponent } from './components/dynamic-table/cell/cell.component';
import { RowComponent } from './components/dynamic-table/row/row.component';
import { ColumnDirective } from './components/dynamic-table/meta/column/column.directive';
import { ErrorComponent } from './pages/error/error.component';
import { SplashComponent } from './components/splash/splash.component';
import { LoadingBarComponent } from './components/loading-bar/loading-bar.component';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { SeparatorComponent } from './components/separator/separator.component';

@NgModule({
  declarations: [
    CapitalizeFirstPipe,
    IconifyPipe,
    DynamicTableComponent,
    HeadCellComponent,
    CellComponent,
    RowComponent,
    ColumnDirective,
    ErrorComponent,
    SplashComponent,
    LoadingBarComponent,
    HeaderComponent,
    SeparatorComponent,
  ],
  imports: [CommonModule, RouterModule],
  exports: [
    CapitalizeFirstPipe,
    IconifyPipe,
    DynamicTableComponent,
    HeadCellComponent,
    CellComponent,
    RowComponent,
    ColumnDirective,
    ErrorComponent,
    SplashComponent,
    LoadingBarComponent,
    HeaderComponent,
    SeparatorComponent,
  ],
})
export class SharedModule {}
