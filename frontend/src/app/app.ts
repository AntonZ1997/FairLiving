import { Component, inject, signal } from '@angular/core';
import {  Router, RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';
import {
  clearLastHouseholdId,
} from './household/household-selection-service';
import { MatToolbar } from '@angular/material/toolbar';
import { MatIcon } from '@angular/material/icon';
import { MatButton } from '@angular/material/button';
import { MatMenu, MatMenuItem, MatMenuTrigger } from '@angular/material/menu';


@Component({
  imports: [RouterOutlet, MatToolbar, MatIcon, MatButton, MatMenuTrigger, MatMenu, MatMenuItem],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly currentUser = this.authService.currentUser;
  protected readonly isLoggedIn = this.authService.isLoggedIn;
  protected readonly showSwitchHousehold = signal(false);

  protected onMenuOpened(): void {
    this.showSwitchHousehold.set(/^\/households\/[^/]+/.test(this.router.url));
  }

  protected async switchHousehold(): Promise<void> {
    await this.router.navigate(['/households']);
  }

  protected async logout(): Promise<void> {
    clearLastHouseholdId();

    try {
      await this.authService.logout();
    } finally {
      await this.router.navigate(['/login']);
    }
  }
}
