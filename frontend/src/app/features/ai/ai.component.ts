import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-ai',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, LayoutComponent],
  template: `
    <app-layout>
      <div class="ai-container">
        <h1>AI Assistant</h1>
        <p>Leverage AI-powered features to enhance customer support</p>

        <div class="ai-features">
          <mat-card class="feature-card">
            <mat-card-header>
              <mat-icon class="feature-icon">psychology</mat-icon>
              <mat-card-title>Smart Suggestions</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Get AI-powered response suggestions for tickets based on historical data</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Enable</button>
            </mat-card-actions>
          </mat-card>

          <mat-card class="feature-card">
            <mat-card-header>
              <mat-icon class="feature-icon">sentiment_satisfied</mat-icon>
              <mat-card-title>Sentiment Analysis</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Automatically detect customer sentiment from ticket messages</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Enable</button>
            </mat-card-actions>
          </mat-card>

          <mat-card class="feature-card">
            <mat-card-header>
              <mat-icon class="feature-icon">auto_awesome</mat-icon>
              <mat-card-title>Auto-categorization</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Automatically categorize tickets using machine learning</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Enable</button>
            </mat-card-actions>
          </mat-card>

          <mat-card class="feature-card chat-card">
            <mat-card-header>
              <mat-icon class="feature-icon">chat</mat-icon>
              <mat-card-title>AI Chat</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="chat-messages">
                <div class="message bot-message">
                  <mat-icon>smart_toy</mat-icon>
                  <span>How can I help you today?</span>
                </div>
              </div>
              <div class="chat-input">
                <mat-form-field appearance="outline" class="full-width">
                  <input matInput [(ngModel)]="chatMessage" placeholder="Ask me anything...">
                  <button mat-icon-button matSuffix (click)="sendMessage()">
                    <mat-icon>send</mat-icon>
                  </button>
                </mat-form-field>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </app-layout>
  `,
  styles: [`
    .ai-container {
      h1 { margin-bottom: 8px; }
      p { color: #666; margin-bottom: 32px; }

      .ai-features {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 24px;

        .feature-card {
          mat-card-header {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            margin-bottom: 16px;

            .feature-icon {
              font-size: 48px;
              width: 48px;
              height: 48px;
              color: #3f51b5;
              margin-bottom: 16px;
            }
          }

          mat-card-content {
            min-height: 80px;
            text-align: center;
            color: #666;
          }

          mat-card-actions {
            justify-content: center;
          }

          &.chat-card {
            grid-column: 1 / -1;

            mat-card-header {
              flex-direction: row;
              justify-content: flex-start;
              text-align: left;

              .feature-icon {
                margin-bottom: 0;
                margin-right: 16px;
              }
            }

            mat-card-content {
              text-align: left;
            }

            .chat-messages {
              min-height: 200px;
              max-height: 400px;
              overflow-y: auto;
              padding: 16px;
              background: #f5f5f5;
              border-radius: 8px;
              margin-bottom: 16px;

              .message {
                display: flex;
                align-items: flex-start;
                gap: 12px;
                margin-bottom: 12px;

                &.bot-message {
                  mat-icon { color: #3f51b5; }
                }
              }
            }

            .chat-input {
              mat-form-field {
                width: 100%;
              }
            }
          }
        }
      }
    }
  `]
})
export class AiComponent {
  chatMessage = '';

  sendMessage(): void {
    if (this.chatMessage.trim()) {
      console.log('Sending message:', this.chatMessage);
      this.chatMessage = '';
    }
  }
}
