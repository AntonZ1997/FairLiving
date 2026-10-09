import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButton, MatIconButton } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TaskService } from '../task-service';
import { TaskDetailResponse } from '../task.model';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { dueLabel, isOverdue } from '../due-date';

@Component({
  imports: [
    DatePipe,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatIconButton,
    MatIcon,
    MatProgressSpinner,
  ],
  selector: 'app-task-detail-dialog',
  styleUrl: './task-detail-dialog.scss',
  templateUrl: './task-detail-dialog.html',
})
export class TaskDetailDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<TaskDetailDialog>);
  private readonly taskService = inject(TaskService);
  private readonly snackbar = inject(SnackbarService);

  protected readonly taskId = inject<string>(MAT_DIALOG_DATA);
  protected readonly task = signal<TaskDetailResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);

  protected readonly dueLabel = dueLabel;
  protected readonly isOverdue = isOverdue;

  async ngOnInit(): Promise<void> {
    try {
      this.task.set(await this.taskService.findTaskDetails(this.taskId));
    } catch {
      this.snackbar.error('Aufgabe konnte nicht geladen werden.');
      this.dialogRef.close();
    } finally {
      this.loading.set(false);
    }
  }

  protected async pause(): Promise<void> {
    this.busy.set(true);

    try {
      this.task.set(await this.taskService.pauseTask(this.taskId));
      this.snackbar.success('Aufgabe pausiert. Die Zuweisung wurde entfernt.');
    } catch {
      this.snackbar.error('Aufgabe konnte nicht pausiert werden.');
    } finally {
      this.busy.set(false);
    }
  }

  protected async complete(): Promise<void> {
    const assignedTaskId = this.task()?.assignedTaskId;

    if (!assignedTaskId) {
      return;
    }

    this.busy.set(true);

    try {
      const reward = await this.taskService.complete(assignedTaskId);

      const levelUp = reward.leveledUp
        ? ` - Level ${reward.levelNumber} erreicht: ${reward.levelTitle}`
        : '';

      this.snackbar.success(`Erledigt! +${reward.earnedExperiencePoints} XP${levelUp}`);
      this.dialogRef.close();
    } catch {
      this.snackbar.error('Aufgabe konnte nicht abgeschlossen werden.');
      this.busy.set(false);
    }
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
