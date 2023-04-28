import { Component, Input, OnInit } from '@angular/core';
import { CellDefinition } from '../typed/cell-definition.interface';
import { Observable, startWith } from 'rxjs';

@Component({
  selector: 'shared-dyntable-cell',
  templateUrl: './cell.component.html',
  styleUrls: ['./cell.component.scss']
})
export class CellComponent implements CellDefinition {
  @Input()
  index!: number;
  @Input()
  value!: string;

  getValue(): string {
    return this.value;
  }
}
