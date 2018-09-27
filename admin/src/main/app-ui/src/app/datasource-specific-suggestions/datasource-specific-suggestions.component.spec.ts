import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasourceSpecificSuggestionsComponent } from './datasource-specific-suggestions.component';

describe('DatasourceSpecificSuggestionsComponent', () => {
  let component: DatasourceSpecificSuggestionsComponent;
  let fixture: ComponentFixture<DatasourceSpecificSuggestionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasourceSpecificSuggestionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasourceSpecificSuggestionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
