import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { TicketService } from '../../core/services/ticket.service';
import { Ticket, TicketStatus, TicketPriority } from '../../core/models/ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    TranslateModule,
    BaseChartDirective
  ],
  template: `
    <div class="dashboard">
      <h1>{{ 'dashboard.title' | translate }}</h1>

      <!-- Stats Cards -->
      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon open">
              <mat-icon>inbox</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.open }}</span>
              <span class="stat-label">{{ 'dashboard.open.tickets' | translate }}</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon pending">
              <mat-icon>hourglass_empty</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.pending }}</span>
              <span class="stat-label">{{ 'dashboard.pending.tickets' | translate }}</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon resolved">
              <mat-icon>check_circle</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.resolved }}</span>
              <span class="stat-label">{{ 'dashboard.resolved.today' | translate }}</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon response">
              <mat-icon>schedule</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.avgResponseTime }}</span>
              <span class="stat-label">{{ 'dashboard.avg.response.time' | translate }}</span>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Charts Row -->
      <div class="charts-row">
        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>Tickets by Status</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
              [data]="statusChartData"
              [options]="pieChartOptions"
              type="doughnut">
            </canvas>
          </mat-card-content>
        </mat-card>

        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>Tickets Trend (Last 7 Days)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
              [data]="trendChartData"
              [options]="lineChartOptions"
              type="line">
            </canvas>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Recent Tickets -->
      <mat-card class="tickets-card">
        <mat-card-header>
          <mat-card-title>Recent Tickets</mat-card-title>
          <button mat-button color="primary" routerLink="/tickets">View All</button>
        </mat-card-header>
        <mat-card-content>
          <div *ngIf="loading" class="loading">
            <mat-spinner diameter="40"></mat-spinner>
          </div>

          <table mat-table [dataSource]="recentTickets" *ngIf="!loading && recentTickets.length">
            <ng-container matColumnDef="ticketNumber">
              <th mat-header-cell *matHeaderCellDef>Ticket</th>
              <td mat-cell *matCellDef="let ticket">
                <a [routerLink]="['/tickets', ticket.id]">{{ ticket.ticketNumber }}</a>
              </td>
            </ng-container>

            <ng-container matColumnDef="subject">
              <th mat-header-cell *matHeaderCellDef>Subject</th>
              <td mat-cell *matCellDef="let ticket">{{ ticket.subject | slice:0:50 }}...</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let ticket">
                <mat-chip [ngClass]="'status-' + ticket.status.toLowerCase()">
                  {{ ticket.status }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="priority">
              <th mat-header-cell *matHeaderCellDef>Priority</th>
              <td mat-cell *matCellDef="let ticket">
                <span [ngClass]="'priority-' + ticket.priority.toLowerCase()">
                  {{ ticket.priority }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Created</th>
              <td mat-cell *matCellDef="let ticket">{{ ticket.createdAt | date:'short' }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <p *ngIf="!loading && !recentTickets.length" class="no-data">No recent tickets</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard {
      h1 {
        margin-bottom: 24px;
        font-weight: 500;
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }

    .stat-card mat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-icon mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: white;
    }

    .stat-icon.open { background: #2196f3; }
    .stat-icon.pending { background: #ff9800; }
    .stat-icon.resolved { background: #4caf50; }
    .stat-icon.response { background: #9c27b0; }

    .stat-info {
      display: flex;
      flex-direction: column;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 600;
    }

    .stat-label {
      color: #666;
      font-size: 14px;
    }

    .charts-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }

    .chart-card mat-card-content {
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .tickets-card mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 40px;
    }

    .no-data {
      text-align: center;
      color: #666;
      padding: 40px;
    }

    table {
      width: 100%;
    }

    table a {
      color: #1a237e;
      text-decoration: none;
      font-weight: 500;
    }

    table a:hover {
      text-decoration: underline;
    }

    mat-chip {
      font-size: 12px;
    }

    .status-open { background-color: #e3f2fd !important; color: #1565c0 !important; }
    .status-in_progress { background-color: #fff3e0 !important; color: #ef6c00 !important; }
    .status-pending { background-color: #f3e5f5 !important; color: #7b1fa2 !important; }
    .status-resolved { background-color: #e8f5e9 !important; color: #2e7d32 !important; }
    .status-closed { background-color: #eceff1 !important; color: #546e7a !important; }
  `]
})
export class DashboardComponent implements OnInit {
  loading = true;
  recentTickets: Ticket[] = [];
  displayedColumns = ['ticketNumber', 'subject', 'status', 'priority', 'createdAt'];

  stats = {
    open: 0,
    pending: 0,
    resolved: 0,
    avgResponseTime: '2.5h'
  };

  statusChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: ['Open', 'In Progress', 'Pending', 'Resolved', 'Closed'],
    datasets: [{
      data: [12, 8, 5, 15, 25],
      backgroundColor: ['#2196f3', '#ff9800', '#9c27b0', '#4caf50', '#607d8b']
    }]
  };

  trendChartData: ChartConfiguration<'line'>['data'] = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [{
      label: 'New Tickets',
      data: [12, 19, 15, 17, 14, 8, 10],
      borderColor: '#1a237e',
      backgroundColor: 'rgba(26, 35, 126, 0.1)',
      fill: true,
      tension: 0.4
    }, {
      label: 'Resolved',
      data: [10, 15, 12, 18, 16, 7, 9],
      borderColor: '#4caf50',
      backgroundColor: 'rgba(76, 175, 80, 0.1)',
      fill: true,
      tension: 0.4
    }]
  };

  pieChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom'
      }
    }
  };

  lineChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom'
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  constructor(private ticketService: TicketService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.ticketService.getTickets({ size: 10, sortBy: 'createdAt', sortDirection: 'DESC' })
      .subscribe({
        next: (response) => {
          this.recentTickets = response.content;
          this.calculateStats(response.content);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private calculateStats(tickets: Ticket[]): void {
    this.stats.open = tickets.filter(t => t.status === TicketStatus.OPEN).length;
    this.stats.pending = tickets.filter(t => t.status === TicketStatus.PENDING).length;
    this.stats.resolved = tickets.filter(t => t.status === TicketStatus.RESOLVED).length;
  }
}
