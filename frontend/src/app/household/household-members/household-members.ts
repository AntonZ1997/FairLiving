import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { HouseholdService } from '../household-service';
import { HouseholdMemberResponse, HouseholdResponse } from '../household.model';
import { InvitationLinkDialog } from '../household-dashboard/invitation-link-dialog/invitation-link-dialog';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { ConfirmDialog, ConfirmDialogData } from '../../misc/confirm-dialog/confirm-dialog';
import { firstValueFrom } from 'rxjs';

@Component({
  imports: [
    DatePipe,
    RouterLink,
    MatTableModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinner,
  ],
  selector: 'app-household-members',
  styleUrl: './household-members.scss',
  templateUrl: './household-members.html',
})
export class HouseholdMembers implements OnInit {
  readonly householdId = input.required<string>();

  private readonly householdService = inject(HouseholdService);
  private readonly snackbar = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly household = signal<HouseholdResponse | null>(null);
  protected readonly members = signal<HouseholdMemberResponse[]>([]);
  protected readonly loading = signal(true);

  protected readonly isAdmin = computed(
    () => this.members().find((member) => member.isCurrentUser)?.role === 'admin',
  );

  protected readonly displayedColumns = computed(() =>
    this.isAdmin()
      ? ['userName', 'role', 'levelNumber', 'levelTitle', 'joined', 'actions']
      : ['userName', 'role', 'levelNumber', 'levelTitle', 'joined'],
  );

  async ngOnInit(): Promise<void> {
    await this.reload();
  }

  protected openInvitationDialog(): void {
    const household = this.household();

    if (!household) {
      return;
    }

    this.dialog.open<InvitationLinkDialog, string>(InvitationLinkDialog, {
      data: `${window.location.origin}/join/${household.invitationId}`,
      width: '640px',
    });
  }

  protected async removeMember(member: HouseholdMemberResponse): Promise<void> {
    const dialogRef = this.dialog.open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
      data: {
        title: 'Mitglied entfernen',
        message:
          `${member.userName} wird aus dem Haushalt entfernt. ` +
          'Offene Aufgaben werden automatisch neu verteilt.',
        confirmLabel: 'Entfernen',
        delete: true,
      },
      width: '420px',
    });

    const confirmed = await firstValueFrom(dialogRef.afterClosed());

    if (!confirmed) {
      return;
    }

    try {
      await this.householdService.removeMember(this.householdId(), member.memberId);
      this.snackbar.success(`${member.userName} wurde aus dem Haushalt entfernt.`);
      await this.reload();
    } catch {
      this.snackbar.error('Mitglied konnte nicht entfernt werden.');
    }
  }

  protected roleLabel(role: HouseholdMemberResponse['role']): string {
    return role === 'admin' ? 'Admin' : 'Mitglied';
  }

  private async reload(): Promise<void> {
    this.loading.set(true);

    try {
      const [household, members] = await Promise.all([
        this.householdService.findById(this.householdId()),
        this.householdService.findMembers(this.householdId()),
      ]);

      this.household.set(household);
      this.members.set(members);
    } catch {
      this.snackbar.error('Mitglieder konnten nicht geladen werden.');
      await this.router.navigate(['/households']);
    } finally {
      this.loading.set(false);
    }
  }
}
