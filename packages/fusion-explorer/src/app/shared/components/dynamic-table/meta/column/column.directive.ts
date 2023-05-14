import { Directive, Input } from '@angular/core';
import { Ordering } from '@shared/constants/utils/ordering';

@Directive({
  selector: 'shared-dyntable-column'
})
export class ColumnDirective {
  @Input()
  key!: string;
  @Input()
  title!: string;
  @Input()
  resizable!: boolean;
  @Input()
  initOrder: Ordering = Ordering.NONE;
  @Input()
  width: number = 0;

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

  getWidth(): number {
    return this.width;
  }
}
