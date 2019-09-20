import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurationArchiveComponent } from './curation-archive.component';

describe('CurationArchiveComponent', () => {
  let component: CurationArchiveComponent;
  let fixture: ComponentFixture<CurationArchiveComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CurationArchiveComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurationArchiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
