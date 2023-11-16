import { Directive, Input, TemplateRef } from '@angular/core'
import { Ordering } from '@shared/constants/utils/ordering'

@Directive({
  selector: 'shared-dyntable-column'
})
export class ColumnDirective {
  @Input()
  key!: string
  @Input()
  template!: TemplateRef<unknown>
  @Input()
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  compute: ((model: any) => { value: string }) = (m) => {
    return {
      value: m[this.key]?.toString() || ""
    }
  }
  @Input()
  title!: string
  @Input()
  resizable!: boolean
  @Input()
  initOrder: Ordering = Ordering.NONE
  @Input()
  width = 0

  getKey(): string {
    return this.key
  }

  getTitle(): string {
    return this.title
  }

  isResizable(): boolean {
    return this.resizable
  }

  getOrder(): Ordering {
    return this.initOrder
  }

  getWidth(): number {
    return this.width
  }
}
