import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingBarComponent } from './loading-bar.component';
import { BehaviorSubject } from 'rxjs';

describe('LoadingBarComponent', () => {
  let component: LoadingBarComponent;
  let fixture: ComponentFixture<LoadingBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LoadingBarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingBarComponent);
    fixture.componentInstance.nextStageSignalSubject = new BehaviorSubject<void>(undefined);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
