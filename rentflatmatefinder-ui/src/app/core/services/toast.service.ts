import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly message = signal<string | null>(null);
  readonly type = signal<'success' | 'error' | 'info'>('info');

  show(message: string, type: 'success' | 'error' | 'info' = 'info') {
    this.message.set(message);
    this.type.set(type);
    setTimeout(() => this.message.set(null), 3500);
  }
}
