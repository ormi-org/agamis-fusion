import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeadCellComponent } from './head-cell.component';

describe('HeadCellComponent', () => {
  let component: HeadCellComponent;
  let fixture: ComponentFixture<HeadCellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HeadCellComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HeadCellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
