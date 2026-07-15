import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Listing } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-listings-page',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Available spaces</p>
          <h2>Browse listings</h2>
        </div>
        @if (auth.isLoggedIn() && auth.hasRole('OWNER')) {
          <a routerLink="/owner/create" class="btn btn-primary">Create listing</a>
        }
      </div>

      <form class="filters" (ngSubmit)="applyFilters()">
        <input name="city" [(ngModel)]="filters.city" placeholder="City" />
        <input name="minBudget" type="number" [(ngModel)]="filters.minBudget" placeholder="Min budget" />
        <input name="maxBudget" type="number" [(ngModel)]="filters.maxBudget" placeholder="Max budget" />
        <button type="submit" class="btn btn-primary">Search</button>
        <button type="button" class="btn btn-outline" (click)="clearFilters()">Reset</button>
      </form>

      @if (loading) {
        <p class="loading">Loading listings...</p>
      } @else if (!listings.length) {
        <div class="empty">No listings match your filters yet.</div>
      } @else {
        <div class="card-grid">
          @for (listing of listings; track listing.id) {
            <article class="card">
              @if (listing.imageUrls && listing.imageUrls.length) {
                <div class="card-image-wrapper">
                  <img [src]="listing.imageUrls[0]" alt="{{ listing.title }}" class="card-image" />
                </div>
              } @else {
                <div class="card-image-wrapper placeholder">
                  <span class="placeholder-icon">🏠</span>
                </div>
              }
              <div class="card-top">
                <span class="badge">{{ listing.roomType }}</span>
                <span class="badge muted">{{ listing.city }}</span>
              </div>
              <h3>{{ listing.title }}</h3>
              <p>{{ listing.description || 'Comfortable rental with flexible move-in options.' }}</p>
              <div class="meta">
                <span>₹{{ listing.rent }}/month</span>
                <span>{{ listing.furnishingStatus }}</span>
              </div>
              <div class="meta">
                <span>{{ listing.locality }}</span>
                <span>Available from {{ listing.availableFrom }}</span>
              </div>
              <div class="actions">
                <a [routerLink]="['/listings', listing.id]" class="btn btn-outline">View details</a>
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: [
    `:host { display:block; }`,
    `.page { display:grid; gap:1.25rem; padding:1rem 0 2rem; }`,
    `.section-heading { display:flex; justify-content:space-between; gap:1rem; align-items:center; }`,
    `.eyebrow { text-transform:uppercase; letter-spacing:.24em; font-size:.78rem; color:#2563eb; font-weight:700; margin:0 0 .25rem; }`,
    `h2 { margin:0; color:#0f172a; }`,
    `.filters { display:flex; flex-wrap:wrap; gap:.75rem; background:white; padding:1rem; border-radius:18px; border:1px solid #e2e8f0; }`,
    `input { border:1px solid #cbd5e1; border-radius:12px; padding:.75rem .85rem; min-width:160px; }`,
    `.btn { display:inline-block; border:none; padding:.75rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; text-decoration:none; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.btn-outline { border:1px solid #dbeafe; color:#1d4ed8; background:white; }`,
    `.card-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:1rem; }`,
    `.card { background:white; border:1px solid #e2e8f0; border-radius:20px; padding:1rem; box-shadow:0 16px 40px rgba(15,23,42,.05); display:grid; gap:.7rem; }`,
    `.card-image-wrapper { height:180px; width:100%; overflow:hidden; border-radius:14px; margin-bottom:.5rem; background:#f1f5f9; display:flex; align-items:center; justify-content:center; }`,
    `.card-image { width:100%; height:100%; object-fit:cover; transition:transform 0.3s; }`,
    `.card-image:hover { transform:scale(1.05); }`,
    `.card-image-wrapper.placeholder { color:#94a3b8; font-size:2.5rem; }`,
    `.card-top, .meta, .actions { display:flex; justify-content:space-between; gap:.5rem; flex-wrap:wrap; }`,
    `.badge { background:#eff6ff; color:#1d4ed8; padding:.35rem .6rem; border-radius:999px; font-size:.75rem; font-weight:700; text-transform:uppercase; }`,
    `.badge.muted { background:#f8fafc; color:#475569; }`,
    `.loading, .empty { background:white; border:1px dashed #cbd5e1; border-radius:16px; padding:1rem; color:#475569; }`,
    `@media (max-width:700px) { .section-heading { flex-direction:column; align-items:flex-start; } }`
  ]
})
export class ListingsPageComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  listings: Listing[] = [];
  filters = { city: '', minBudget: '', maxBudget: '' };
  loading = false;

  ngOnInit() {
    this.loadListings();
  }

  applyFilters() {
    this.loadListings({
      city: this.filters.city || undefined,
      minBudget: this.filters.minBudget ? Number(this.filters.minBudget) : undefined,
      maxBudget: this.filters.maxBudget ? Number(this.filters.maxBudget) : undefined
    });
  }

  clearFilters() {
    this.filters = { city: '', minBudget: '', maxBudget: '' };
    this.loadListings();
  }

  private loadListings(filters?: { city?: string; minBudget?: number; maxBudget?: number }) {
    this.loading = true;
    this.api.getListings(filters).subscribe({
      next: response => {
        this.listings = response.data ?? [];
        this.loading = false;
      },
      error: () => {
        this.toast.show('Unable to fetch listings right now.', 'error');
        this.loading = false;
      }
    });
  }
}
