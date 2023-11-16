import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamicTableComponent } from './dynamic-table.component';
import { Uniquely } from './typed/uniquely.interface';
import { Component } from '@angular/core';

@Component({
  selector: 'test-cmp',
  template: `
    <shared-dyntable [datasource]="datasource">
      <shared-dyntable-column
        key="first"
        [title]="'a first column'"
        [resizable]="true"
        [initOrder]="initOrder"
        [width]=120>
      </shared-dyntable-column>
      <shared-dyntable-column
        key="second"
        [title]="'a second column'"
        [resizable]="true"
        [width]=140>
      </shared-dyntable-column>
      <shared-dyntable-column
        key="third"
        [title]="'a third column'"
        [resizable]="true"
        [width]=120>
      </shared-dyntable-column>
      <shared-dyntable-column
        key="fourth"
        [title]="'a fourth column'"
        [resizable]="false">
      </shared-dyntable-column>
    </shared-dyntable>
  `
})
class TestWrapperDynamicTableComponent {}

describe('DynamicTableComponent', () => {
  let component: DynamicTableComponent<Uniquely>;
  let fixture: ComponentFixture<TestWrapperDynamicTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DynamicTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestWrapperDynamicTableComponent);
    component = fixture.debugElement.children[0].componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
