import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivateTaskDialog } from './edit-task-dialog';

describe('ActivateTaskDialog', () => {
  let component: ActivateTaskDialog;
  let fixture: ComponentFixture<ActivateTaskDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivateTaskDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(ActivateTaskDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
