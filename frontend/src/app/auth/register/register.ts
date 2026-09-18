import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatError, MatFormField, MatHint, MatInput, MatLabel } from '@angular/material/input';
import { MatButton } from '@angular/material/button';

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
  private readonly formBuilder = new FormBuilder();

  protected errorMessage = signal<string | null>(null);
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
    this.errorMessage.set(null);

    try {
      const request = this.form.getRawValue();
      await this.authService.register(request);
      await this.authService.login({ email: request.email, password: request.password });
      await this.router.navigate(['/']);
    } catch (error) {
      this.errorMessage.set(this.toMessage(error));
    } finally {
      this.submitting.set(false);
    }
  }

  private toMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict) {
      return 'Die E-Mail Adresse ist bereits registriert.';
    }
    return 'Registrierung fehlgeschlagen. Versuchen Sie es erneut.';
  }
}

