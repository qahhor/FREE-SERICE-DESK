import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="loading-spinner-container" [class.overlay]="overlay">
      <mat-spinner [diameter]="diameter" [color]="color"></mat-spinner>
      <p *ngIf="message" class="loading-message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .loading-spinner-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;

      &.overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 9999;
      }
    }

    .loading-message {
      margin-top: 16px;
      color: #666;
      font-size: 14px;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() diameter: number = 50;
  @Input() color: 'primary' | 'accent' | 'warn' = 'primary';
  @Input() overlay: boolean = false;
  @Input() message?: string;
}
