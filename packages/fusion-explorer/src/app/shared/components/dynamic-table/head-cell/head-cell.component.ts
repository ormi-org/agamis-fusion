import { Component, Input, OnInit } from '@angular/core';
import { Observable, Subject, Subscription, startWith } from 'rxjs';
import { HeadCellDefinition } from '../models/head-cell-definition.model';
import { Icon } from '@shared/constants/assets';
import { Ordering } from '@shared/constants/utils/ordering';

@Component({
  selector: 'admin-dyntable-head-cell',
  templateUrl: './head-cell.component.html',
  styleUrls: ['./head-cell.component.scss']
})
export class HeadCellComponent implements OnInit {
  protected orderingEnum: typeof Ordering = Ordering;
  protected orderingIcon: Icon = Icon.ARROW;
  protected value!: HeadCellDefinition;
  // Init default ordering to NONE
  protected ordering: Ordering = Ordering.NONE;
  private orderingSubject: Subject<Ordering> = new Subject();

  constructor() {
    // Init ordering subject value and reverse subscription
    this.orderingSubject.next(this.ordering);
    this.orderingSubject.subscribe((newVal) => {
      this.ordering = newVal;
    });
  }
  
  @Input()
  def!: Observable<HeadCellDefinition>;

  ngOnInit() {
    this.def
    .pipe(startWith(new HeadCellDefinition(
      "undefined text"
    )))
    .subscribe((updatedVal) => {
      this.value = updatedVal;
    })
  }

  switchOrdering(): void {
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

  getOrdering(): Subject<Ordering> {
    return this.orderingSubject;
  }
}
