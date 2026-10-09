import { AbstractControl, ValidationErrors } from '@angular/forms';


export function isOverdue(dueDate: string): boolean {
  return new Date(dueDate).getTime() < Date.now();
}

export function dueLabel(dueDate: string): string {
  const days = calendarDaysUntil(dueDate);

  if (days === 0) {
    return hourLabel(dueDate);
  }

  const absoluteDays = Math.abs(days);
  const unit = absoluteDays === 1 ? 'Tag' : 'Tagen';

  return isOverdue(dueDate)
    ? `Fällig seit ${absoluteDays} ${unit}`
    : `Fällig in ${absoluteDays} ${unit}`;
}

export function hourLabel(dueDate: string): string {
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

export function calendarDaysUntil(dueDate: string): number {
  const due = new Date(dueDate);
  const dueDay = new Date(due.getFullYear(), due.getMonth(), due.getDate());
  const today = new Date();
  const todayDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());

  return Math.round((dueDay.getTime() - todayDay.getTime()) / 86_400_000);
}

export function combineDateAndTime(date: Date, time: Date): Date {
  const combined = new Date(date);
  combined.setHours(time.getHours(), time.getMinutes(), 0, 0);
  return combined;
}

export function dueInFuture(group: AbstractControl): ValidationErrors | null {
  const date = group.get('dueDate')?.value as Date | null;
  const time = group.get('dueTime')?.value as Date | null;

  if (!date || !time) {
    return null;
  }

  return combineDateAndTime(date, time).getTime() > Date.now() ? null : { dueInPast: true };
}
