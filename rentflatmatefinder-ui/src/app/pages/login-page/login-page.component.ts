import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth-shell">
      <div class="card">
        <h2>Welcome back</h2>
        <p>Sign in to browse homes, save preferences, and manage requests.</p>
        <form (ngSubmit)="submit()">
          <label>
            Email
            <input type="email" name="email" [(ngModel)]="form.email" required />
          </label>
          <label>
            Password
            <input type="password" name="password" [(ngModel)]="form.password" required />
          </label>
          <button class="btn btn-primary" type="submit">Log in</button>
        </form>
        <p class="muted">Don't have an account? <a routerLink="/register">Create one</a></p>
      </div>
    </section>
  `,
  styles: [
    `:host { display:block; }`,
    `.auth-shell { display:flex; justify-content:center; padding:2rem 0; }`,
    `.card { width:min(100%, 460px); background:white; border:1px solid #e2e8f0; border-radius:24px; padding:2rem; box-shadow:0 20px 50px rgba(15,23,42,.06); }`,
    `h2 { margin:0 0 .5rem; color:#0f172a; }`,
    `p { color:#475569; }`,
    `form { display:grid; gap:1rem; margin-top:1.25rem; }`,
    `label { display:grid; gap:.45rem; font-weight:600; color:#0f172a; }`,
    `input { border:1px solid #cbd5e1; border-radius:12px; padding:.8rem .9rem; font:inherit; }`,
    `.btn { border:none; padding:.8rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.muted { margin-top:1rem; font-size:.95rem; }`,
    `a { color:#2563eb; text-decoration:none; }`
  ]
})
export class LoginPageComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = { email: '', password: '' };

  submit() {
    this.auth.login(this.form.email, this.form.password).subscribe({
      next: () => {
        this.toast.show('Signed in successfully', 'success');
        this.auth.redirectByRole();
      },
      error: () => this.toast.show('Unable to sign in. Please check your details.', 'error')
    });
  }
}
