import { Component, inject } from '@angular/core';
import { MatIconButton } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { NotificationResponse, NotificationType } from '../notification.model';

@Component({
  imports: [MatDialogTitle, MatDialogContent, MatIconButton, MatIcon],
  selector: 'app-notification-dialog',
  styleUrl: './notification-dialog.scss',
  templateUrl: './notification-dialog.html',
})
export class NotificationDialog {
  private readonly dialogRef = inject(MatDialogRef<NotificationDialog>);

  protected readonly notifications = inject<NotificationResponse[]>(MAT_DIALOG_DATA);

  protected title(): string {
    const count = this.notifications.length;

    if (count === 0) {
      return 'Sie haben keine neuen Benachrichtigungen';
    }

    return count === 1
      ? 'Sie haben 1 neue Benachrichtigung'
      : `Sie haben ${count} neue Benachrichtigungen`;
  }

  protected symbol(type: NotificationType): string {
    return type === 'info' ? 'i' : '!';
  }

  protected close(): void {
    this.dialogRef.close();
  }
}
