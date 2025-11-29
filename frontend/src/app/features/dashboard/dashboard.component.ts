import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';
import { ApiService } from '@core/services/api.service';

interface DashboardStats {
  totalTickets: number;
  openTickets: number;
  resolvedTickets: number;
  avgResponseTime: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    LayoutComponent,
    LoadingSpinnerComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);

  loading = true;
  stats: DashboardStats = {
    totalTickets: 0,
    openTickets: 0,
    resolvedTickets: 0,
    avgResponseTime: '0h'
  };

  recentTickets: any[] = [];

  statCards = [
    {
      title: 'Total Tickets',
      value: 0,
      icon: 'confirmation_number',
      color: '#3f51b5',
      key: 'totalTickets'
    },
    {
      title: 'Open Tickets',
      value: 0,
      icon: 'inbox',
      color: '#ff9800',
      key: 'openTickets'
    },
    {
      title: 'Resolved',
      value: 0,
      icon: 'check_circle',
      color: '#4caf50',
      key: 'resolvedTickets'
    },
    {
      title: 'Avg Response Time',
      value: '0h',
      icon: 'schedule',
      color: '#9c27b0',
      key: 'avgResponseTime'
    }
  ];

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load dashboard stats
    this.apiService.get<any>('/analytics/dashboard').subscribe({
      next: (response) => {
        this.stats = response.data || response;
        this.updateStatCards();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        // Use mock data for demo
        this.stats = {
          totalTickets: 1234,
          openTickets: 42,
          resolvedTickets: 987,
          avgResponseTime: '2.5h'
        };
        this.updateStatCards();
        this.loading = false;
      }
    });

    // Load recent tickets
    this.apiService.get<any>('/tickets', { page: 0, size: 5, sort: 'createdAt,desc' }).subscribe({
      next: (response) => {
        this.recentTickets = response.content || response.data || [];
      },
      error: (err) => {
        console.error('Error loading recent tickets:', err);
        this.recentTickets = [];
      }
    });
  }

  updateStatCards(): void {
    this.statCards = this.statCards.map(card => ({
      ...card,
      value: this.stats[card.key as keyof DashboardStats]
    }));
  }
}
