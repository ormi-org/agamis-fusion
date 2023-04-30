import { Component, Input } from '@angular/core';
import { Ordering } from '@shared/constants/utils/ordering';

@Component({
  selector: 'shared-dyntable-column',
  templateUrl: './column.component.html'
})
export class ColumnComponent {
  @Input()
  private key!: string;
  @Input()
  private title!: string;
  @Input()
  private resizable!: boolean;
  @Input()
  private initOrder: Ordering = Ordering.NONE;

  getKey(): string {
    return this.key;
  }

  getTitle(): string {
    return this.title;
  }

  isResizable(): boolean {
    return this.resizable;
  }

  getOrder(): Ordering {
    return this.initOrder;
  }
}
