import { Component, inject } from '@angular/core';
import { MatButton } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel: string;
  delete?: boolean;
}

@Component({
  imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton],
  selector: 'app-confirm-dialog',
  styleUrl: './confirm-dialog.scss',
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  private readonly dialogRef = inject(MatDialogRef<ConfirmDialog, boolean>);

  protected readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);

  protected confirm(): void {
    this.dialogRef.close(true);
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }
}
