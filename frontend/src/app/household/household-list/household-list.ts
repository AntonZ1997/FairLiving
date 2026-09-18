import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HouseholdService } from '../household-service';
import {
  HouseholdPreviewResponse,
  HouseholdResponse,
  HouseholdSummaryResponse,
} from '../household.model';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { MatButton, MatFabButton } from '@angular/material/button';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from '@angular/material/card';
import { MatChip } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { extractInvitationId } from '../invitationId';
import {
  JoinHouseholdDialog,
  JoinHouseholdDialogData,
} from '../join-household-dialog/join-household-dialog';
import { firstValueFrom } from 'rxjs';
import { SnackbarService } from '../../snackbar/snackbar-service';
import { CreateHouseholdDialog } from '../create-household-dialog/create-household-dialog';

@Component({
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatError,
    MatInput,
    MatButton,
    RouterLink,
    MatIcon,
    MatFabButton,
    MatProgressSpinner,
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardContent,
    MatChip,
  ],
  selector: 'app-household-list',
  styleUrl: './household-list.scss',
  templateUrl: './household-list.html',
})
export class HouseholdList implements OnInit {
  private readonly householdService = inject(HouseholdService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(SnackbarService);

  protected readonly households = signal<HouseholdSummaryResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showCreateForm = signal(false);
  protected readonly checkingInvitation = signal(false);
  protected readonly joinErrorMessage = signal<string | null>(null);

  protected readonly createForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
  });

  protected readonly joinForm = this.formBuilder.nonNullable.group({
    invitationId: ['', Validators.required],
  });

  async ngOnInit(): Promise<void> {
    await this.reload();

    const invitationId = this.route.snapshot.queryParamMap.get('join');

    if (invitationId) {
      await this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
      await this.startJoin(invitationId);
    }
  }

  protected async create(): Promise<void> {
    if (this.createForm.invalid) {
      return;
    }

    this.errorMessage.set(null);

    try {
      await this.householdService.create(this.createForm.getRawValue().name);
      this.createForm.reset();
      this.showCreateForm.set(false);
      await this.reload();
    } catch {
      this.errorMessage.set('Haushalt konnte nicht angelegt werden.');
    }
  }

  protected async join(): Promise<void> {
    if (this.joinForm.invalid) {
      return;
    }

    await this.startJoin(extractInvitationId(this.joinForm.getRawValue().invitationId));
  }

  protected openTaskLabel(count: number): string {
    return count === 1 ? '1 offene Aufgabe' : `${count} offene Aufgaben`;
  }

  protected memberLabel(count: number): string {
    return count === 1 ? '1 Mitglied' : `${count} Mitglieder`;
  }

  protected roleLabel(role: HouseholdSummaryResponse['role']): string {
    return role === 'admin' ? 'Admin' : 'Mitglied';
  }

  private async startJoin(invitationId: string): Promise<void> {
    this.checkingInvitation.set(true);
    this.joinErrorMessage.set(null);

    try {
      const preview = await this.householdService.preview(invitationId);

      if (preview.alreadyMember) {
        this.setJoinError('Du bist diesem Haushalt bereits beigetreten.');
        return;
      }

      await this.openJoinDialog(invitationId, preview);
    } catch (error) {
      this.setJoinError(this.toPreviewMessage(error));
    } finally {
      this.checkingInvitation.set(false);
    }
  }

  private async openJoinDialog(
    invitationId: string,
    preview: HouseholdPreviewResponse,
  ): Promise<void> {
    const dialogRef = this.dialog.open<JoinHouseholdDialog, JoinHouseholdDialogData, string | null>(
      JoinHouseholdDialog,
      { data: { invitationId, preview }, width: '420px' },
    );

    const householdId = await firstValueFrom(dialogRef.afterClosed());

    if (householdId) {
      this.joinForm.reset();
      this.snackBar.success(`Du bist „${preview.name}" beigetreten.`);
      await this.router.navigate(['/households', householdId]);
    }
  }

  private setJoinError(message: string): void {
    this.joinErrorMessage.set(message);
    this.joinForm.controls.invitationId.setErrors({ server: true });
    this.joinForm.controls.invitationId.markAsTouched();
  }

  private toPreviewMessage(error: unknown): string {
    if (
      error instanceof HttpErrorResponse &&
      (error.status === HttpStatusCode.NotFound || error.status === HttpStatusCode.BadRequest)
    ) {
      return 'Dieser Einladungslink ist ungültig.';
    }
    return 'Der Haushalt konnte nicht geladen werden.';
  }

  private async reload(): Promise<void> {
    this.loading.set(true);

    try {
      this.households.set(await this.householdService.findMine());
    } catch {
      this.errorMessage.set('Haushalte konnten nicht geladen werden.');
    } finally {
      this.loading.set(false);
    }
  }

  protected async openCreateDialog(): Promise<void> {
    const dialogRef = this.dialog.open<CreateHouseholdDialog, undefined, HouseholdResponse | null>(
      CreateHouseholdDialog,
      { width: '420px' },
    );

    const household = await firstValueFrom(dialogRef.afterClosed());

    if (household) {
      this.snackBar.success(`Haushalt „${household.name}" wurde angelegt.`);
      await this.reload();
    }
  }
}
