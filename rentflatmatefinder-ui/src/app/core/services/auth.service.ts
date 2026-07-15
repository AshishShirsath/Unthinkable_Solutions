import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse, UserSession } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'rfmf_session';
  readonly currentUser = signal<UserSession | null>(this.loadSession());

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(tap(res => this.setSession(res.data)));
  }

  register(payload: Record<string, unknown>) {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/register`, payload)
      .pipe(tap(res => this.setSession(res.data)));
  }

  logout() {
    const session = this.currentUser();
    if (session?.refreshToken) {
      this.http
        .post<ApiResponse<string>>(`${environment.apiUrl}/auth/logout`, null, {
          params: { refreshToken: session.refreshToken }
        })
        .subscribe({ error: () => undefined });
    }
    localStorage.removeItem(this.storageKey);
    this.currentUser.set(null);
    this.router.navigate(['/']);
  }

  getToken(): string | null {
    return this.currentUser()?.accessToken ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.role === role;
  }

  redirectByRole(): void {
    const role = this.currentUser()?.role;
    if (role === 'TENANT') this.router.navigate(['/tenant']);
    else if (role === 'OWNER') this.router.navigate(['/owner']);
    else if (role === 'ADMIN') this.router.navigate(['/admin']);
    else this.router.navigate(['/']);
  }

  private setSession(data: AuthResponse): void {
    const session: UserSession = {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      userId: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      role: data.role
    };
    localStorage.setItem(this.storageKey, JSON.stringify(session));
    this.currentUser.set(session);
  }

  private loadSession(): UserSession | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserSession;
    } catch {
      return null;
    }
  }
}
