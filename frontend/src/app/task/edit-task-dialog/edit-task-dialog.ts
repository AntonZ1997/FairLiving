import { Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatCheckbox } from '@angular/material/checkbox';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerToggle,
} from '@angular/material/datepicker';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatError, MatFormField, MatInput, MatLabel, MatSuffix } from '@angular/material/input';
import { MatOption, MatSelect } from '@angular/material/select';
import {
  MatTimepicker,
  MatTimepickerInput,
  MatTimepickerToggle,
} from '@angular/material/timepicker';
import { TaskService } from '../task-service';
import { DifficultyResponse, TaskDetailResponse } from '../task.model';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { combineDateAndTime, dueRequiredWhenActivatingTask } from '../due-date';

@Component({
  imports: [
    ReactiveFormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    MatSuffix,
    MatSelect,
    MatOption,
    MatCheckbox,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatTimepicker,
    MatTimepickerInput,
    MatTimepickerToggle,
  ],
  selector: 'app-edit-task-dialog',
  styleUrl: './edit-task-dialog.scss',
  templateUrl: './edit-task-dialog.html',
})
export class EditTaskDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<EditTaskDialog, TaskDetailResponse | null>);
  private readonly taskService = inject(TaskService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackbar = inject(SnackbarService);

  protected readonly task = inject<TaskDetailResponse>(MAT_DIALOG_DATA);
  protected readonly difficulties = signal<DifficultyResponse[]>([]);
  protected readonly submitting = signal(false);
  protected readonly today = new Date();

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      name: [this.task.name, [Validators.required, Validators.maxLength(200)]],
      description: [this.task.description ?? '', Validators.maxLength(1000)],
      difficultyId: [this.task.difficultyId, Validators.required],
      recurring: [this.task.intervalDays > 0],
      intervalDays: [
        this.task.intervalDays > 0 ? this.task.intervalDays : 1,
        [Validators.required, Validators.min(1)],
      ],
      activate: [false],
      dueDate: this.formBuilder.nonNullable.control<Date | null>(null),
      dueTime: this.formBuilder.nonNullable.control<Date | null>(null),
    },
    { validators: dueRequiredWhenActivatingTask },
  );

  protected readonly recurring = toSignal(this.form.controls.recurring.valueChanges, {
    initialValue: this.task.intervalDays > 0,
  });

  protected readonly activate = toSignal(this.form.controls.activate.valueChanges, {
    initialValue: false,
  });

  async ngOnInit(): Promise<void> {
    try {
      this.difficulties.set(await this.taskService.findDifficulties());
    } catch {
      this.snackbar.error('Schwierigkeitsstufen konnten nicht geladen werden.');
    }
  }

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    this.submitting.set(true);

    try {
      const raw = this.form.getRawValue();

      const updated = await this.taskService.updateTask(this.task.taskId, {
        name: raw.name,
        description: raw.description || null,
        difficultyId: raw.difficultyId,
        intervalDays: raw.recurring ? raw.intervalDays : 0,
        dueDate: raw.activate ? combineDateAndTime(raw.dueDate!, raw.dueTime!).toISOString() : null,
      });

      this.dialogRef.close(updated);
    } catch (error) {
      this.snackbar.error(this.toMessage(error));
      this.submitting.set(false);
    }
  }

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  private toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict) {
      return 'Nur pausierte Aufgaben können bearbeitet werden.';
    }
    return 'Aufgabe konnte nicht gespeichert werden.';
  }
}
