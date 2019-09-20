import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurationManageComponent } from './curation-manage.component';

describe('CurationManageComponent', () => {
  let component: CurationManageComponent;
  let fixture: ComponentFixture<CurationManageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CurationManageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurationManageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
