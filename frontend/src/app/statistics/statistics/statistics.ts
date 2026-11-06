import {
  Component,
  ElementRef,
  OnInit,
  computed,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButton } from '@angular/material/button';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerToggle,
} from '@angular/material/datepicker';
import { MatIcon } from '@angular/material/icon';
import { MatFormField, MatInput, MatLabel, MatSuffix } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatOption, MatSelect } from '@angular/material/select';
import { Chart, ChartConfiguration, ChartOptions, registerables } from 'chart.js';
import { HouseholdService } from '../../household/household-service';
import { HouseholdResponse } from '../../household/household.model';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { StatisticsService } from '../statistics-service';
import {
  CompletionShareResponse,
  CumulatedExperiencePoints,
  HouseholdMemberCompletionCount,
  HouseholdMemberWorkloadShare,
} from '../statistics.model';

Chart.register(...registerables);

type chartOption = 'completedTasks' | 'completionShares' | 'experiencePoints' | 'workload';

type ExperiencePoint = { x: number; y: number };

type StatisticsChart =
  | Chart<'bar', number[], string>
  | Chart<'doughnut', number[], string>
  | Chart<'line', ExperiencePoint[], unknown>;

const DEFAULT_RANGE_DAYS = 14;

const COLOR_PRIMARY = '#1668c5';
const COLOR_PRIMARY_SOFT = 'rgb(20 96 182 / 0.15)';
const COLOR_ON_TIME = '#318535';
const COLOR_LATE = '#d82b2b';
const COLOR_OPEN = '#a5a5a5';

function getBeginningOfADay(date: Date): Date {
  const result = new Date(date);
  result.setHours(0, 0, 0, 0);
  return result;
}

function getEndingOfADay(date: Date): Date {
  const result = new Date(date);
  result.setHours(23, 59, 59, 999);
  return result;
}

function checkValidDateRange(group: AbstractControl): ValidationErrors | null {
  const from = group.get('from')?.value as Date | null;
  const to = group.get('to')?.value as Date | null;

  if (!from || !to) {
    return null;
  }

  if (getBeginningOfADay(from).getTime() > getBeginningOfADay(to).getTime()) {
    return { rangeReversed: true };
  }

  if (getBeginningOfADay(to).getTime() > getBeginningOfADay(new Date()).getTime()) {
    return { endInFuture: true };
  }

  return null;
}

@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButton,
    MatIcon,
    MatFormField,
    MatLabel,
    MatInput,
    MatSuffix,
    MatSelect,
    MatOption,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatProgressSpinner,
  ],
  selector: 'app-statistics',
  styleUrl: './statistics.scss',
  templateUrl: './statistics.html',
})
export class Statistics implements OnInit {
  readonly householdId = input.required<string>();

  private readonly householdService = inject(HouseholdService);
  private readonly statisticsService = inject(StatisticsService);
  private readonly snackbar = inject(SnackbarService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  private readonly chartCanvas = viewChild<ElementRef<HTMLCanvasElement>>('chartCanvas');
  private readonly householdMemberCompletionCounts = signal<HouseholdMemberCompletionCount[]>([]);
  private readonly completionShares = signal<CompletionShareResponse | null>(null);
  private readonly cumulatedExperiencePoints = signal<CumulatedExperiencePoints[]>([]);
  private readonly householdMemberWorkloadShare = signal<HouseholdMemberWorkloadShare[]>([]);

  private readonly completionOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true, ticks: { precision: 0 } },
    },
    plugins: { legend: { display: false } },
  };

  private readonly workloadOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true },
    },
    plugins: { legend: { display: false } },
  };

  private readonly shareOptions: ChartOptions<'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    radius: '75%',
    cutout: '60%',
    layout: { padding: { top: 23, bottom: 7 } },
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          font: { size: 16 },
          boxWidth: 18,
          boxHeight: 18,
          padding: 24,
        },
      },
      tooltip: {
        callbacks: {
          label: (item) => {
            const total = this.shareTotal();
            const value = Number(item.parsed);
            const share = total > 0 ? Math.round((value / total) * 100) : 0;
            return `${item.label}: ${value} (${share} %)`;
          },
        },
      },
    },
  };

  private readonly experienceOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: 'linear',
        ticks: {
          maxTicksLimit: 8,
          callback: (value) => new Date(Number(value)).toLocaleDateString('de-DE'),
        },
      },
      y: { beginAtZero: true, title: { display: true, text: 'Experience Points' } },
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          title: (items) => new Date(Number(items[0].parsed.x)).toLocaleString('de-DE'),
          label: (item) => `${Number(item.parsed.y)} XP`,
        },
      },
    },
  };
  protected readonly household = signal<HouseholdResponse | null>(null);
  protected readonly loading = signal(false);
  protected readonly today = new Date();

  protected readonly evaluations: { value: chartOption; label: string }[] = [
    { value: 'completedTasks', label: 'Erledigte Aufgaben pro Haushaltsmitglied' },
    { value: 'completionShares', label: 'Deine pünktlichen, verspäteten und offenen Aufgaben' },
    { value: 'experiencePoints', label: 'Verlauf deiner Experience Points' },
    { value: 'workload', label: 'Aktuelle Workload-Verteilung' },
  ];

  protected readonly form = this.formBuilder.nonNullable.group(
    {
      evaluation: this.formBuilder.nonNullable.control<chartOption>(
        'completedTasks',
        Validators.required,
      ),
      from: this.formBuilder.nonNullable.control<Date | null>(
        new Date(Date.now() - DEFAULT_RANGE_DAYS * 86400000),
        Validators.required,
      ),
      to: this.formBuilder.nonNullable.control<Date | null>(new Date(), Validators.required),
    },
    { validators: checkValidDateRange },
  );

  protected readonly evaluation = toSignal(this.form.controls.evaluation.valueChanges, {
    initialValue: this.form.controls.evaluation.value,
  });

  protected readonly hasData = computed(() => {
    switch (this.evaluation()) {
      case 'completedTasks':
        return this.householdMemberCompletionCounts().some((entry) => entry.completedCount > 0);
      case 'completionShares':
        return this.shareTotal() > 0;
      case 'experiencePoints':
        return this.cumulatedExperiencePoints().length > 0;
      case 'workload':
        return this.householdMemberWorkloadShare().length > 0;
    }
  });

  protected readonly chartTitle = computed(() => {
    switch (this.evaluation()) {
      case 'completedTasks':
        return 'Erledigte Aufgaben pro Haushaltsmitglied';
      case 'completionShares':
        return 'Deine Aufgaben im Zeitraum';
      case 'experiencePoints':
        return 'Verlauf deiner kumulierten Experience Points';
      case 'workload':
        return 'Aktuelle Workload-Verteilung';
    }
  });

  protected readonly emptyMessage = computed(() => {
    switch (this.evaluation()) {
      case 'completedTasks':
        return 'In diesem Zeitraum wurde keine Aufgabe erledigt.';
      case 'completionShares':
        return 'Du hast in diesem Zeitraum keine Aufgabe erledigt und auch keine offenen Aufgaben.';
      case 'experiencePoints':
        return 'Du hast in diesem Zeitraum keine Aufgabe abgeschlossen.';
      case 'workload':
        return 'Dieser Haushalt hat keine Mitglieder.';
    }
  });

  private readonly shareTotal = computed(() => {
    const shares = this.completionShares();
    return shares ? shares.onTime + shares.late + shares.open : 0;
  });

  private readonly completionConfig = computed<ChartConfiguration<'bar', number[], string>>(() => ({
    type: 'bar',
    data: {
      labels: this.householdMemberCompletionCounts().map((entry) => entry.householdMemberName),
      datasets: [
        {
          label: 'Erledigte Aufgaben',
          data: this.householdMemberCompletionCounts().map((entry) => entry.completedCount),
          backgroundColor: COLOR_PRIMARY,
          borderRadius: 5,
          maxBarThickness: 65,
        },
      ],
    },
    options: this.completionOptions,
  }));

  private readonly workloadConfig = computed<ChartConfiguration<'bar', number[], string>>(() => ({
    type: 'bar',
    data: {
      labels: this.householdMemberWorkloadShare().map((entry) => entry.householdMemberName),
      datasets: [
        {
          label: 'Workload',
          data: this.householdMemberWorkloadShare().map((entry) => entry.workload),
          backgroundColor: COLOR_PRIMARY,
          borderRadius: 5,
          maxBarThickness: 65,
        },
      ],
    },
    options: this.workloadOptions,
  }));

  private readonly shareConfig = computed<ChartConfiguration<'doughnut', number[], string>>(() => {
    const shares = this.completionShares();

    return {
      type: 'doughnut',
      data: {
        labels: ['Pünktlich erledigt', 'Verspätet erledigt', 'Noch offen'],
        datasets: [
          {
            data: shares ? [shares.onTime, shares.late, shares.open] : [],
            backgroundColor: [COLOR_ON_TIME, COLOR_LATE, COLOR_OPEN],
            borderWidth: 0,
          },
        ],
      },
      options: this.shareOptions,
    };
  });

  private readonly experienceConfig = computed<ChartConfiguration<'line', ExperiencePoint[], unknown>>(
    () => ({
    type: 'line',
    data: {
      datasets: [
        {
          label: 'Kumulierte XP',
          data: this.cumulatedExperiencePoints().map((entry) => ({
            x: new Date(entry.completedAt).getTime(),
            y: entry.cumulatedExperiencePoints,
          })),
          borderColor: COLOR_PRIMARY,
          backgroundColor: COLOR_PRIMARY_SOFT,
          borderWidth: 3,
          pointRadius: 5,
          fill: true,
          tension: 0.3,
        },
      ],
    },
    options: this.experienceOptions,
  }));

  constructor() {
    this.form.valueChanges.pipe(takeUntilDestroyed())
      .subscribe(
        () => void this.load());

    effect((onCleanup) => {
      const canvas = this.chartCanvas();

      if (!canvas) {
        return;
      }

      const chart = this.createChart(canvas.nativeElement);
      onCleanup(() => chart.destroy());
    });
  }

  async ngOnInit(): Promise<void> {
    try {
      this.household.set(await this.householdService.findById(this.householdId()));
    } catch {
      this.snackbar.error('Dieser Haushalt existiert nicht oder du bist kein Mitglied.');
      await this.router.navigate(['/households']);
      return;
    }

    await this.load();
  }

  private createChart(element: HTMLCanvasElement): StatisticsChart {
    switch (this.evaluation()) {
      case 'completedTasks':
        return new Chart(element, this.completionConfig());
      case 'completionShares':
        return new Chart(element, this.shareConfig());
      case 'experiencePoints':
        return new Chart(element, this.experienceConfig());
      case 'workload':
        return new Chart(element, this.workloadConfig());
    }
  }

  private async load(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    const raw = this.form.getRawValue();
    const from = getBeginningOfADay(raw.from!).toISOString();
    const to = new Date(Math.min(getEndingOfADay(raw.to!).getTime(), Date.now())).toISOString();

    this.loading.set(true);

    try {
      switch (raw.evaluation) {
        case 'completedTasks':
          this.householdMemberCompletionCounts.set(
            await this.statisticsService.findHouseholdMemberCompletionCounts(this.householdId(), from, to),
          );
          break;
        case 'completionShares':
          this.completionShares.set(
            await this.statisticsService.findCompletionShares(this.householdId(), from, to),
          );
          break;
        case 'experiencePoints':
          this.cumulatedExperiencePoints.set(
            await this.statisticsService.findCumulatedExperiencePoints(this.householdId(), from, to),
          );
          break;
        case 'workload':
          this.householdMemberWorkloadShare.set(
            await this.statisticsService.findHouseholdMemberWorkloadShares(this.householdId(), from, to),
          );
          break;
      }
    } catch {
      this.snackbar.error('Die Auswertung konnte nicht geladen werden.');
    } finally {
      this.loading.set(false);
    }
  }
}
