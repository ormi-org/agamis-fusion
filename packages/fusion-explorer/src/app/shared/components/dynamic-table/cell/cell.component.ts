import { Component, ElementRef, Input, OnInit, TemplateRef } from '@angular/core';
import { CellDefinition } from '../typed/cell-definition.interface';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'shared-dyntable-cell',
  templateUrl: './cell.component.html',
  styleUrls: ['./cell.component.scss']
})
export class CellComponent implements CellDefinition, OnInit {
  @Input()
  index!: number;
  @Input()
  context!: {
    value: string
  };
  @Input()
  widthSubject!: BehaviorSubject<number>;
  @Input()
  template!: TemplateRef<any>;

  constructor(private host: ElementRef) {}

  ngOnInit(): void {
    this.widthSubject.subscribe((updatedValue) => {
      this.host.nativeElement.style.width = updatedValue + 'px';
    });
  }

  getValue(): string {
    return this.context.value;
  }
}
