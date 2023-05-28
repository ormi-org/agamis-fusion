import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ExplorerRoutingModule } from './explorer-routing.module';
import { ExplorerComponent } from './explorer.component';
import { StoreModule } from '@ngrx/store';
import { CoreModule } from '@core/core.module';
import { SharedModule } from '@shared/shared.module';
import {
  explorerStateExplorerFeatureKey,
  explorerStateReducer,
} from './states/explorer-state/explorer-state.reducers';
import { BrowserComponent } from './components/browser/browser.component';
import { AdminModule } from '@admin/admin.module';

@NgModule({
  declarations: [
    ExplorerComponent,
    BrowserComponent
  ],
  imports: [
    CommonModule,
    CoreModule,
    SharedModule,
    AdminModule,
    ExplorerRoutingModule,
    StoreModule.forFeature(
      explorerStateExplorerFeatureKey,
      explorerStateReducer
    ),
  ],
})
export class ExplorerModule {}
