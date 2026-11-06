import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HouseholdService } from '../household-service';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { Router, RouterLink } from '@angular/router';
import { HouseholdMemberResponse, HouseholdResponse, HouseholdRole } from '../household.model';
import { clearLastHouseholdId, setLastHouseholdId } from '../household-selection-service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatButton, MatFabButton } from '@angular/material/button';
import { MatBadge } from '@angular/material/badge';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { InvitationLinkDialog } from './invitation-link-dialog/invitation-link-dialog';
import { AssignedTaskResponse } from '../../task/task.model';
import { CreateTaskDialog } from '../../task/create-task-dialog/create-task-dialog';
import { firstValueFrom } from 'rxjs';
import { TaskService } from '../../task/task-service';
import { dueLabel, isOverdue } from '../../task/due-date';
import { TaskDetailDialog } from '../../task/task-detail-dialog/task-detail-dialog';
import { NotificationService } from '../../notification/notification-service';
import { NotificationResponse } from '../../notification/notification.model';
import { NotificationDialog } from '../../notification/notification-dialog/notification-dialog';

@Component({
  imports: [
    MatProgressSpinner,
    MatCard,
    MatCardContent,
    MatButton,
    MatProgressBar,
    MatIcon,
    MatFabButton,
    MatBadge,
    RouterLink,
  ],
  selector: 'app-household-dashboard',
  styleUrl: './household-dashboard.scss',
  templateUrl: './household-dashboard.html',
})
export class HouseholdDashboard {
  readonly householdId = input.required<string>();

  private readonly householdService = inject(HouseholdService);
  private readonly snackbar = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly taskService = inject(TaskService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly household = signal<HouseholdResponse | null>(null);
  protected readonly members = signal<HouseholdMemberResponse[]>([]);
  protected readonly openTasks = signal<AssignedTaskResponse[]>([]);
  protected readonly notifications = signal<NotificationResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly topMembers = computed(() => this.members().slice(0, 3));
  protected readonly newNotificationCount = computed(() => this.notifications().length);
  protected readonly isOverdue = isOverdue;
  protected readonly dueLabel = dueLabel;
  protected readonly me = computed(
    () => this.members().find((member) => member.isCurrentUser) ?? null,
  );

  protected readonly levelProgress = computed(() => {
    const me = this.me();

    if (!me || me.nextLevelRequiredXp === null) {
      return 100;
    }

    const span = me.nextLevelRequiredXp - me.currentLevelRequiredXp;
    return span > 0
      ? Math.round(((me.experiencePoints - me.currentLevelRequiredXp) / span) * 100)
      : 100;
  });

  protected readonly openTasksHeadline = computed(() => {
    const count = this.openTasks().length;

    if (count === 0) {
      return 'Du hast keine offenen Aufgaben';
    }

    return count === 1 ? 'Du hast 1 offene Aufgabe' : `Du hast ${count} offene Aufgaben`;
  });

  async ngOnInit(): Promise<void> {
    try {
      const [household, members, openTasks] = await Promise.all([
        this.householdService.findById(this.householdId()),
        this.householdService.findMembers(this.householdId()),
        this.taskService.findOpenTasks(this.householdId()),
      ]);

      this.household.set(household);
      this.members.set(members);
      this.openTasks.set(openTasks);
      setLastHouseholdId(this.householdId());

      this.notificationService
        .streamNotifications(this.householdId())
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((notifications) => this.notifications.set(notifications));
    } catch {
      clearLastHouseholdId();
      this.snackbar.error('Dieser Haushalt existiert nicht oder du bist kein Mitglied.');
      await this.router.navigate(['/households']);
    } finally {
      this.loading.set(false);
    }
  }

  protected openInvitationDialog(): void {
    const household = this.household();

    if (!household) {
      return;
    }

    this.dialog.open<InvitationLinkDialog, string>(InvitationLinkDialog, {
      data: `${window.location.origin}/join/${household.invitationId}`,
      width: '600px',
    });
  }

  protected async openNotificationDialog(): Promise<void> {
    const shown = this.notifications(); // Angezeigte Benachrichtigungen werden als gelesen markiert

    const dialogRef = this.dialog.open<NotificationDialog, NotificationResponse[]>(
      NotificationDialog,
      { data: shown, width: '560px' },
    );

    if (shown.length > 0) {
      const shownIds = shown.map((notification) => notification.notificationId);

      this.notifications.update((current) =>
        current.filter((notification) => !shownIds.includes(notification.notificationId)),
      );

      try {
        await this.notificationService.readNotifications(this.householdId(), shownIds);
      } catch {
        this.snackbar.error('Benachrichtigungen konnten nicht als gelesen markiert werden.');
      }
    }

    await firstValueFrom(dialogRef.afterClosed());
  }

  protected async openCreateTaskDialog(): Promise<void> {
    const dialogRef = this.dialog.open<CreateTaskDialog, string, AssignedTaskResponse | null>(
      CreateTaskDialog,
      { data: this.householdId(), width: '520px' },
    );

    const task = await firstValueFrom(dialogRef.afterClosed());

    if (task) {
      this.snackbar.success(`Aufgabe wurde erstellt und an ${task.assignedMemberName} vergeben.`);
      this.openTasks.set(await this.taskService.findOpenTasks(this.householdId()));
    }
  }

  protected async openTaskDetails(taskId: string): Promise<void> {
    const dialogRef = this.dialog.open<TaskDetailDialog, string>(TaskDetailDialog, {
      data: taskId,
      width: '520px',
    });

    await firstValueFrom(dialogRef.afterClosed());
    await this.reloadTasksAndMembers();
  }

  private async reloadTasksAndMembers(): Promise<void> {
    const [openTasks, members] = await Promise.all([
      this.taskService.findOpenTasks(this.householdId()),
      this.householdService.findMembers(this.householdId()),
    ]);

    this.openTasks.set(openTasks);
    this.members.set(members);
  }

  protected roleLabel(role: HouseholdRole): string {
    return role === 'admin' ? 'Admin' : 'Mitglied';
  }
}
