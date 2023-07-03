import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SplashComponent } from './splash.component';
import { SharedModule } from '@shared/shared.module';
import { CoreModule } from '@core/core.module';
import { StoreModule } from '@ngrx/store';

describe('SplashComponent', () => {
  let component: SplashComponent;
  let fixture: ComponentFixture<SplashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedModule, CoreModule, StoreModule.forRoot({})],
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
