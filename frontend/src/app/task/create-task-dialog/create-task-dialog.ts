import { Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
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
import { AssignedTaskResponse, DifficultyResponse } from '../task.model';
import { SnackbarService } from '../../snackbar/snackbar-service';

function combineDateAndTime(date: Date, time: Date): Date {
  const combined = new Date(date);
  combined.setHours(time.getHours(), time.getMinutes(), 0, 0);
  return combined;
}

function dueInFuture(group: AbstractControl): ValidationErrors | null {
  const date = group.get('dueDate')?.value as Date | null;
  const time = group.get('dueTime')?.value as Date | null;

  if (!date || !time) {
    return null;
  }

  return combineDateAndTime(date, time).getTime() > Date.now() ? null : { dueInPast: true };
}

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
  selector: 'app-create-task-dialog',
  styleUrl: './create-task-dialog.scss',
  templateUrl: './create-task-dialog.html',
})
export class CreateTaskDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<CreateTaskDialog, AssignedTaskResponse | null>);
  private readonly taskService = inject(TaskService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackbar = inject(SnackbarService);

  protected readonly householdId = inject<string>(MAT_DIALOG_DATA);
  protected readonly difficulties = signal<DifficultyResponse[]>([]);
  protected readonly submitting = signal(false);
  protected readonly today = new Date();

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', Validators.maxLength(1000)],
      difficultyId: ['', Validators.required],
      dueDate: this.formBuilder.nonNullable.control<Date | null>(null, Validators.required),
      dueTime: this.formBuilder.nonNullable.control<Date | null>(null, Validators.required),
      recurring: [false],
      intervalDays: [1, [Validators.required, Validators.min(1)]],
    },
    { validators: dueInFuture },
  );

  protected readonly recurring = toSignal(this.form.controls.recurring.valueChanges, {
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

      const task = await this.taskService.create(this.householdId, {
        name: raw.name,
        description: raw.description || null,
        difficultyId: raw.difficultyId,
        dueDate: combineDateAndTime(raw.dueDate!, raw.dueTime!).toISOString(),
        intervalDays: raw.recurring ? raw.intervalDays : 0,
      });

      this.dialogRef.close(task);
    } catch (error) {
      this.snackbar.error(this.toMessage(error));
      this.submitting.set(false);
    }
  }

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  private toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.BadRequest) {
      return 'Bitte die Eingaben prüfen — die Fälligkeit darf nicht in der Vergangenheit liegen.';
    }
    return 'Aufgabe konnte nicht erstellt werden. Bitte später erneut versuchen.';
  }
}
