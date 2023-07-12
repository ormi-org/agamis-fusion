import { NgModule } from '@angular/core'
import { RouterModule } from '@angular/router'
import { adminRoutes } from './admin.routes'

@NgModule({
  imports: [RouterModule.forChild(adminRoutes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
