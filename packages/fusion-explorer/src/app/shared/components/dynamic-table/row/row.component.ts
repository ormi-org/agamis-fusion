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
  index!: number;
  @Input()
  keys!: string[];
  @Input()
  model!: T;
  @Input()
  cellsWidths!: () => [string, BehaviorSubject<number>][];

  protected cells!: Cell[];

  protected cellsTracking = trackByUqId;

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
    // populate cells
    this.cells = this.keys.reduce((acc, key, i) => {
      const keyPath = key.split('.');
      acc.push(
        new Cell(
          i.toString(),
          i,
          keyPath.length > 1 ? 
          keyPath.slice(1).reduce((previous, pathItem) => {
            console.log(pathItem, previous);
            return pathItem ? previous[pathItem] : previous;
          }, this.model[keyPath[0]]).toString() :
          this.model[key].toString(),
          this.cellsWidths().find(_ => _[0] === key)?.[1] || new BehaviorSubject(0)
        )
      )
      return acc;
    }, <Cell[]>[]);
    // this.cells = Object.entries(this.model).reduce((acc, field, i) => {
    //   console.log(field);
    //   if (this.keys.includes(field[0])) {
    //     acc.push(
    //       new Cell(
    //         i.toString(),
    //         this.keys.indexOf(field[0]),
    //         field[0].split('.').reduce((previous, pathItem) => {
    //           return pathItem ? previous[pathItem] : previous;
    //         }, field[1]).toString(),
    //         this.cellsWidths().find(_ => _[0] === field[0])?.[1] || new BehaviorSubject(0)
    //       )
    //     );
    //   }
    //   return acc;
    // }, <Cell[]>[]).sort((a, b) => a.index - b.index);
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
