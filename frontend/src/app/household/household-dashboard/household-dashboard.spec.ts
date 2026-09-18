import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HouseholdDashboard } from './household-dashboard';

describe('HouseholdDashboard', () => {
  let component: HouseholdDashboard;
  let fixture: ComponentFixture<HouseholdDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HouseholdDashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
