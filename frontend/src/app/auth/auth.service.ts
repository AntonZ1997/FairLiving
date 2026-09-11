import { Service, signal, inject, computed } from '@angular/core';
import { LoginRequest, RegisterRequest, UserResponse } from './auth.model';
import {HttpClient} from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Service()
export class AuthService {

  private readonly http: HttpClient = inject(HttpClient);
  private readonly currentUserSignal = signal<UserResponse | null>(null);

  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUserSignal() !== null);

  async loadCurrentUser(): Promise<void> {
    try {
      const user = await firstValueFrom(this.http.get<UserResponse>('/api/auth/me'));
      this.currentUserSignal.set(user)
    } catch {
      this.currentUserSignal.set(null);
    }
  }

  async login(request: LoginRequest): Promise<UserResponse> {
    const user = await firstValueFrom(this.http.post<UserResponse>('/api/auth/login', request));
    this.currentUserSignal.set(user);
    return user;
  }

  async register(request: RegisterRequest): Promise<UserResponse> {
    return firstValueFrom(this.http.post<UserResponse>('/api/auth/register', request));
  }

  async logout(): Promise<void> {
    await firstValueFrom(this.http.delete<void>('/api/auth/logout'));
    this.currentUserSignal.set(null);
  }

}
