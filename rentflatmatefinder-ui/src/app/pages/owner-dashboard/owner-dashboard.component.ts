import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Listing, InterestRequest } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-owner-dashboard',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Owner dashboard</p>
          <h2>Manage listings and applicants</h2>
        </div>
        <a routerLink="/owner/create" class="btn btn-primary">Add listing</a>
      </div>

      <div class="grid">
        <div class="card">
          <h3>Your listings</h3>
          @if (!listings.length) {
            <p class="muted">You do not have any listings yet.</p>
          } @else {
            <div class="stack">
              @for (listing of listings; track listing.id) {
                <div class="item">
                  <div class="item-content-wrapper">
                    @if (listing.imageUrls && listing.imageUrls.length) {
                      <img [src]="listing.imageUrls[0]" alt="{{ listing.title }}" class="list-thumb" />
                    }
                    <div class="item-info">
                      <div class="item-top">
                        <strong>{{ listing.title }}</strong>
                        <span class="status">{{ listing.status }}</span>
                      </div>
                      <p>₹{{ listing.rent }} · {{ listing.city }}</p>
                    </div>
                  </div>
                  <div class="actions">
                    <button class="btn btn-outline" type="button" (click)="markFilled(listing.id)" [disabled]="listing.status === 'FILLED'">
                      {{ listing.status === 'FILLED' ? 'Filled' : 'Mark filled' }}
                    </button>
                  </div>
                </div>
              }
            </div>
          }
        </div>

        <div class="card">
          <h3>Received interests</h3>
          @if (!interests.length) {
            <p class="muted">No incoming requests yet.</p>
          } @else {
            <div class="stack">
              @for (interest of interests; track interest.id) {
                <div class="item">
                  <div class="item-top">
                    <strong>{{ interest.tenantName }}</strong>
                    <span class="status">{{ interest.status }}</span>
                  </div>
                  <div class="tenant-profile">
                    <p class="muted">Tenant email: {{ interest.tenantEmail }}</p>
                    <p>{{ interest.message }}</p>
                  </div>
                  <div class="actions">
                    @if (interest.status === 'PENDING') {
                      <button class="btn btn-outline" type="button" (click)="accept(interest.id)">Accept</button>
                      <button class="btn btn-outline" type="button" (click)="decline(interest.id)">Decline</button>
                    } @else if (interest.status === 'ACCEPTED' && interest.chatRoomId) {
                      <a [routerLink]="['/chat', interest.chatRoomId]" class="btn btn-primary">Chat</a>
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
    `.grid { display:grid; grid-template-columns:1fr 1fr; gap:1rem; }`,
    `.card { background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.25rem; box-shadow:0 16px 40px rgba(15,23,42,.05); }`,
    `.stack { display:grid; gap:.75rem; margin-top:1rem; }`,
    `.item { background:#f8fafc; border-radius:14px; padding:.8rem; display:grid; gap:.45rem; }`,
    `.item-content-wrapper { display: flex; gap: 0.75rem; align-items: center; }`,
    `.list-thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 8px; border: 1px solid #cbd5e1; }`,
    `.item-info { flex: 1; display: grid; gap: 0.25rem; }`,
    `.item-top, .actions { display:flex; justify-content:space-between; gap:.5rem; flex-wrap:wrap; }`,
    `.status { color:#1d4ed8; font-size:.85rem; font-weight:700; text-transform:uppercase; }`,
    `.btn { display:inline-block; border:none; padding:.6rem .9rem; border-radius:999px; font-weight:600; cursor:pointer; text-decoration:none; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.btn-outline { border:1px solid #dbeafe; color:#1d4ed8; background:white; }`,
    `.muted { color:#64748b; }`,
    `.tenant-profile { display:grid; gap:.35rem; }`,
    `@media (max-width:900px) { .grid { grid-template-columns:1fr; } .section-heading { flex-direction:column; align-items:flex-start; } }`
  ]
})
export class OwnerDashboardComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  listings: Listing[] = [];
  interests: InterestRequest[] = [];

  ngOnInit() {
    this.loadListings();
    this.loadInterests();
  }

  markFilled(id: number) {
    this.api.markFilled(id).subscribe({
      next: () => {
        this.toast.show('Listing updated.', 'success');
        this.loadListings();
      },
      error: () => this.toast.show('Unable to update listing.', 'error')
    });
  }

  accept(id: number) {
    this.api.acceptInterest(id).subscribe({
      next: () => {
        this.toast.show('Interest accepted.', 'success');
        this.loadInterests();
      },
      error: () => this.toast.show('Unable to accept interest.', 'error')
    });
  }

  decline(id: number) {
    this.api.declineInterest(id).subscribe({
      next: () => {
        this.toast.show('Interest declined.', 'success');
        this.loadInterests();
      },
      error: () => this.toast.show('Unable to decline interest.', 'error')
    });
  }

  private loadListings() {
    this.api.getMyListings().subscribe({
      next: response => this.listings = response.data ?? [],
      error: () => undefined
    });
  }

  private loadInterests() {
    this.api.getReceivedInterests().subscribe({
      next: response => this.interests = response.data ?? [],
      error: () => undefined
    });
  }
}
