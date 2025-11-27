import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Ticket, TicketFilter, TicketStatus, TicketPriority } from '../../../core/models/ticket.model';
import * as TicketActions from '../store/ticket.actions';
import { TicketState, selectAll } from '../store/ticket.reducer';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="ticket-list-container">
      <div class="header">
        <h1>{{ 'tickets.my_tickets' | translate }}</h1>
        <a mat-raised-button color="primary" routerLink="/tickets/new">
          <mat-icon>add</mat-icon>
          {{ 'tickets.new_ticket' | translate }}
        </a>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <div class="filters">
          <mat-form-field appearance="outline">
            <mat-label>{{ 'tickets.search' | translate }}</mat-label>
            <input matInput [(ngModel)]="filter.search" (keyup.enter)="applyFilter()" placeholder="{{ 'tickets.search_placeholder' | translate }}">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>{{ 'tickets.status' | translate }}</mat-label>
            <mat-select [(ngModel)]="filter.status" multiple (selectionChange)="applyFilter()">
              <mat-option value="OPEN">{{ 'ticket.status.open' | translate }}</mat-option>
              <mat-option value="IN_PROGRESS">{{ 'ticket.status.in_progress' | translate }}</mat-option>
              <mat-option value="PENDING">{{ 'ticket.status.pending' | translate }}</mat-option>
              <mat-option value="RESOLVED">{{ 'ticket.status.resolved' | translate }}</mat-option>
              <mat-option value="CLOSED">{{ 'ticket.status.closed' | translate }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>{{ 'tickets.priority' | translate }}</mat-label>
            <mat-select [(ngModel)]="filter.priority" multiple (selectionChange)="applyFilter()">
              <mat-option value="LOW">{{ 'ticket.priority.low' | translate }}</mat-option>
              <mat-option value="MEDIUM">{{ 'ticket.priority.medium' | translate }}</mat-option>
              <mat-option value="HIGH">{{ 'ticket.priority.high' | translate }}</mat-option>
              <mat-option value="URGENT">{{ 'ticket.priority.urgent' | translate }}</mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-icon-button (click)="clearFilters()" *ngIf="hasFilters">
            <mat-icon>clear</mat-icon>
          </button>
        </div>
      </mat-card>

      <!-- Loading -->
      <div class="loading" *ngIf="isLoading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <!-- Ticket List -->
      <div class="tickets" *ngIf="!isLoading">
        <mat-card *ngFor="let ticket of tickets" class="ticket-card" [routerLink]="['/tickets', ticket.id]">
          <div class="ticket-main">
            <div class="ticket-info">
              <span class="ticket-number">#{{ ticket.number }}</span>
              <h3>{{ ticket.subject }}</h3>
              <p class="description">{{ ticket.description | slice:0:100 }}{{ ticket.description.length > 100 ? '...' : '' }}</p>
            </div>
            <div class="ticket-badges">
              <span class="status-badge" [class]="ticket.status.toLowerCase()">
                {{ 'ticket.status.' + ticket.status.toLowerCase() | translate }}
              </span>
              <span class="priority-badge" [class]="ticket.priority.toLowerCase()">
                {{ ticket.priority }}
              </span>
            </div>
          </div>
          <div class="ticket-meta">
            <span><mat-icon>calendar_today</mat-icon> {{ ticket.createdAt | date:'medium' }}</span>
            <span *ngIf="ticket.assignedAgentName"><mat-icon>person</mat-icon> {{ ticket.assignedAgentName }}</span>
            <span *ngIf="ticket.categoryName"><mat-icon>folder</mat-icon> {{ ticket.categoryName }}</span>
          </div>
        </mat-card>

        <!-- Empty State -->
        <div class="empty-state" *ngIf="tickets.length === 0">
          <mat-icon>inbox</mat-icon>
          <h3>{{ 'tickets.no_tickets' | translate }}</h3>
          <p>{{ 'tickets.no_tickets_message' | translate }}</p>
          <a mat-raised-button color="primary" routerLink="/tickets/new">
            {{ 'tickets.create_first' | translate }}
          </a>
        </div>
      </div>

      <!-- Pagination -->
      <mat-paginator
        *ngIf="totalElements > 0"
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageIndex]="currentPage"
        [pageSizeOptions]="[10, 25, 50]"
        (page)="onPageChange($event)"
        showFirstLastButtons>
      </mat-paginator>
    </div>
  `,
  styles: [`
    .ticket-list-container {
      max-width: 1000px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      h1 {
        margin: 0;
        color: #333;
      }
    }

    .filters-card {
      margin-bottom: 24px;
      padding: 16px;
    }

    .filters {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      align-items: center;

      mat-form-field {
        flex: 1;
        min-width: 200px;
      }
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 48px;
    }

    .tickets {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }

    .ticket-card {
      padding: 16px;
      cursor: pointer;
      transition: box-shadow 0.2s, transform 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }
    }

    .ticket-main {
      display: flex;
      justify-content: space-between;
      gap: 16px;
    }

    .ticket-info {
      flex: 1;

      .ticket-number {
        color: #666;
        font-size: 12px;
      }

      h3 {
        margin: 4px 0 8px;
        color: #333;
      }

      .description {
        color: #666;
        font-size: 14px;
        margin: 0;
      }
    }

    .ticket-badges {
      display: flex;
      flex-direction: column;
      gap: 8px;
      align-items: flex-end;
    }

    .ticket-meta {
      display: flex;
      gap: 24px;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #eee;
      color: #666;
      font-size: 13px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
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

      h3 {
        margin: 16px 0 8px;
        color: #333;
      }

      p {
        color: #666;
        margin-bottom: 24px;
      }
    }

    mat-paginator {
      background: transparent;
    }
  `]
})
export class TicketListComponent implements OnInit {
  tickets: Ticket[] = [];
  isLoading = true;
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;

  filter: TicketFilter = {
    status: [],
    priority: [],
    search: ''
  };

  constructor(private store: Store<{ tickets: TicketState }>) {}

  ngOnInit(): void {
    this.store.select(state => state.tickets).subscribe(state => {
      this.tickets = selectAll(state);
      this.isLoading = state.isLoading;
      this.totalElements = state.totalElements;
      this.currentPage = state.currentPage;
    });

    this.loadTickets();
  }

  get hasFilters(): boolean {
    return !!(this.filter.search || this.filter.status?.length || this.filter.priority?.length);
  }

  loadTickets(): void {
    this.store.dispatch(TicketActions.loadTickets({
      filter: this.filter,
      pageRequest: {
        page: this.currentPage,
        size: this.pageSize,
        sort: 'createdAt',
        direction: 'DESC'
      }
    }));
  }

  applyFilter(): void {
    this.currentPage = 0;
    this.loadTickets();
  }

  clearFilters(): void {
    this.filter = { status: [], priority: [], search: '' };
    this.applyFilter();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTickets();
  }
}
