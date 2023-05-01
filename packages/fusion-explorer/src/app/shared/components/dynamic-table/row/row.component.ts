import { AfterViewInit, Component, ContentChildren, Directive, Input, OnInit, QueryList } from '@angular/core';
import { RowDefinition } from '../typed/row-definition.interface';
import { Observable, Subject } from 'rxjs';
import { CellComponent } from '../cell/cell.component';
import { Cell, trackByUqId } from '../models/cell.model';
import { CellDefinition } from '../typed/cell-definition.interface';

@Component({
  selector: 'shared-dyntable-row',
  templateUrl: './row.component.html',
  styleUrls: ['./row.component.scss']
})
export class RowComponent<T extends Object> implements RowDefinition<T>, OnInit {
  @Input()
  protected index!: number;
  @Input()
  protected keys!: string[];
  @Input()
  protected model!: T;

  protected cells!: Cell[];

  cellsTracking = trackByUqId;

  protected selected: boolean = false;
  private selectedSubject: Subject<boolean> = new Subject();

  constructor() {
    // Init selected subject value and reversed subscription
    this.selectedSubject.next(this.selected);
    this.selectedSubject.subscribe((updatedVal) => {
      this.selected = updatedVal;
    });
  }

  ngOnInit(): void {
    // populate cells;
    // remove unused cells
    this.cells = Object.entries(this.model).reduce((acc, field, i) => {
      if (this.keys.includes(field[0])) acc.push(new Cell(i.toString(), i, field[1].toString()));
      return acc;
    }, <Cell[]>[]);
  }

  protected select(): void {
    this.selectedSubject.next(true);
  }

  clearSelect(): void {
    this.selectedSubject.next(false);
  }

  isSelected(): Observable<boolean> {
    return this.selectedSubject.asObservable();
  }

  getIndex(): number {
    return this.index;
  }
  getModel(): T {
    return this.model;
  }
}
