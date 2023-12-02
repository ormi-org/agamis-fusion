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
    // Calculate the widths of cells
    const cellsWidths = this.cellsWidths();

    // Create a Map to store cell widths for faster look
    const cellsWidthsMap = new Map(cellsWidths.map(([key, width]) => [key, width]));

    // Initialize the cells array
    this.cells = [];

    // Iterate through the templating data
    this.templating.forEach(({ key, compute, template }, i) => {

      // Calculate the computed value for the current cell
      const computedValue = compute(this.model);

      // Retrieve the corresponding cell width from the Map or create a new BehaviorSubject if not found
      const widthSubject = cellsWidthsMap.get(key) ?? new BehaviorSubject(0);

      // Create a new Cell object with the calculated values
      this.cells.push(new Cell(i.toString(), i, computedValue, template, widthSubject));
    });
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
