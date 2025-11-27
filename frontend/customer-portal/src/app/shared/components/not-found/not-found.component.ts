import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule, TranslateModule],
  template: `
    <div class="not-found-container">
      <mat-icon class="error-icon">error_outline</mat-icon>
      <h1>404</h1>
      <h2>{{ 'errors.page_not_found' | translate }}</h2>
      <p>{{ 'errors.page_not_found_message' | translate }}</p>
      <div class="actions">
        <a mat-raised-button color="primary" routerLink="/">
          <mat-icon>home</mat-icon>
          {{ 'common.go_home' | translate }}
        </a>
        <a mat-stroked-button routerLink="/knowledge-base">
          <mat-icon>help</mat-icon>
          {{ 'common.browse_help' | translate }}
        </a>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 60vh;
      text-align: center;
      padding: 24px;

      .error-icon {
        font-size: 80px;
        width: 80px;
        height: 80px;
        color: #ccc;
      }

      h1 {
        font-size: 72px;
        margin: 16px 0 8px;
        color: #333;
      }

      h2 {
        font-size: 24px;
        margin: 0 0 16px;
        color: #666;
      }

      p {
        color: #888;
        margin-bottom: 24px;
      }

      .actions {
        display: flex;
        gap: 16px;
      }
    }
  `]
})
export class NotFoundComponent {}
