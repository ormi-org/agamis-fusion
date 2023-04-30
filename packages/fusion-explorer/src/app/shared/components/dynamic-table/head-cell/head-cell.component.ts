import { Component, Input } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { HeadCellDefinition } from '../typed/head-cell-definition.interface';
import { Icon } from '@shared/constants/assets';
import { Ordering } from '@shared/constants/utils/ordering';

@Component({
  selector: 'shared-dyntable-head-cell',
  templateUrl: './head-cell.component.html',
  styleUrls: ['./head-cell.component.scss']
})
export class HeadCellComponent implements HeadCellDefinition {
  @Input()
  value: string = "undefined text";
  @Input()
  resizable: boolean = false;
  @Input()
  ordering: Ordering = Ordering.NONE;

  protected orderingEnum: typeof Ordering = Ordering;
  protected orderingIcon: Icon = Icon.ARROW;
  // Init default ordering to NONE
  private orderingSubject: Subject<Ordering> = new Subject();

  constructor() {
    // Init ordering subject value and reversed subscription
    this.orderingSubject.next(this.ordering);
    this.orderingSubject.subscribe((updatedVal) => {
      this.ordering = updatedVal;
    });
  }

  protected switchOrdering(): void {
    this.orderingSubject.next(
      (() => {
        switch (this.ordering) {
          case Ordering.ASC:
            return Ordering.DESC;
          default:
            return Ordering.ASC;
        }
      })()
    );
  }

  protected isResizable(): boolean {
    return this.resizable;
  }

  protected getValue(): string {
    return this.value;
  }

  public getOrdering(): Observable<Ordering> {
    return this.orderingSubject.asObservable();
  }

  public clearOrdering(): void {
    this.orderingSubject.next(Ordering.NONE);
  }
}
