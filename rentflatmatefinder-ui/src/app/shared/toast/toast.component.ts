import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    @if (toast.message()) {
      <div class="toast" [class]="toast.type()">{{ toast.message() }}</div>
    }
  `,
  styles: [`
    .toast {
      position: fixed;
      top: 1.5rem;
      right: 1.5rem;
      padding: 1rem 1.5rem;
      border-radius: 12px;
      color: white;
      font-weight: 500;
      z-index: 9999;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease;
    }
    .success { background: linear-gradient(135deg, #059669, #10b981); }
    .error { background: linear-gradient(135deg, #dc2626, #ef4444); }
    .info { background: linear-gradient(135deg, #2563eb, #3b82f6); }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastComponent {
  toast = inject(ToastService);
}
