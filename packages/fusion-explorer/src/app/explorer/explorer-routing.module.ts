import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { explorerRoutes } from './explorer.routes';

@NgModule({
  imports: [RouterModule.forChild(explorerRoutes)],
  exports: [RouterModule]
})
export class ExplorerRoutingModule { }
