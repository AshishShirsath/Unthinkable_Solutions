import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="hero">
      <div>
        <p class="eyebrow">Find your place. Share your space.</p>
        <h1>Discover comfortable rentals and trusted flatmates.</h1>
        <p class="lead">
          RentFlatmate helps tenants discover ideal homes and owners find reliable matches in a single experience.
        </p>
        <div class="actions">
          <a routerLink="/listings" class="btn btn-primary">Browse listings</a>
          @if (auth.isLoggedIn()) {
            <a [routerLink]="auth.hasRole('OWNER') ? '/owner' : auth.hasRole('ADMIN') ? '/admin' : '/tenant'" class="btn btn-outline">Go to dashboard</a>
          } @else {
            <a routerLink="/register" class="btn btn-outline">Create account</a>
          }
        </div>
      </div>
      <div class="hero-card">
        <h3>Why people love it</h3>
        <ul>
          <li>Smart property discovery</li>
          <li>Fast tenant-owner matching</li>
          <li>Secure profile and interest flow</li>
        </ul>
      </div>
    </section>
  `,
  styles: [
    `:host { display: block; }`,
    `.hero { display:grid; grid-template-columns: 1.3fr 0.8fr; gap:2rem; align-items:center; padding:2rem 0 3rem; }`,
    `.eyebrow { text-transform:uppercase; letter-spacing:.24em; font-size:.8rem; color:#2563eb; font-weight:700; margin-bottom:1rem; }`,
    `h1 { font-size: clamp(2rem, 3.5vw, 3.2rem); line-height:1.1; margin:0 0 1rem; color:#0f172a; }`,
    `.lead { font-size:1.05rem; color:#475569; max-width:720px; }`,
    `.actions { display:flex; gap:1rem; margin-top:1.5rem; flex-wrap:wrap; }`,
    `.btn { display:inline-block; padding:.8rem 1.1rem; border-radius:999px; font-weight:600; text-decoration:none; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.btn-outline { border:1px solid #dbeafe; color:#1d4ed8; background:white; }`,
    `.hero-card { background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.5rem; box-shadow:0 20px 50px rgba(15,23,42,.06); }`,
    `.hero-card ul { margin:0; padding-left:1rem; color:#475569; display:grid; gap:.8rem; }`,
    `@media (max-width: 800px) { .hero { grid-template-columns:1fr; } }`
  ]
})
export class HomePageComponent {
  auth = inject(AuthService);
}
