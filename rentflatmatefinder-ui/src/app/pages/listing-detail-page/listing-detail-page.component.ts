import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { Compatibility, Listing } from '../../core/models/api.models';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-listing-detail-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    @if (loading) {
      <p class="loading">Loading listing...</p>
    } @else if (listing) {
      <section class="detail-shell">
        <div class="card primary">
          @if (listing.imageUrls && listing.imageUrls.length) {
            <div class="detail-gallery">
              <img [src]="listing.imageUrls[selectedImageIndex]" alt="{{ listing.title }}" class="main-image" />
              @if (listing.imageUrls.length > 1) {
                <div class="thumbnail-grid">
                  @for (img of listing.imageUrls; track img; let idx = $index) {
                    <img
                      [src]="img"
                      alt="Thumbnail"
                      class="thumbnail"
                      [class.active]="selectedImageIndex === idx"
                      (click)="selectedImageIndex = idx"
                    />
                  }
                </div>
              }
            </div>
          }
          <p class="eyebrow">{{ listing.city }}</p>
          <h2>{{ listing.title }}</h2>
          <p class="description">{{ listing.description || 'A well-kept rental with a friendly atmosphere.' }}</p>
          <div class="stats">
            <div><strong>Rent</strong><span>₹{{ listing.rent }}/month</span></div>
            <div><strong>Deposit</strong><span>₹{{ listing.deposit }}</span></div>
            <div><strong>Room type</strong><span>{{ listing.roomType }}</span></div>
            <div><strong>Furnishing</strong><span>{{ listing.furnishingStatus }}</span></div>
          </div>
          <div class="meta">
            <span>{{ listing.locality }}</span>
            <span>{{ listing.address }}</span>
          </div>
          <div class="meta">
            <span>Owner: {{ listing.ownerName }}</span>
            <span>Available from {{ listing.availableFrom }}</span>
          </div>
        </div>

        <div class="card secondary">
          @if (compatibility) {
            <h3>Compatibility</h3>
            <p class="score">{{ compatibility.score }}% match</p>
            <p>{{ compatibility.explanation }}</p>
            <button class="btn btn-outline" (click)="computeCompatibility()">Compatibility AI</button>
          } @else if (auth.isLoggedIn() && auth.hasRole('TENANT')) {
            <p class="muted">Compatibility insights are being prepared for this listing.</p>
            <button class="btn btn-outline" (click)="computeCompatibility()">Compatibility AI</button>
          }

          @if (auth.isLoggedIn() && auth.hasRole('TENANT')) {
            <h3>Show your interest</h3>
            <div class="interest-form">
              <label>
                Message to owner (optional)
                <textarea name="interestMessage" rows="3" [(ngModel)]="interestMessage"></textarea>
              </label>

              <div class="interest-actions">
                <button
                  class="btn btn-primary"
                  type="button"
                  [disabled]="interestLoading || interestSent"
                  (click)="submitInterest()"
                >
                  @if (interestLoading) {
                    Sending...
                  } @else if (interestSent) {
                    Interested ✓
                  } @else {
                    Interested
                  }
                </button>
              </div>

              @if (interestSent) {
                <p class="success-note">Thanks! Your interest has been sent.</p>
              }
            </div>
          } @else {
            <p class="muted">Interest and chat are available to authorized users only.</p>
          }
        </div>
      </section>
    }
  `,
  styles: [
    `:host { display:block; }`,
    `.loading { background:white; border:1px dashed #cbd5e1; border-radius:16px; padding:1rem; }`,
    `.detail-shell { display:grid; grid-template-columns:1.3fr .8fr; gap:1.25rem; padding:1rem 0 2rem; }`,
    `.card { background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.25rem; box-shadow:0 18px 45px rgba(15,23,42,.05); }`,
    `.detail-gallery { display: grid; gap: 0.75rem; margin-bottom: 1.5rem; }`,
    `.main-image { width: 100%; height: 320px; object-fit: cover; border-radius: 18px; border: 1px solid #e2e8f0; }`,
    `.thumbnail-grid { display: flex; gap: 0.5rem; overflow-x: auto; padding-bottom: 0.25rem; }`,
    `.thumbnail { width: 80px; height: 60px; object-fit: cover; border-radius: 8px; cursor: pointer; border: 2px solid transparent; }`,
    `.thumbnail:hover, .thumbnail.active { border-color: #2563eb; }`,
    `.eyebrow { text-transform:uppercase; letter-spacing:.24em; font-size:.78rem; color:#2563eb; font-weight:700; margin:0 0 .25rem; }`,
    `h2 { margin:0 0 .6rem; }`,
    `.description { color:#475569; line-height:1.6; }`,
    `.stats { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:.8rem; margin:1rem 0; }`,
    `.stats div { background:#f8fafc; border-radius:14px; padding:.8rem; display:grid; gap:.25rem; }`,
    `.meta { display:flex; justify-content:space-between; gap:.75rem; flex-wrap:wrap; color:#334155; margin-top:.5rem; }`,
    `.score { font-size:1.1rem; font-weight:700; color:#0f172a; }`,
    `.interest-form { display:grid; gap:.8rem; margin-top:1rem; }`,
    `.interest-actions { display:flex; flex-wrap:wrap; gap:.75rem; align-items:center; }`,
    `.success-note { color:#047857; background:#ecfdf5; border:1px solid #d1fae5; border-radius:12px; padding:.75rem 1rem; }`,
    `label { display:grid; gap:.4rem; font-weight:600; color:#0f172a; }`,
    `textarea { border:1px solid #cbd5e1; border-radius:12px; padding:.8rem .9rem; font:inherit; }`,
    `.btn { display:inline-flex; align-items:center; justify-content:center; border:none; padding:.75rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; text-decoration:none; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `.btn-outline { border:1px solid #dbeafe; color:#1d4ed8; background:white; }`,
    `.muted { color:#64748b; }`,
    `@media (max-width: 900px) { .detail-shell { grid-template-columns:1fr; } }`
  ]
})
export class ListingDetailPageComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  auth = inject(AuthService);

  listing: Listing | null = null;
  compatibility: Compatibility | null = null;
  selectedImageIndex = 0;
  loading = true;
  interestMessage = '';
  interestLoading = false;
  interestSent = false;

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      this.loadListing(id);
    });
  }

  submitInterest() {
    if (!this.listing) return;
    this.interestLoading = true;
    this.api.sendInterest(this.listing.id, this.interestMessage).subscribe({
      next: () => {
        this.toast.show('Your interest was sent successfully.', 'success');
        this.interestMessage = '';
        this.interestSent = true;
        this.interestLoading = false;
      },
      error: () => {
        this.toast.show('Unable to send your interest right now.', 'error');
        this.interestLoading = false;
      }
    });
  }

  computeCompatibility() {
    if (!this.listing) return;
    this.api.getCompatibility(this.listing.id).subscribe({
      next: resp => {
        this.compatibility = resp.data;
        this.toast.show('Compatibility computed.', 'success');
      },
      error: () => this.toast.show('Unable to compute compatibility now.', 'error')
    });
  }

  private loadListing(id: number) {
    this.loading = true;
    this.api.getListing(id).subscribe({
      next: response => {
        this.listing = response.data;
        if (this.auth.isLoggedIn() && this.auth.hasRole('TENANT')) {
          this.api.getCompatibility(id).subscribe({
            next: compatibilityResponse => {
              this.compatibility = compatibilityResponse.data;
              this.loading = false;
            },
            error: () => {
              this.loading = false;
            }
          });
        } else {
          this.loading = false;
        }
      },
      error: () => {
        this.toast.show('The listing could not be loaded.', 'error');
        this.loading = false;
      }
    });
  }
}
