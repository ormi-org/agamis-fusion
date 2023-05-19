import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { MenuComponent } from './components/menu/menu.component';
import { ItemComponent } from './components/menu/item/item.component';
import { SharedModule } from '@shared/shared.module';
import { UsersComponent } from './pages/users/users.component';
import { CoreModule } from '@core/core.module';

@NgModule({
  declarations: [MenuComponent, ItemComponent, UsersComponent],
  imports: [CommonModule, AdminRoutingModule, SharedModule],
  exports: [MenuComponent],
})
export class AdminModule {}
