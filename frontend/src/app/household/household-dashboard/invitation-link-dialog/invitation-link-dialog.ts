import { Component, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { SnackbarService } from '../../../snackbar/snackbar-service';
import { MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';

@Component({
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatFormField,
    MatLabel,
    MatInput,
    MatDialogActions,
    MatButton,
    MatIcon,
    MatIconButton,
  ],
  selector: 'app-invitation-link-dialog',
  styleUrl: './invitation-link-dialog.scss',
  templateUrl: './invitation-link-dialog.html',
})
export class InvitationLinkDialog {
  private readonly dialogRef = inject(MatDialogRef<InvitationLinkDialog>);
  private readonly snackbar = inject(SnackbarService);

  protected readonly invitationLink = inject<string>(MAT_DIALOG_DATA);

  protected async copy(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.invitationLink);
      this.snackbar.success('Einladungslink kopiert.');
    } catch {
      this.snackbar.error('Kopieren fehlgeschlagen.');
    }
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
