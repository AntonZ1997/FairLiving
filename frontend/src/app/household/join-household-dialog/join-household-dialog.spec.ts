import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JoinHouseholdDialog } from './join-household-dialog';

describe('JoinHouseholdDialog', () => {
  let component: JoinHouseholdDialog;
  let fixture: ComponentFixture<JoinHouseholdDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JoinHouseholdDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(JoinHouseholdDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
