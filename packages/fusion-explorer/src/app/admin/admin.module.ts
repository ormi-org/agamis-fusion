import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { MenuComponent } from './menu/menu.component';
import { ItemComponent } from './menu/item/item.component';
import { SharedModule } from '@shared/shared.module';

@NgModule({
  declarations: [MenuComponent, ItemComponent],
  imports: [CommonModule, SharedModule, AdminRoutingModule],
  exports: [MenuComponent],
})
export class AdminModule {}
