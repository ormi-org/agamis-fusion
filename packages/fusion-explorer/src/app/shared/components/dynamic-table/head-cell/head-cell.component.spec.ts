import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeadCellComponent } from './head-cell.component';
import { SharedModule } from '@shared/shared.module';
import { Ordering } from '@shared/constants/utils/ordering';

describe('HeadCellComponent', () => {
  let component: HeadCellComponent;
  let fixture: ComponentFixture<HeadCellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HeadCellComponent ],
      imports: [ SharedModule ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HeadCellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  test('#switchOrdering() should toggle #associatedColumn->ordering', () => {
    expect(component.associatedColumn.ordering).toBe(Ordering.NONE);
    (component as any).switchOrdering();
    expect(component.associatedColumn.ordering).toBe(Ordering.ASC);
    (component as any).switchOrdering();
    expect(component.associatedColumn.ordering).toBe(Ordering.DESC);
  });

  test('#clearOrdering() should set #associatedColumn->ordering to NONE', () => {
    component.associatedColumn.ordering = Ordering.ASC;
    component.clearOrdering();
    expect(component.associatedColumn.ordering).toBe(Ordering.NONE);
  });
});
