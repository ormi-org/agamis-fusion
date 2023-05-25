import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ExplorerRoutingModule } from './explorer-routing.module';
import { ExplorerComponent } from './explorer.component';
import { StoreModule } from '@ngrx/store';
import { CoreModule } from '@core/core.module';

@NgModule({
  declarations: [ExplorerComponent],
  imports: [CommonModule, CoreModule, ExplorerRoutingModule, StoreModule.forRoot({})],
})
export class ExplorerModule {}
