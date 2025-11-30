import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { TimeAgoPipe } from '@shared/pipes/time-ago.pipe';
import { TicketsService } from '../services/tickets.service';
import { Ticket, TicketStatus, TicketPriority } from '../models/ticket.model';

@Component({
  selector: 'app-tickets-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    LayoutComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    TimeAgoPipe
  ],
  templateUrl: './tickets-list.component.html',
  styleUrl: './tickets-list.component.scss'
})
export class TicketsListComponent implements OnInit {
  private ticketsService = inject(TicketsService);

  tickets: Ticket[] = [];
  loading = true;
  displayedColumns: string[] = ['id', 'title', 'status', 'priority', 'customer', 'assignee', 'createdAt', 'actions'];

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  // Filters
  searchQuery = '';
  statusFilter = '';
  priorityFilter = '';

  statuses = Object.values(TicketStatus);
  priorities = Object.values(TicketPriority);

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loading = true;

    const params: any = {
      page: this.pageIndex,
      size: this.pageSize
    };

    if (this.searchQuery) {
      params.search = this.searchQuery;
    }
    if (this.statusFilter) {
      params.status = this.statusFilter;
    }
    if (this.priorityFilter) {
      params.priority = this.priorityFilter;
    }

    this.ticketsService.getTickets(params).subscribe({
      next: (response) => {
        this.tickets = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading tickets:', err);
        this.loading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTickets();
  }

  onSearch(): void {
    this.pageIndex = 0;
    this.loadTickets();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.loadTickets();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.statusFilter = '';
    this.priorityFilter = '';
    this.pageIndex = 0;
    this.loadTickets();
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace(/_/g, '-')}`;
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }
}
