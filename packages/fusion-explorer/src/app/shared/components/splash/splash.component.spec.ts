import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SplashComponent } from './splash.component';
import { SharedModule } from '@shared/shared.module';
import { BehaviorSubject } from 'rxjs';

describe('SplashComponent', () => {
  let component: SplashComponent;
  let fixture: ComponentFixture<SplashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedModule],
      declarations: [SplashComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SplashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
