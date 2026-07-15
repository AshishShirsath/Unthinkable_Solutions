import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-owner-listing-form',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="page">
      <div class="card">
        <h2>Create a listing</h2>
        <p>List a room or apartment and let compatible tenants reach out.</p>
        <form (ngSubmit)="submit()">
          <label>Title <input name="title" [(ngModel)]="form.title" required /></label>
          <label>Description <textarea name="description" rows="4" [(ngModel)]="form.description"></textarea></label>
          <div class="split">
            <label>City <input name="city" [(ngModel)]="form.city" required /></label>
            <label>Locality <input name="locality" [(ngModel)]="form.locality" required /></label>
          </div>
          <label>Address <input name="address" [(ngModel)]="form.address" required /></label>
          <div class="split">
            <label>Rent <input type="number" name="rent" [(ngModel)]="form.rent" required /></label>
            <label>Deposit <input type="number" name="deposit" [(ngModel)]="form.deposit" required /></label>
          </div>
          <div class="split">
            <label>Available from <input type="date" name="availableFrom" [(ngModel)]="form.availableFrom" required /></label>
            <label>Room type
              <select name="roomType" [(ngModel)]="form.roomType">
                <option value="SINGLE">Single</option>
                <option value="DOUBLE">Double</option>
                <option value="SHARING">Sharing</option>
              </select>
            </label>
          </div>
          <label>Furnishing
            <select name="furnishingStatus" [(ngModel)]="form.furnishingStatus">
              <option value="UNFURNISHED">Unfurnished</option>
              <option value="SEMI_FURNISHED">Semi-furnished</option>
              <option value="FULLY_FURNISHED">Fully furnished</option>
            </select>
          </label>
          <label>Image URLs <input name="imageUrls" [(ngModel)]="imageUrlsText" placeholder="https://..." /></label>
          <label>Upload images (optional)
            <input type="file" (change)="onFileSelected($event)" multiple accept="image/*" />
          </label>
          @if (uploadedImages.length) {
            <small>{{ uploadedImages.length }} file(s) selected</small>
          }
          <button class="btn btn-primary" type="submit" [disabled]="submitting">
            {{ submitting ? 'Publishing...' : 'Publish listing' }}
          </button>
        </form>
      </div>
    </section>
  `,
  styles: [
    `:host { display:block; }`,
    `.page { display:flex; justify-content:center; padding:1rem 0 2rem; }`,
    `.card { width:min(100%, 720px); background:white; border:1px solid #e2e8f0; border-radius:24px; padding:1.5rem; box-shadow:0 20px 50px rgba(15,23,42,.06); }`,
    `h2 { margin:0 0 .4rem; color:#0f172a; }`,
    `p { color:#475569; }`,
    `form { display:grid; gap:.85rem; margin-top:1rem; }`,
    `.split { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:.8rem; }`,
    `label { display:grid; gap:.4rem; font-weight:600; color:#0f172a; }`,
    `input, textarea, select { border:1px solid #cbd5e1; border-radius:12px; padding:.75rem .85rem; font:inherit; }`,
    `.btn { border:none; padding:.8rem 1rem; border-radius:999px; font-weight:600; cursor:pointer; }`,
    `.btn-primary { background:#2563eb; color:white; }`,
    `@media (max-width:700px) { .split { grid-template-columns:1fr; } }`
  ]
})
export class OwnerListingFormComponent {
  private api = inject(ApiService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = {
    title: '',
    description: '',
    city: '',
    locality: '',
    address: '',
    rent: 0,
    deposit: 0,
    availableFrom: '',
    roomType: 'SINGLE',
    furnishingStatus: 'UNFURNISHED'
  };
  imageUrlsText = '';
  uploadedImages: File[] = [];
  submitting = false;

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) {
      return;
    }
    for (let i = 0; i < input.files.length; i++) {
      this.uploadedImages.push(input.files.item(i) as File);
    }
  }

  submit() {
    if (this.submitting) return;
    this.submitting = true;
    const manualUrls = this.imageUrlsText.split(',').map(item => item.trim()).filter(Boolean);

    if (this.uploadedImages.length > 0) {
      this.api.uploadFiles(this.uploadedImages).subscribe({
        next: (response) => {
          const uploadedUrls = response.data || [];
          const combinedUrls = [...manualUrls, ...uploadedUrls];
          this.publishListing(combinedUrls);
        },
        error: () => {
          this.toast.show('Unable to upload images.', 'error');
          this.submitting = false;
        }
      });
    } else {
      this.publishListing(manualUrls);
    }
  }

  private publishListing(imageUrls: string[]) {
    const payload: Record<string, unknown> = {
      ...this.form,
      imageUrls: imageUrls.length ? imageUrls : null
    };

    this.api.createListing(payload).subscribe({
      next: () => {
        this.toast.show('Listing published successfully.', 'success');
        this.submitting = false;
        this.router.navigate(['/owner']);
      },
      error: () => {
        this.toast.show('Unable to publish your listing.', 'error');
        this.submitting = false;
      }
    });
  }
}
