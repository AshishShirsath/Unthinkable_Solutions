import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <a routerLink="/" class="brand">
        <span class="brand-icon">🏠</span>
        RentFlatmate
      </a>
      <div class="nav-links">
        <a routerLink="/listings" routerLinkActive="active">Browse</a>
        @if (auth.isLoggedIn()) {
          @if (auth.hasRole('TENANT')) {
            <a routerLink="/tenant" routerLinkActive="active">Dashboard</a>
            <a routerLink="/tenant/profile" routerLinkActive="active">Profile</a>
          }
          @if (auth.hasRole('OWNER')) {
            <a routerLink="/owner" routerLinkActive="active">Dashboard</a>
            <a routerLink="/owner/create" routerLinkActive="active">List Room</a>
          }
          @if (auth.hasRole('ADMIN')) {
            <a routerLink="/admin" routerLinkActive="active">Admin</a>
          }
          <a routerLink="/chat" routerLinkActive="active">Messages</a>
          <button class="btn btn-outline" (click)="auth.logout()">Logout</button>
        } @else {
          <a routerLink="/login" class="btn btn-outline">Login</a>
          <a routerLink="/register" class="btn btn-primary">Register</a>
        }
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem 2rem;
      background: rgba(255,255,255,0.95);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid #e8eef7;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.25rem;
      font-weight: 700;
      color: #1e40af;
      text-decoration: none;
    }
    .brand-icon { font-size: 1.5rem; }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .nav-links a {
      text-decoration: none;
      color: #475569;
      font-weight: 500;
      padding: 0.4rem 0.6rem;
      border-radius: 8px;
      transition: all 0.2s;
    }
    .nav-links a:hover, .nav-links a.active {
      color: #2563eb;
      background: #eff6ff;
    }
  `]
})
export class NavbarComponent {
  auth = inject(AuthService);
}
