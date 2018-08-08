import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasourceSummaryComponent } from './datasource-summary.component';

describe('DatasourceSummaryComponent', () => {
  let component: DatasourceSummaryComponent;
  let fixture: ComponentFixture<DatasourceSummaryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasourceSummaryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasourceSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
