import { Component, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { HeadCellDefinition } from '../typed/head-cell-definition.interface';
import { Icon } from '@shared/constants/assets';
import { DefinedOrdering, Ordering } from '@shared/constants/utils/ordering';
import { Column } from '../models/column.model';

const MIN_WIDTH = 50;

@Component({
  selector: 'shared-dyntable-head-cell',
  templateUrl: './head-cell.component.html',
  styleUrls: ['./head-cell.component.scss']
})
export class HeadCellComponent implements HeadCellDefinition, OnInit {
  @Input()
  associatedColumn: Column = new Column(
    "undefined",
    "undefined text",
    false,
    Ordering.NONE,
    new BehaviorSubject(0)
  );

  private colWidth!: number;

  protected orderingEnum: typeof Ordering = Ordering;
  protected orderingIcon: Icon = Icon.ARROW;
  // Init ordering subject
  private orderingSubject: Subject<DefinedOrdering> = new Subject();
  // Init resizing subject (key, newWidth, moveDelta)
  private resizingSubject: Subject<[string, number, number]> = new Subject();
  private resizing: boolean = false;

  constructor(private renderer: Renderer2, private host: ElementRef) {
    // Init ordering subject value with specified one
    if (this.associatedColumn.ordering !== Ordering.NONE) {
      this.orderingSubject.next(this.associatedColumn.ordering);
    }
  }

  ngOnInit(): void {
    // Init width value and subscribe to changes
    this.associatedColumn.widthSubject.subscribe((updatedValue) => {
      this.colWidth = updatedValue;
      this.host.nativeElement.style.width = updatedValue + 'px';
    });
  }

  protected switchOrdering(): void {
    var newOrdering = (() => {
      switch (this.associatedColumn.ordering) {
        case Ordering.ASC:
          return Ordering.DESC;
        default:
          return Ordering.ASC;
      }
    })();
    this.associatedColumn.ordering = newOrdering;
    this.orderingSubject.next(newOrdering);
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
    let previousDelta = 0;
    let resizableMousemove = this.renderer.listen('document', 'mousemove', (event: MouseEvent) => {
      if (this.resizing) {
        const deltaX = event.pageX - startX;
        const newWidth = Math.max(startWidth + deltaX, MIN_WIDTH);
        // const newWidth = startWidth + deltaX;
        this.resizingSubject.next([this.associatedColumn.key, newWidth, - (previousDelta - deltaX)]);
        previousDelta = deltaX;
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

  public getResizing(): Observable<[string, number, number]> {
    return this.resizingSubject.asObservable();
  }

  public getOrdering(): Observable<DefinedOrdering> {
    return this.orderingSubject.asObservable();
  }

  public clearOrdering(): void {
    this.associatedColumn.ordering = Ordering.NONE;
  }
}
