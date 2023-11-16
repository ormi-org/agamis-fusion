import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CellComponent } from './cell.component';
import { SharedModule } from '@shared/shared.module';
import { BehaviorSubject } from 'rxjs';

describe('CellComponent', () => {
  let component: CellComponent;
  let fixture: ComponentFixture<CellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CellComponent ],
      imports: [ SharedModule ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CellComponent);
    component = fixture.componentInstance;
    component.widthSubject = new BehaviorSubject(100);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
