import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExplorerComponent } from './explorer.component';
import { StoreModule } from '@ngrx/store';
import { CoreModule } from '@core/core.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ExplorerComponent', () => {
  let component: ExplorerComponent;
  let fixture: ComponentFixture<ExplorerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CoreModule, StoreModule.forRoot({}), RouterTestingModule],
      declarations: [ExplorerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ExplorerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
