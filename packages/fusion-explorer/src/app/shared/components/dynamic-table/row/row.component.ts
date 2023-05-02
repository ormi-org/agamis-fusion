import {
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { RowDefinition } from '../typed/row-definition.interface';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Cell, trackByUqId } from '../models/cell.model';

@Component({
  selector: 'shared-dyntable-row',
  templateUrl: './row.component.html',
  styleUrls: ['./row.component.scss'],
})
export class RowComponent<T extends Object>
  implements RowDefinition<T>, OnInit
{
  @Input()
  protected index!: number;
  @Input()
  protected keys!: string[];
  @Input()
  protected model!: T;
  @Input()
  protected cellsWidths!: () => [string, BehaviorSubject<number>][];

  protected cells!: Cell[];

  cellsTracking = trackByUqId;

  protected selected: boolean = false;
  private selectedSubject: Subject<[boolean, T]> = new Subject();

  constructor() {
    // Init selected subject value and reversed subscription
    this.selectedSubject.next([this.selected, this.model]);
    this.selectedSubject.subscribe((updatedVal) => {
      this.selected = updatedVal[0];
    });
  }

  ngOnInit(): void {
    // populate cells;
    // remove unused cells
    this.cells = Object.entries(this.model).reduce((acc, field, i) => {
      if (this.keys.includes(field[0])) {
        acc.push(
          new Cell(
            i.toString(),
            i,
            field[1].toString(),
            this.cellsWidths().find(_ => _[0] === field[0])?.[1] || new BehaviorSubject(0)
          )
        );
      }
      return acc;
    }, <Cell[]>[]);
  }

  protected select(): void {
    this.selectedSubject.next([true, this.model]);
  }

  clearSelect(): void {
    this.selectedSubject.next([false, this.model]);
  }

  isSelected(): Observable<[boolean, T]> {
    return this.selectedSubject.asObservable();
  }

  getIndex(): number {
    return this.index;
  }
  getModel(): T {
    return this.model;
  }
}
