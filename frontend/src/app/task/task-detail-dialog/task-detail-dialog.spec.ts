import { ComponentFixture, TestBed } from '@angular/core/testing';
import TaskDetailDialog from './task-detail-dialog';

describe('TaskDetailDialog', () => {
  let component: TaskDetailDialog;
  let fixture: ComponentFixture<TaskDetailDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskDetailDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetailDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
