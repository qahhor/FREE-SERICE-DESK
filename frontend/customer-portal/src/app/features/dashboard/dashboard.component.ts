import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { TicketService, TicketStats } from '../../core/services/ticket.service';
import { User } from '../../core/models/user.model';
import { Ticket } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="dashboard">
      <div class="welcome-section">
        <h1>{{ 'dashboard.welcome' | translate }}, {{ user?.firstName }}!</h1>
        <p>{{ 'dashboard.subtitle' | translate }}</p>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <mat-card class="action-card" routerLink="/tickets/new">
          <mat-icon>add_circle</mat-icon>
          <h3>{{ 'dashboard.new_ticket' | translate }}</h3>
          <p>{{ 'dashboard.new_ticket_desc' | translate }}</p>
        </mat-card>

        <mat-card class="action-card" routerLink="/knowledge-base">
          <mat-icon>library_books</mat-icon>
          <h3>{{ 'dashboard.knowledge_base' | translate }}</h3>
          <p>{{ 'dashboard.knowledge_base_desc' | translate }}</p>
        </mat-card>

        <mat-card class="action-card" routerLink="/chat">
          <mat-icon>chat</mat-icon>
          <h3>{{ 'dashboard.live_chat' | translate }}</h3>
          <p>{{ 'dashboard.live_chat_desc' | translate }}</p>
        </mat-card>
      </div>

      <!-- Stats -->
      <div class="stats-section" *ngIf="stats">
        <h2>{{ 'dashboard.your_tickets' | translate }}</h2>
        <div class="stats-grid">
          <mat-card class="stat-card">
            <div class="stat-value open">{{ stats.open }}</div>
            <div class="stat-label">{{ 'ticket.status.open' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value in-progress">{{ stats.inProgress }}</div>
            <div class="stat-label">{{ 'ticket.status.in_progress' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value pending">{{ stats.pending }}</div>
            <div class="stat-label">{{ 'ticket.status.pending' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value resolved">{{ stats.resolved }}</div>
            <div class="stat-label">{{ 'ticket.status.resolved' | translate }}</div>
          </mat-card>
        </div>
      </div>

      <!-- Recent Tickets -->
      <div class="recent-tickets">
        <div class="section-header">
          <h2>{{ 'dashboard.recent_tickets' | translate }}</h2>
          <a mat-button color="primary" routerLink="/tickets">
            {{ 'dashboard.view_all' | translate }}
            <mat-icon>arrow_forward</mat-icon>
          </a>
        </div>

        <mat-spinner *ngIf="isLoading" diameter="40"></mat-spinner>

        <div class="ticket-list" *ngIf="!isLoading">
          <mat-card *ngFor="let ticket of recentTickets" class="ticket-card" [routerLink]="['/tickets', ticket.id]">
            <div class="ticket-header">
              <span class="ticket-number">#{{ ticket.number }}</span>
              <span class="status-badge" [class]="ticket.status.toLowerCase()">
                {{ 'ticket.status.' + ticket.status.toLowerCase() | translate }}
              </span>
            </div>
            <h4>{{ ticket.subject }}</h4>
            <div class="ticket-meta">
              <span><mat-icon>schedule</mat-icon> {{ ticket.createdAt | date:'short' }}</span>
              <span class="priority-badge" [class]="ticket.priority.toLowerCase()">
                {{ ticket.priority }}
              </span>
            </div>
          </mat-card>

          <div class="empty-state" *ngIf="recentTickets.length === 0">
            <mat-icon>inbox</mat-icon>
            <p>{{ 'dashboard.no_tickets' | translate }}</p>
            <a mat-raised-button color="primary" routerLink="/tickets/new">
              {{ 'dashboard.create_first_ticket' | translate }}
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      max-width: 1200px;
      margin: 0 auto;
    }

    .welcome-section {
      margin-bottom: 32px;

      h1 {
        margin: 0 0 8px;
        color: #333;
      }

      p {
        color: #666;
        margin: 0;
      }
    }

    .quick-actions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-bottom: 32px;

      .action-card {
        padding: 24px;
        cursor: pointer;
        transition: transform 0.2s, box-shadow 0.2s;
        text-align: center;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }

        mat-icon {
          font-size: 48px;
          width: 48px;
          height: 48px;
          color: #673ab7;
        }

        h3 {
          margin: 16px 0 8px;
          color: #333;
        }

        p {
          margin: 0;
          color: #666;
          font-size: 14px;
        }
      }
    }

    .stats-section {
      margin-bottom: 32px;

      h2 {
        margin: 0 0 16px;
        color: #333;
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;

      .stat-card {
        padding: 20px;
        text-align: center;

        .stat-value {
          font-size: 36px;
          font-weight: 600;

          &.open { color: #1976d2; }
          &.in-progress { color: #f57c00; }
          &.pending { color: #c2185b; }
          &.resolved { color: #388e3c; }
        }

        .stat-label {
          color: #666;
          font-size: 14px;
          margin-top: 4px;
        }
      }
    }

    .recent-tickets {
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h2 {
          margin: 0;
          color: #333;
        }
      }
    }

    .ticket-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .ticket-card {
        padding: 16px;
        cursor: pointer;
        transition: box-shadow 0.2s;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .ticket-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .ticket-number {
            color: #666;
            font-size: 12px;
          }
        }

        h4 {
          margin: 0 0 8px;
          color: #333;
        }

        .ticket-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          color: #666;
          font-size: 12px;

          span {
            display: flex;
            align-items: center;
            gap: 4px;

            mat-icon {
              font-size: 14px;
              width: 14px;
              height: 14px;
            }
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 48px;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #ccc;
      }

      p {
        color: #666;
        margin: 16px 0;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  user: User | null = null;
  stats: TicketStats | null = null;
  recentTickets: Ticket[] = [];
  isLoading = true;

  constructor(
    private authService: AuthService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => this.user = user);
    this.loadData();
  }

  private loadData(): void {
    this.ticketService.getTicketStats().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.stats = response.data;
        }
      }
    });

    this.ticketService.getMyTickets({}, { page: 0, size: 5, sort: 'createdAt', direction: 'DESC' }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.recentTickets = response.data.content;
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }
}
