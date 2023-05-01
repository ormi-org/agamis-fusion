import { Component, Input } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { HeadCellDefinition } from '../typed/head-cell-definition.interface';
import { Icon } from '@shared/constants/assets';
import { Ordering } from '@shared/constants/utils/ordering';
import { Column } from '../models/column.model';

@Component({
  selector: 'shared-dyntable-head-cell',
  templateUrl: './head-cell.component.html',
  styleUrls: ['./head-cell.component.scss']
})
export class HeadCellComponent implements HeadCellDefinition {
  @Input()
  associatedColumn: Column = {
    key: "undefined",
    resizable: false,
    value: "undefined text",
    ordering: Ordering.NONE
  };

  protected orderingEnum: typeof Ordering = Ordering;
  protected orderingIcon: Icon = Icon.ARROW;
  // Init default ordering to NONE
  private orderingSubject: Subject<Ordering> = new Subject();

  constructor() {
    // Init ordering subject value and reversed subscription
    this.orderingSubject.next(this.associatedColumn.ordering);
    this.orderingSubject.subscribe((updatedVal) => {
      this.associatedColumn.ordering = updatedVal;
    });
  }

  protected switchOrdering(): void {
    this.orderingSubject.next(
      (() => {
        switch (this.associatedColumn.ordering) {
          case Ordering.ASC:
            return Ordering.DESC;
          default:
            return Ordering.ASC;
        }
      })()
    );
  }

  protected isResizable(): boolean {
    return this.associatedColumn.resizable;
  }

  protected getValue(): string {
    return this.associatedColumn.value;
  }

  public getOrdering(): Observable<Ordering> {
    return this.orderingSubject.asObservable();
  }

  public clearOrdering(): void {
    this.orderingSubject.next(Ordering.NONE);
  }
}
