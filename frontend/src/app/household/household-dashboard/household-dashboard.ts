import { Component, computed, inject, input, signal } from '@angular/core';
import { HouseholdService } from '../household-service';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { Router } from '@angular/router';
import { HouseholdMemberResponse, HouseholdResponse, HouseholdRole } from '../household.model';
import { clearLastHouseholdId, setLastHouseholdId } from '../household-selection-service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatButton, MatFabButton } from '@angular/material/button';
import { MatProgressBar } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { InvitationLinkDialog } from './invitation-link-dialog/invitation-link-dialog';
import { AssignedTaskResponse } from '../../task/task.model';
import { CreateTaskDialog } from '../../task/create-task-dialog/create-task-dialog';
import { firstValueFrom } from 'rxjs';
import { TaskService } from '../../task/task-service';

@Component({
  imports: [
    MatProgressSpinner,
    MatCard,
    MatCardContent,
    MatButton,
    MatProgressBar,
    MatIcon,
    MatFabButton,
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

  protected readonly household = signal<HouseholdResponse | null>(null);
  protected readonly members = signal<HouseholdMemberResponse[]>([]);
  protected readonly openTasks = signal<AssignedTaskResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly topMembers = computed(() => this.members().slice(0, 3));

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

  protected isOverdue(dueDate: string): boolean {
    return new Date(dueDate).getTime() < Date.now();
  }

  protected dueLabel(dueDate: string): string {
    const days = this.calendarDaysUntil(dueDate);

    if (days === 0) {
      return this.hourLabel(dueDate);
    }

    const absoluteDays = Math.abs(days);
    const unit = absoluteDays === 1 ? 'Tag' : 'Tagen';

    return this.isOverdue(dueDate)
      ? `Fällig seit ${absoluteDays} ${unit}`
      : `Fällig in ${absoluteDays} ${unit}`;
  }

  private hourLabel(dueDate: string): string {
    const differenceMilliseconds = new Date(dueDate).getTime() - Date.now();
    const hours = Math.floor(Math.abs(differenceMilliseconds) / 3_600_000);
    const unit = hours === 1 ? 'Stunde' : 'Stunden';

    if (differenceMilliseconds < 0) {
      return hours === 0
        ? 'Seit weniger als einer Stunde überfällig'
        : `Überfällig seit ${hours} ${unit}`;
    }

    return hours === 0 ? 'Fällig in weniger als einer Stunde' : `Fällig in ${hours} ${unit}`;
  }

  private calendarDaysUntil(dueDate: string): number {
    const due = new Date(dueDate);
    const dueDay = new Date(due.getFullYear(), due.getMonth(), due.getDate());
    const today = new Date();
    const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    return Math.round((dueDay.getTime() - todayDay.getTime()) / 86_400_000);
  }

  protected roleLabel(role: HouseholdRole): string {
    return role === 'admin' ? 'Admin' : 'Mitglied';
  }
}
