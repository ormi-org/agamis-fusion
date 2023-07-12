import {
  Component,
  Input,
  OnInit,
  TemplateRef,
} from '@angular/core'
import { RowDefinition } from '../typed/row-definition.interface'
import { BehaviorSubject, Observable, Subject } from 'rxjs'
import { Cell, trackByUqId } from '../models/cell.model'

@Component({
  selector: 'shared-dyntable-row',
  templateUrl: './row.component.html',
  styleUrls: ['./row.component.scss'],
})
export class RowComponent<T extends object>
  implements RowDefinition<T>, OnInit
{
  @Input()
  index!: number
  @Input()
  templating!: {
    key: string,
    compute: ((model: T) => { value: string }),
    template: TemplateRef<unknown>
  }[]
  @Input()
  model!: T
  @Input()
  cellsWidths!: () => [string, BehaviorSubject<number>][]

  protected cells!: Cell[]

  protected cellsTracking = trackByUqId

  protected selected = false
  private selectedSubject: Subject<[boolean, T]> = new Subject()

  constructor() {
    // Init selected subject value and reversed subscription
    this.selectedSubject.next([this.selected, this.model])
    this.selectedSubject.subscribe((updatedVal) => {
      this.selected = updatedVal[0]
    })
  }

  ngOnInit(): void {
    // populate cells
    this.cells = this.templating.reduce((acc, { key, compute, template }, i) => {
      const computedValue = compute(this.model)
      acc.push(
        new Cell(
          i.toString(),
          i,
          computedValue,
          template,
          this.cellsWidths().find(_ => _[0] === key)?.[1] || new BehaviorSubject(0)
        )
      )
      return acc
    }, <Cell[]>[])
  }

  protected select(): void {
    this.selectedSubject.next([true, this.model])
  }

  clearSelect(): void {
    this.selectedSubject.next([false, this.model])
  }

  isSelected(): Observable<[boolean, T]> {
    return this.selectedSubject.asObservable()
  }

  getIndex(): number {
    return this.index
  }
  getModel(): T {
    return this.model
  }
}
