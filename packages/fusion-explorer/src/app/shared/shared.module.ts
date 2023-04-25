import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CapitalizeFirstPipe } from './pipes/capitalize-first.pipe';
import { IconifyPipe } from './pipes/iconify.pipe';



@NgModule({
  declarations: [CapitalizeFirstPipe, IconifyPipe],
  imports: [
    CommonModule
  ],
  exports: [CapitalizeFirstPipe, IconifyPipe]
})
export class SharedModule { }
