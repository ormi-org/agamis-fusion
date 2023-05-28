import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { SharedModule } from '@shared/shared.module';
import { StoreModule } from '@ngrx/store';
import { appStateCoreFeatureKey, appStateReducer } from './states/app-state/app-state.reducers';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule,
    SharedModule,
    StoreModule.forFeature(
      appStateCoreFeatureKey,
      appStateReducer
    )
  ]
})
export class CoreModule { }
