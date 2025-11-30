import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    <div class="empty-state">
      <mat-icon class="empty-icon">{{ icon }}</mat-icon>
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      <button mat-raised-button color="primary" *ngIf="actionText" (click)="onAction()">
        {{ actionText }}
      </button>
    </div>
  `,
  styles: [`
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      text-align: center;
      color: #666;
    }

    .empty-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #bbb;
      margin-bottom: 16px;
    }

    h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 500;
      color: #333;
    }

    p {
      margin: 0 0 24px 0;
      font-size: 14px;
      max-width: 400px;
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon: string = 'inbox';
  @Input() title: string = 'No data';
  @Input() message: string = 'There is no data to display';
  @Input() actionText?: string;
  @Input() action?: () => void;

  onAction(): void {
    if (this.action) {
      this.action();
    }
  }
}
