import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButton } from '@angular/material/button';

@Component({
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButton],
  selector: 'app-login',
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly errorMessage = signal<string | null>(null);
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
    this.errorMessage.set(null);

    try {
      await this.authService.login(this.form.getRawValue());
      await this.router.navigate(['/households']);
    } catch (error) {
      this.errorMessage.set(this.toMessage(error));
    } finally {
      this.submitting.set(false);
    }
  }

  private toMessage(error: unknown) {
    if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
      return 'E-Mail oder Password ist falsch.';
    }
    return 'Anmeldung fehlgeschlagen';
  }
}
