import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurationOrphanComponent } from './curation-orphan.component';

describe('CurationOrphanComponent', () => {
  let component: CurationOrphanComponent;
  let fixture: ComponentFixture<CurationOrphanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CurationOrphanComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurationOrphanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
