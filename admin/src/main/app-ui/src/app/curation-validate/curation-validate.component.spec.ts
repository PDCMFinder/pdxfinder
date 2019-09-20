import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurationValidateComponent } from './curation-validate.component';

describe('CurationValidateComponent', () => {
  let component: CurationValidateComponent;
  let fixture: ComponentFixture<CurationValidateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CurationValidateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurationValidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
