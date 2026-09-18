import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HouseholdList } from './household-list';

describe('HouseholdList', () => {
  let component: HouseholdList;
  let fixture: ComponentFixture<HouseholdList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HouseholdList],
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
