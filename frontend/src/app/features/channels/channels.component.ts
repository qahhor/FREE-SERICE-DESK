import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-channels',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, LayoutComponent],
  template: `
    <app-layout>
      <div class="channels-container">
        <h1>Channels Management</h1>
        <p>Manage your communication channels (Email, Telegram, WhatsApp, Live Chat)</p>

        <div class="channels-grid">
          <mat-card>
            <mat-card-header>
              <mat-icon>email</mat-icon>
              <mat-card-title>Email</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Configure email integration for ticket creation</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Configure</button>
            </mat-card-actions>
          </mat-card>

          <mat-card>
            <mat-card-header>
              <mat-icon>telegram</mat-icon>
              <mat-card-title>Telegram</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Connect Telegram bot for customer support</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Configure</button>
            </mat-card-actions>
          </mat-card>

          <mat-card>
            <mat-card-header>
              <mat-icon>whatsapp</mat-icon>
              <mat-card-title>WhatsApp</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Integrate WhatsApp Business API</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Configure</button>
            </mat-card-actions>
          </mat-card>

          <mat-card>
            <mat-card-header>
              <mat-icon>chat</mat-icon>
              <mat-card-title>Live Chat</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>Enable live chat widget on your website</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button color="primary">Configure</button>
            </mat-card-actions>
          </mat-card>
        </div>
      </div>
    </app-layout>
  `,
  styles: [`
    .channels-container {
      h1 { margin-bottom: 8px; }
      p { color: #666; margin-bottom: 24px; }

      .channels-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        gap: 16px;

        mat-card {
          mat-card-header {
            display: flex;
            align-items: center;
            gap: 12px;

            mat-icon {
              font-size: 32px;
              width: 32px;
              height: 32px;
              color: #3f51b5;
            }
          }

          mat-card-content {
            padding: 16px 0;
            min-height: 60px;
          }
        }
      }
    }
  `]
})
export class ChannelsComponent {}
