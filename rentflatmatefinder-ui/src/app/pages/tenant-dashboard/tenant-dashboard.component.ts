import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { TenantProfile, InterestRequest } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-tenant-dashboard',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Tenant dashboard</p>
          <h2>Find a place that fits your life</h2>
        </div>
        <a routerLink="/listings" class="btn btn-primary">Browse listings</a>
      </div>

      <div class="grid">
        <div class="card">
          <h3>Your preferences</h3>
          <form (ngSubmit)="saveProfile()">
            <label>Preferred city <input name="preferredCity" [(ngModel)]="profile.preferredCity" /></label>
            <label>Preferred locality <input name="preferredLocality" [(ngModel)]="profile.preferredLocality" /></label>
            <div class="split">
              <label>Min budget <input type="number" name="minBudget" [(ngModel)]="profile.minBudget" /></label>
              <label>Max budget <input type="number" name="maxBudget" [(ngModel)]="profile.maxBudget" /></label>
            </div>
            <label>Move-in date <input type="date" name="moveInDate" [(ngModel)]="profile.moveInDate" /></label>
            <label>About you <textarea name="description" rows="4" [(ngModel)]="profile.description"></textarea></label>
            <button class="btn btn-primary" type="submit">Save profile</button>
          </form>
        </div>

        <div class="card">
          <h3>Sent interests</h3>
          @if (!interests.length) {
            <p class="muted">You haven't sent any interest requests yet.</p>
          } @else {
            <div class="stack">
              @for (interest of interests; track interest.id) {
                <div class="item">
                  <div class="item-top">
                    <strong>{{ interest.listingTitle }}</strong>
                    <span class="status-badge" [class]="interest.status.toLowerCase()">{{ interest.status }}</span>
                  </div>
                  <p class="muted" style="margin: 0 0 0.25rem 0;">{{ interest.listingCity }}</p>
                  
                  @if (interest.compatibilityScore != null) {
                    <div class="compatibility-info">
                      <span class="compatibility-badge">
                        Match: {{ interest.compatibilityScore }}%
                      </span>
                      <p class="explanation">{{ interest.scoreExplanation }}</p>
                    </div>
                  }
                  
                  @if (interest.message) {
                    <p class="tenant-message">"{{ interest.message }}"</p>
                  }

                  <div class="actions">
                    @if (interest.status === 'ACCEPTED' && interest.chatRoomId) {
                      <a [routerLink]="['/chat', interest.chatRoomId]" class="btn btn-primary btn-sm">Chat with Owner</a>
                    }
                  </div>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </section>
  `,
  styles: [
    `:host { display:block; }`,
    `.page { display:grid; gap:1.25rem; padding:1rem 0 2rem; }`,
    `.section-heading { display:flex; justify-content:space-between; gap:1rem; align-items:center; }`,
    `.eyebrow { text-transform:uppercase; letter-spacing:.24em; font-size:.78rem; color:#2563eb; font-weight:700; margin:0 0 .25rem; }`,
    `h2, h3 { margin:0; color:#0f172a; }`,
    `.grid { display:grid; grid-template-columns:1.2fr .8fr; gap:1rem; }`,
    `.card { background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.25rem; box-shadow:0 16px 40px rgba(15,23,42,.05); }`,
    `form { display:grid; gap:.8rem; margin-top:1rem; }`,
    `.split { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:.8rem; }`,
    `label { display:grid; gap:.4rem; font-weight:600; color:#0f172a; }`,
    `input, textarea { border:1px solid #cbd5e1; border-radius:12px; padding:.75rem .85rem; font:inherit; }`,
    `.btn { display:inline-block; border:none; padding:.75rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; text-decoration:none; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.stack { display:grid; gap:.75rem; margin-top:1rem; }`,
    `.item { background:#f8fafc; border-radius:14px; padding:.8rem; display:grid; gap:.25rem; }`,
    `.item-top { display:flex; justify-content:space-between; align-items:center; gap:.5rem; }`,
    `.status-badge { font-size:.78rem; font-weight:700; text-transform:uppercase; padding:.25rem .6rem; border-radius:999px; }`,
    `.status-badge.pending { background:#fef3c7; color:#d97706; }`,
    `.status-badge.accepted { background:#dcfce7; color:#15803d; }`,
    `.status-badge.declined { background:#fee2e2; color:#b91c1c; }`,
    `.compatibility-info { margin:.3rem 0; font-size:.85rem; }`,
    `.compatibility-badge { display:inline-block; font-weight:700; color:#1e40af; background:#eff6ff; padding:.15rem .4rem; border-radius:6px; margin-bottom:.2rem; }`,
    `.explanation { margin:0; font-size:.8rem; color:#475569; line-height:1.3; }`,
    `.tenant-message { font-style:italic; font-size:.85rem; color:#475569; border-left:3px solid #cbd5e1; padding-left:.5rem; margin:.3rem 0; }`,
    `.btn-sm { padding:.4rem .8rem; font-size:.8rem; margin-top:.25rem; }`,
    `.muted { color:#64748b; }`,
    `@media (max-width:900px) { .grid { grid-template-columns:1fr; } .split { grid-template-columns:1fr; } .section-heading { flex-direction:column; align-items:flex-start; } }`
  ]
})
export class TenantDashboardComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  profile: TenantProfile = {
    id: 0,
    userId: 0,
    preferredCity: '',
    preferredLocality: '',
    minBudget: 0,
    maxBudget: 0,
    moveInDate: '',
    description: ''
  };
  interests: InterestRequest[] = [];

  ngOnInit() {
    this.loadProfile();
    this.loadInterests();
  }

  saveProfile() {
    this.api.saveTenantProfile(this.profile as unknown as Record<string, unknown>).subscribe({
      next: () => this.toast.show('Profile saved successfully.', 'success'),
      error: () => this.toast.show('Unable to save your profile right now.', 'error')
    });
  }

  private loadProfile() {
    this.api.getTenantProfile().subscribe({
      next: response => this.profile = response.data,
      error: () => undefined
    });
  }

  private loadInterests() {
    this.api.getSentInterests().subscribe({
      next: response => this.interests = response.data ?? [],
      error: () => undefined
    });
  }

}
