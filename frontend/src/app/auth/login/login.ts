import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../auth.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButton } from '@angular/material/button';
import { SnackbarService } from '../../snackbar/snackbar-service';

@Component({
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButton],
  selector: 'app-login',
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly formBuilder = new FormBuilder();
  private readonly snackbar = inject(SnackbarService);
  private readonly redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');

  protected readonly invitationActive = this.redirectTo?.includes('join=') ?? false;
  protected readonly registerQueryParameters = this.redirectTo
    ? { redirectTo: this.redirectTo }
    : {};
  protected readonly submitting = signal(false);

  protected readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      return;
    }

    this.submitting.set(true);

    try {
      await this.authService.login(this.form.getRawValue());
      await this.router.navigateByUrl(this.goalUrl());
    } catch (error) {
      this.snackbar.error(this.toMessage(error));
    } finally {
      this.submitting.set(false);
    }
  }

  private goalUrl(): string {
    return this.redirectTo?.startsWith('/') && !this.redirectTo.startsWith('//')
      ? this.redirectTo
      : '/households';
  }

  private toMessage(error: unknown) {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
      return 'E-Mail oder Password ist falsch.';
    }
    return 'Anmeldung fehlgeschlagen';
  }
}
