import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatInput, MatLabel, MatPrefix, MatSuffix } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom } from 'rxjs';
import { HouseholdService } from '../../household/household-service';
import { HouseholdResponse } from '../../household/household.model';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { TaskService } from '../task-service';
import { AssignedTaskResponse, TaskListItemResponse } from '../task.model';
import { CreateTaskDialog } from '../create-task-dialog/create-task-dialog';
import { TaskDetailDialog } from '../task-detail-dialog/task-detail-dialog';
import { isOverdue } from '../due-date';
import { MatSort, MatSortHeader, Sort } from '@angular/material/sort';

type TaskFilter = 'open' | 'paused';

@Component({
  imports: [
    DatePipe,
    RouterLink,
    MatTableModule,
    MatButton,
    MatIconButton,
    MatIcon,
    MatFormField,
    MatLabel,
    MatInput,
    MatPrefix,
    MatSuffix,
    MatSelect,
    MatOption,
    MatProgressSpinner,
    MatSort,
    MatSortHeader,
  ],
  selector: 'app-task-list',
  styleUrl: './task-list.scss',
  templateUrl: './task-list.html',
})
export class TaskList implements OnInit {
  readonly householdId = input.required<string>();

  private readonly householdService = inject(HouseholdService);
  private readonly taskService = inject(TaskService);
  private readonly snackbar = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly household = signal<HouseholdResponse | null>(null);
  protected readonly tasks = signal<TaskListItemResponse[]>([]);
  protected readonly filter = signal<TaskFilter>('open');
  protected readonly searchTerm = signal('');
  protected readonly loading = signal(true);
  protected readonly sort = signal<Sort>({ active: 'dueDate', direction: 'asc' });

  protected readonly isOverdue = isOverdue;

  protected readonly displayedColumns = [
    'name',
    'difficultyName',
    'dueDate',
    'intervalDays',
    'assignedMemberName',
  ];

  protected readonly visibleTasks = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();

    const filtered = term
      ? this.tasks().filter(
          (task) =>
            task.name.toLowerCase().includes(term) ||
            (task.assignedMemberName?.toLowerCase().includes(term) ?? false),
        )
      : this.tasks();

    return this.sortTasks(filtered);
  });

  private sortTasks(tasks: TaskListItemResponse[]): TaskListItemResponse[] {
    const { active, direction } = this.sort();

    if (!direction) {
      return tasks;
    }

    const factor = direction === 'asc' ? 1 : -1;

    return [...tasks].sort((left, right) => {
      const leftValue = this.sortValue(left, active);
      const rightValue = this.sortValue(right, active);

      if (leftValue === null || rightValue === null) {
        return leftValue === rightValue ? 0 : leftValue === null ? 1 : -1;
      }

      return typeof leftValue === 'number' && typeof rightValue === 'number'
        ? factor * (leftValue - rightValue)
        : factor * String(leftValue).localeCompare(String(rightValue), 'de');
    });
  }

  private sortValue(task: TaskListItemResponse, column: string): string | number | null {
    switch (column) {
      case 'name':
        return task.name;
      case 'difficultyName':
        return task.difficultyWeight;
      case 'dueDate':
        return task.dueDate ? new Date(task.dueDate).getTime() : null;
      case 'intervalDays':
        return task.intervalDays;
      case 'assignedMemberName':
        return task.assignedMemberName;
      default:
        return null;
    }
  }

  protected readonly emptyMessage = computed(() => {
    if (this.searchTerm().trim()) {
      return 'Keine Aufgabe passt zu deiner Suche.';
    }

    return this.filter() === 'paused'
      ? 'In diesem Haushalt gibt es keine pausierten Aufgaben.'
      : 'In diesem Haushalt gibt es keine offenen Aufgaben.';
  });

  async ngOnInit(): Promise<void> {
    try {
      this.household.set(await this.householdService.findById(this.householdId()));
    } catch {
      this.snackbar.error('Dieser Haushalt existiert nicht oder du bist kein Mitglied.');
      await this.router.navigate(['/households']);
      return;
    }

    await this.reload();
  }

  protected async changeFilter(filter: TaskFilter): Promise<void> {
    this.filter.set(filter);
    await this.reload();
  }

  protected async openTaskDetails(taskId: string): Promise<void> {
    const dialogRef = this.dialog.open<TaskDetailDialog, string>(TaskDetailDialog, {
      data: taskId,
      width: '520px',
    });

    await firstValueFrom(dialogRef.afterClosed());
    await this.reload();
  }

  protected async openCreateTaskDialog(): Promise<void> {
    const dialogRef = this.dialog.open<CreateTaskDialog, string, AssignedTaskResponse | null>(
      CreateTaskDialog,
      { data: this.householdId(), width: '520px' },
    );

    const task = await firstValueFrom(dialogRef.afterClosed());

    if (!task) {
      return;
    }

    this.snackbar.success(`Aufgabe wurde erstellt und an ${task.assignedMemberName} vergeben.`);
    this.filter.set('open');
    await this.reload();
  }

  protected intervalLabel(intervalDays: number): string {
    return intervalDays > 0 ? `alle ${intervalDays} Tage` : 'einmalig';
  }

  private async reload(): Promise<void> {
    this.loading.set(true);

    try {
      this.tasks.set(
        await this.taskService.findTasks(this.householdId(), this.filter() === 'open'),
      );
    } catch {
      this.snackbar.error('Aufgaben konnten nicht geladen werden.');
    } finally {
      this.loading.set(false);
    }
  }
}
