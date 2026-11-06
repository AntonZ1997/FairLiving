import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatError, MatFormField, MatHint, MatInput, MatLabel } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { SnackbarService } from '../../snackbar/snackbar-service';

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/;
@Component({
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormField,
    MatLabel,
    MatError,
    MatButton,
    MatInput,
    MatHint,
  ],
  selector: 'app-register',
  styleUrl: './register.scss',
  templateUrl: './register.html',
})
export class Register {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly formBuilder = new FormBuilder();
  private readonly snackbar = inject(SnackbarService);
  private readonly redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');

  protected readonly invitationActive = this.redirectTo?.includes('join=') ?? false;
  protected readonly loginQueryParameters = this.redirectTo ? { redirectTo: this.redirectTo } : {};
  protected readonly submitting = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    userName: ['', [Validators.required]],
  });

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    this.submitting.set(true);

    try {
      const request = this.form.getRawValue();
      await this.authService.register(request);
      await this.authService.login({ email: request.email, password: request.password });
      await this.router.navigateByUrl(this.goalUrl());
    } catch (error) {
      this.snackbar.error(this.toMessage(error));
    } finally {
      this.submitting.set(false);
    }
  }

  private goalUrl(): string {
    return this.redirectTo?.startsWith('/') && !this.redirectTo.startsWith('//') ? this.redirectTo : '/households';
  }

  private toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict) {
      return 'Die E-Mail Adresse ist bereits registriert.';
    }
    return 'Registrierung fehlgeschlagen. Versuche es erneut.';
  }
}

