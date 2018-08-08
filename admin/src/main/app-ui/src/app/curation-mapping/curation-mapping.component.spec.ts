import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurationMappingComponent } from './curation-mapping.component';

describe('CurationMappingComponent', () => {
  let component: CurationMappingComponent;
  let fixture: ComponentFixture<CurationMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CurationMappingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurationMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
