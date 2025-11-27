import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule, TranslateModule],
  template: `
    <div class="chat-page">
      <div class="chat-header">
        <mat-icon>chat</mat-icon>
        <h1>{{ 'chat.page_title' | translate }}</h1>
        <p>{{ 'chat.page_subtitle' | translate }}</p>
      </div>

      <div class="chat-info">
        <p>{{ 'chat.instruction' | translate }}</p>
        <button mat-raised-button color="primary" (click)="openChatWidget()">
          <mat-icon>chat_bubble</mat-icon>
          {{ 'chat.start_chat' | translate }}
        </button>
      </div>

      <div class="alternatives">
        <h3>{{ 'chat.other_ways' | translate }}</h3>
        <div class="options">
          <a routerLink="/tickets/new" class="option">
            <mat-icon>confirmation_number</mat-icon>
            <span>{{ 'chat.submit_ticket' | translate }}</span>
          </a>
          <a routerLink="/knowledge-base" class="option">
            <mat-icon>library_books</mat-icon>
            <span>{{ 'chat.browse_kb' | translate }}</span>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .chat-page {
      max-width: 600px;
      margin: 0 auto;
      text-align: center;
      padding: 48px 24px;
    }

    .chat-header {
      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #673ab7;
      }

      h1 { margin: 16px 0 8px; color: #333; }
      p { color: #666; margin: 0; }
    }

    .chat-info {
      margin: 32px 0;
      padding: 24px;
      background: #f5f5f5;
      border-radius: 8px;

      p { margin: 0 0 16px; color: #666; }
    }

    .alternatives {
      margin-top: 48px;

      h3 { color: #666; font-weight: normal; margin: 0 0 16px; }
    }

    .options {
      display: flex;
      justify-content: center;
      gap: 24px;

      .option {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 24px 32px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        text-decoration: none;
        color: #333;
        transition: transform 0.2s, box-shadow 0.2s;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        }

        mat-icon { font-size: 32px; width: 32px; height: 32px; color: #673ab7; }
      }
    }
  `]
})
export class ChatComponent {
  openChatWidget(): void {
    // Trigger chat widget to open
    const event = new CustomEvent('openChatWidget');
    window.dispatchEvent(event);
  }
}
