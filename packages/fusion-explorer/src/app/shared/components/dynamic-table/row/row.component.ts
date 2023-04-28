import { AfterViewInit, Component, ContentChildren, Directive, Input, OnInit, QueryList } from '@angular/core';
import { RowDefinition } from '../typed/row-definition.interface';
import { Observable, Subject } from 'rxjs';
import { CellComponent } from '../cell/cell.component';
import { Cell, trackByUqId } from '../models/cell.model';

@Component({
  selector: 'shared-dyntable-row',
  templateUrl: './row.component.html',
  styleUrls: ['./row.component.scss']
})
export class RowComponent<T extends Object> implements RowDefinition<T>, AfterViewInit {
  @Input()
  index!: number;
  @Input()
  model!: T;

  protected cells!: Cell[];

  cellsTracking = trackByUqId;

  @ContentChildren(CellComponent)
  cellsElements!: QueryList<CellComponent>;

  protected selected: boolean = false;
  private selectedSubject: Subject<boolean> = new Subject();

  constructor() {
    // Init selected subject value and reversed subscription
    this.selectedSubject.next(this.selected);
    this.selectedSubject.subscribe((updatedVal) => {
      this.selected = updatedVal;
    });
  }

  ngAfterViewInit(): void {
    this.cells = Object.entries(this.model).map((field, i) => {
      return new Cell(i.toString(), i, field[1].toString());
    });
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
