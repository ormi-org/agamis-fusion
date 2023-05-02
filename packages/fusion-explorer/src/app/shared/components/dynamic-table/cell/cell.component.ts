import { Component, Input, OnInit } from '@angular/core';
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

  protected width: number = 0;

  ngOnInit(): void {
      this.widthSubject.subscribe((updateValue) => {
        this.width = updateValue
      });
  }

  getValue(): string {
    return this.value;
  }
}
