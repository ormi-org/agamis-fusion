import { Component, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { HeadCellDefinition } from '../typed/head-cell-definition.interface';
import { Icon } from '@shared/constants/assets';
import { Ordering } from '@shared/constants/utils/ordering';
import { Column } from '../models/column.model';

@Component({
  selector: 'shared-dyntable-head-cell',
  templateUrl: './head-cell.component.html',
  styleUrls: ['./head-cell.component.scss']
})
export class HeadCellComponent implements HeadCellDefinition, OnInit {
  @Input()
  associatedColumn: Column = {
    key: "undefined",
    resizable: false,
    value: "undefined text",
    ordering: Ordering.NONE,
    widthSubject: new BehaviorSubject(0)
  };

  protected colWidth!: number;

  protected orderingEnum: typeof Ordering = Ordering;
  protected orderingIcon: Icon = Icon.ARROW;
  // Init ordering subject
  private orderingSubject: Subject<Ordering> = new Subject();
  // Init resizing subject
  private resizingSubject: Subject<[string, number]> = new Subject();
  private resizing: boolean = false;

  constructor(private renderer: Renderer2) {
    // Init ordering subject value and reversed subscription
    this.orderingSubject.next(this.associatedColumn.ordering);
    this.orderingSubject.subscribe((updatedVal) => {
      this.associatedColumn.ordering = updatedVal;
    });
  }

  ngOnInit(): void {
    // Init width value and subscribe to changes
    this.associatedColumn.widthSubject.subscribe((updatedValue) => {
      this.colWidth = updatedValue;
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

  protected onResizeBoxMousedown(event: MouseEvent) {
    this.resizing = true;
    this.toggleResizing(event.pageX, this.colWidth);
    event.preventDefault();
  }

  private toggleResizing(startX: number, startWidth: number): void {
    let resizableMousemove = this.renderer.listen('document', 'mousemove', (event: MouseEvent) => {
      if (this.resizing) {
        const deltaX = event.pageX - startX;
        const newWidth = startWidth + deltaX;
        this.resizingSubject.next([this.associatedColumn.key, newWidth]);
      }
    });
    let resizableMouseup;
    resizableMouseup = this.renderer.listen('document', 'mouseup', (_: MouseEvent) => {
      if (this.resizing) {
        this.resizing = false;
        resizableMousemove();
        resizableMouseup();
      }
    });
  }

  public getResizing(): Observable<[string, number]> {
    return this.resizingSubject.asObservable();
  }

  public getOrdering(): Observable<Ordering> {
    return this.orderingSubject.asObservable();
  }

  public clearOrdering(): void {
    this.orderingSubject.next(Ordering.NONE);
  }
}
