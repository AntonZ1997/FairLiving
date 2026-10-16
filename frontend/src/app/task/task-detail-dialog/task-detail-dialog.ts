import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButton, MatIconButton } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { firstValueFrom } from 'rxjs';
import { TaskService } from '../task-service';
import { TaskDetailResponse } from '../task.model';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { dueLabel, isOverdue } from '../due-date';
import { EditTaskDialog } from '../edit-task-dialog/edit-task-dialog';

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
  private readonly dialog = inject(MatDialog);

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

  protected async edit(): Promise<void> {
    const current = this.task();

    if (!current) {
      return;
    }

    const dialogRef = this.dialog.open<
      EditTaskDialog,
      TaskDetailResponse,
      TaskDetailResponse | null
    >(EditTaskDialog, { data: current, width: '520px' });

    const updated = await firstValueFrom(dialogRef.afterClosed());

    if (!updated) {
      return;
    }

    if (updated.active) {
      this.snackbar.success(`Aufgabe gespeichert und an ${updated.assignedMemberName} vergeben.`);
      this.dialogRef.close();
      return;
    }

    this.task.set(updated);
    this.snackbar.success('Aufgabe gespeichert.');
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
