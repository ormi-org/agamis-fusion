import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RowComponent } from './row.component';
import { BehaviorSubject } from 'rxjs';
import { CellComponent } from '../cell/cell.component';
import { SharedModule } from '@shared/shared.module';

describe('RowComponent', () => {
  let component: RowComponent<object>;
  let fixture: ComponentFixture<RowComponent<object>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RowComponent, CellComponent ],
      imports: [ SharedModule ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RowComponent);
    component = fixture.componentInstance;
    component.index = 0;
    component.templating = [];
    component.model = {
      first: "a value for first",
      second: "a value for second",
      third: "a value for third",
      fourth: "a value for fourth"
    }
    component.cellsWidths = () => [
      ['first', new BehaviorSubject(100)],
      ['second', new BehaviorSubject(100)],
      ['third', new BehaviorSubject(100)],
      ['fourth', new BehaviorSubject(100)]
    ];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  test('#select() should set #selected true', () => {
    // should be false at first
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((component as any).selected).toBe(false);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (component as any).select();
    // should be true after click
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((component as any).selected).toBe(true);
    component.clearSelect();
    // should be false after reset
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect((component as any).selected).toBe(false);
  });
});
