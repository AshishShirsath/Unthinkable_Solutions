import { Component, inject, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `
    <section class="page">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Administration</p>
          <h2>Manage platform users and listings</h2>
        </div>
      </div>
      <div class="card">
        <h3>Users</h3>
        <div class="table">
          <div class="user-row header">
            <span>Name</span><span>Email</span><span>Role</span><span>Status</span><span>Action</span>
          </div>
          @for (user of users; track user.id) {
            <div class="user-row">
              <span>{{ user.name }}</span>
              <span>{{ user.email }}</span>
              <span>{{ user.role }}</span>
              <span>{{ user.enabled ? 'Active' : 'Disabled' }}</span>
              <button class="btn btn-outline" type="button" (click)="disableUser(user.id)" [disabled]="!user.enabled">Disable</button>
            </div>
          }
        </div>
      </div>
      <div class="card">
        <h3>Listings</h3>
        <div class="table">
          <div class="listing-row header">
            <span>Title</span><span>City</span><span>Rent</span><span>Status</span><span>Owner</span><span>Action</span>
          </div>
          @for (listing of listings; track listing.id) {
            <div class="listing-row">
              <span>{{ listing.title }}</span>
              <span>{{ listing.city }}</span>
              <span>₹{{ listing.rent }}</span>
              <span>{{ listing.status }}</span>
              <span>{{ listing.owner }}</span>
              <button class="btn btn-outline btn-delete" type="button" (click)="deleteListing(listing.id)" [disabled]="listing.deleted">
                {{ listing.deleted ? 'Deleted' : 'Delete' }}
              </button>
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
    `.card { background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.25rem; box-shadow:0 16px 40px rgba(15,23,42,.05); }`,
    `.table { display:grid; gap:.4rem; margin-top:1rem; }`,
    `.user-row { display:grid; grid-template-columns:1.4fr 1.4fr .8fr .8fr .8fr; gap:.6rem; align-items:center; padding:.75rem .85rem; background:#f8fafc; border-radius:12px; }`,
    `.listing-row { display:grid; grid-template-columns:1.2fr 1fr .8fr .8fr 1.2fr .8fr; gap:.6rem; align-items:center; padding:.75rem .85rem; background:#f8fafc; border-radius:12px; }`,
    `.header { font-weight:700; color:#0f172a; background:transparent; padding:0 0 .3rem; }`,
    `.btn { border:none; padding:.55rem .8rem; border-radius:999px; font-weight:600; cursor:pointer; }`,
    `.btn-outline { border:1px solid #dbeafe; color:#1d4ed8; background:white; }`,
    `.btn-delete { color:#b91c1c; border-color:#fee2e2; }`,
    `.btn-delete:hover { background:#fef2f2; }`,
    `@media (max-width:900px) { .user-row, .listing-row { grid-template-columns:1fr; } .header { display:none; } }`
  ]
})
export class AdminDashboardComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  users: Array<{ id: number; name: string; email: string; role: string; enabled: boolean }> = [];
  listings: Array<{ id: number; title: string; city: string; rent: number; status: string; owner: string; deleted: boolean }> = [];

  ngOnInit() {
    this.loadData();
  }

  disableUser(id: number) {
    this.api.disableUser(id).subscribe({
      next: () => {
        this.toast.show('User disabled.', 'success');
        this.loadData();
      },
      error: () => this.toast.show('Unable to disable user.', 'error')
    });
  }

  deleteListing(id: number) {
    if (confirm('Are you sure you want to delete this listing?')) {
      this.api.deleteAdminListing(id).subscribe({
        next: () => {
          this.toast.show('Listing deleted.', 'success');
          this.loadData();
        },
        error: () => this.toast.show('Unable to delete listing.', 'error')
      });
    }
  }

  private loadData() {
    this.api.getAdminUsers().subscribe({
      next: response => this.users = response.data ?? [],
      error: () => undefined
    });
    this.api.getAdminListings().subscribe({
      next: response => this.listings = response.data ?? [],
      error: () => undefined
    });
  }
}
