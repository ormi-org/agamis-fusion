import { Component, ElementRef, Input, OnInit } from '@angular/core';
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
  value!: string;
  @Input()
  widthSubject!: BehaviorSubject<number>;

  constructor(private host: ElementRef) {}

  ngOnInit(): void {
    this.widthSubject.subscribe((updatedValue) => {
      this.host.nativeElement.style.width = updatedValue + 'px';
    });
  }

  getValue(): string {
    return this.value;
  }
}
