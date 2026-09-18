import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { HouseholdService } from '../household-service';
import { HouseholdPreviewResponse } from '../household.model';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatButton } from '@angular/material/button';

export interface JoinHouseholdDialogData {
  invitationId: string;
  preview: HouseholdPreviewResponse;
}

@Component({
  imports: [MatDialogContent, MatDialogActions, MatButton, MatDialogTitle],
  selector: 'app-join-household-dialog',
  styleUrl: './join-household-dialog.scss',
  templateUrl: './join-household-dialog.html',
})
export class JoinHouseholdDialog {
  private readonly dialogRef = inject(MatDialogRef<JoinHouseholdDialog, string | null>);
  private readonly householdService = inject(HouseholdService);

  protected readonly data = inject<JoinHouseholdDialogData>(MAT_DIALOG_DATA);
  protected readonly joining = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected async confirm(): Promise<void> {
    this.joining.set(true);
    this.errorMessage.set(null);

    try {
      const household = await this.householdService.join(this.data.invitationId);
      this.dialogRef.close(household.id);
    } catch (error) {
      this.errorMessage.set(this.toJoinMessage(error));
      this.joining.set(false);
    }
  }

  protected cancel(): void {
    this.dialogRef.close(null);
  }

  private toJoinMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict) {
      return 'Du bist diesem Haushalt bereits beigetreten.';
    }
    return 'Beitritt fehlgeschlagen. Bitte später erneut versuchen.';
  }
}
