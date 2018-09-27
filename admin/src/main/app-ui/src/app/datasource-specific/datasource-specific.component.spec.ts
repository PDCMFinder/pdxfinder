import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasourceSpecificComponent } from './datasource-specific.component';

describe('DatasourceSpecificComponent', () => {
  let component: DatasourceSpecificComponent;
  let fixture: ComponentFixture<DatasourceSpecificComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasourceSpecificComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasourceSpecificComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
