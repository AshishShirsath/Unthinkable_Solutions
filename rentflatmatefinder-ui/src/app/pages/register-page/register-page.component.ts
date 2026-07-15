import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth-shell">
      <div class="card">
        <h2>Create your account</h2>
        <p>Join RentFlatmate as a tenant or owner and start matching with confidence.</p>
        <form (ngSubmit)="submit()">
          <div class="grid">
            <label>
              First name
              <input name="firstName" [(ngModel)]="form.firstName" required />
            </label>
            <label>
              Last name
              <input name="lastName" [(ngModel)]="form.lastName" required />
            </label>
          </div>
          <label>
            Email
            <input type="email" name="email" [(ngModel)]="form.email" required />
          </label>
          <label>
            Password
            <input type="password" name="password" [(ngModel)]="form.password" required />
          </label>
          <label>
            Phone number
            <input name="phoneNumber" [(ngModel)]="form.phoneNumber" />
          </label>
          <label>
            I am a
            <select name="role" [(ngModel)]="form.role">
              <option value="TENANT">Tenant</option>
              <option value="OWNER">Owner</option>
            </select>
          </label>
          <button class="btn btn-primary" type="submit">Register</button>
        </form>
        <p class="muted">Already have an account? <a routerLink="/login">Log in</a></p>
      </div>
    </section>
  `,
  styles: [
    `:host { display:block; }`,
    `.auth-shell { display:flex; justify-content:center; padding:2rem 0; }`,
    `.card { width:min(100%, 560px); background:white; border:1px solid #e2e8f0; border-radius:24px; padding:2rem; box-shadow:0 20px 50px rgba(15,23,42,.06); }`,
    `h2 { margin:0 0 .5rem; color:#0f172a; }`,
    `p { color:#475569; }`,
    `form { display:grid; gap:1rem; margin-top:1.25rem; }`,
    `.grid { display:grid; gap:1rem; grid-template-columns:repeat(2,minmax(0,1fr)); }`,
    `label { display:grid; gap:.45rem; font-weight:600; color:#0f172a; }`,
    `input, select { border:1px solid #cbd5e1; border-radius:12px; padding:.8rem .9rem; font:inherit; }`,
    `.btn { border:none; padding:.8rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.muted { margin-top:1rem; font-size:.95rem; }`,
    `a { color:#2563eb; text-decoration:none; }`,
    `@media (max-width: 700px) { .grid { grid-template-columns:1fr; } }`
  ]
})
export class RegisterPageComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'TENANT' as 'TENANT' | 'OWNER'
  };

  submit() {
    const payload = {
      firstName: this.form.firstName,
      lastName: this.form.lastName,
      email: this.form.email,
      password: this.form.password,
      phoneNumber: this.form.phoneNumber || null,
      role: this.form.role
    };

    this.auth.register(payload).subscribe({
      next: () => {
        this.toast.show('Account created successfully', 'success');
        this.auth.redirectByRole();
      },
      error: (err) => {
        const message = err?.error?.message || 'Unable to create your account right now.';
        this.toast.show(message, 'error');
      }
    });
  }
}
