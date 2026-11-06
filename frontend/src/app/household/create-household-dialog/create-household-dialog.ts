import { Component, inject, signal } from '@angular/core';
import { HouseholdResponse } from '../household.model';
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { HouseholdService } from '../household-service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { MatButton } from '@angular/material/button';

@Component({
  imports: [
    MatDialogTitle,
    ReactiveFormsModule,
    MatDialogContent,
    MatFormField,
    MatLabel,
    MatError,
    MatInput,
    MatDialogActions,
    MatButton,
  ],
  selector: 'app-create-household-dialog',
  styleUrl: './create-household-dialog.scss',
  templateUrl: './create-household-dialog.html',
})
export class CreateHouseholdDialog {
  private readonly dialogRef = inject(
    MatDialogRef<CreateHouseholdDialog, HouseholdResponse | null>,
  );
  private readonly householdService = inject(HouseholdService);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly creating = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
  });

  protected async confirm(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    this.creating.set(true);
    this.errorMessage.set(null);

    try {
      const household = await this.householdService.create(this.form.getRawValue().name);
      this.dialogRef.close(household);
    } catch {
      this.errorMessage.set('Haushalt konnte nicht angelegt werden.');
      this.creating.set(false);
    }
  }

  protected cancel(): void {
    this.dialogRef.close(null);
  }
}
