import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateHouseholdDialog } from './create-household-dialog';

describe('CreateHouseholdDialog', () => {
  let component: CreateHouseholdDialog;
  let fixture: ComponentFixture<CreateHouseholdDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateHouseholdDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateHouseholdDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
