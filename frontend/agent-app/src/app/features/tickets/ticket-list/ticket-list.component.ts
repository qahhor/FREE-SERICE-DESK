import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket, TicketStatus, TicketPriority, TicketFilter } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    TranslateModule
  ],
  template: `
    <div class="ticket-list">
      <div class="header">
        <h1>{{ 'ticket.title' | translate }}</h1>
        <button mat-raised-button color="primary" routerLink="/tickets/new">
          <mat-icon>add</mat-icon>
          {{ 'ticket.create' | translate }}
        </button>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <form [formGroup]="filterForm" class="filters">
          <mat-form-field appearance="outline">
            <mat-label>{{ 'common.search' | translate }}</mat-label>
            <input matInput formControlName="search" placeholder="Search tickets...">
            <mat-icon matPrefix>search</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>{{ 'ticket.status' | translate }}</mat-label>
            <mat-select formControlName="statuses" multiple>
              <mat-option *ngFor="let status of statuses" [value]="status">
                {{ status }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>{{ 'ticket.priority' | translate }}</mat-label>
            <mat-select formControlName="priorities" multiple>
              <mat-option *ngFor="let priority of priorities" [value]="priority">
                {{ priority }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-button (click)="resetFilters()">
            <mat-icon>clear</mat-icon>
            Clear Filters
          </button>
        </form>
      </mat-card>

      <!-- Quick Filters -->
      <div class="quick-filters">
        <mat-chip-listbox>
          <mat-chip-option (click)="applyQuickFilter('all')" [selected]="activeQuickFilter === 'all'">
            All Tickets
          </mat-chip-option>
          <mat-chip-option (click)="applyQuickFilter('my')" [selected]="activeQuickFilter === 'my'">
            My Tickets
          </mat-chip-option>
          <mat-chip-option (click)="applyQuickFilter('unassigned')" [selected]="activeQuickFilter === 'unassigned'">
            Unassigned
          </mat-chip-option>
          <mat-chip-option (click)="applyQuickFilter('overdue')" [selected]="activeQuickFilter === 'overdue'">
            Overdue
          </mat-chip-option>
        </mat-chip-listbox>
      </div>

      <!-- Tickets Table -->
      <mat-card>
        <div *ngIf="loading" class="loading">
          <mat-spinner diameter="40"></mat-spinner>
        </div>

        <table mat-table [dataSource]="dataSource" matSort *ngIf="!loading">
          <ng-container matColumnDef="ticketNumber">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Ticket #</th>
            <td mat-cell *matCellDef="let ticket">
              <a [routerLink]="['/tickets', ticket.id]" class="ticket-link">
                {{ ticket.ticketNumber }}
              </a>
            </td>
          </ng-container>

          <ng-container matColumnDef="subject">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'ticket.subject' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              <div class="subject-cell">
                <span class="subject">{{ ticket.subject }}</span>
                <div class="tags" *ngIf="ticket.tags?.length">
                  <mat-chip *ngFor="let tag of ticket.tags.slice(0, 2)" class="tag-chip">
                    {{ tag }}
                  </mat-chip>
                </div>
              </div>
            </td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'ticket.status' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              <mat-chip [ngClass]="'status-' + ticket.status.toLowerCase()">
                {{ ticket.status | titlecase }}
              </mat-chip>
            </td>
          </ng-container>

          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'ticket.priority' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              <span class="priority-badge" [ngClass]="'priority-' + ticket.priority.toLowerCase()">
                <mat-icon *ngIf="ticket.priority === 'URGENT'">priority_high</mat-icon>
                {{ ticket.priority | titlecase }}
              </span>
            </td>
          </ng-container>

          <ng-container matColumnDef="requester">
            <th mat-header-cell *matHeaderCellDef>{{ 'ticket.requester' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              <div class="user-cell">
                <mat-icon>person</mat-icon>
                {{ ticket.requesterName }}
              </div>
            </td>
          </ng-container>

          <ng-container matColumnDef="assignee">
            <th mat-header-cell *matHeaderCellDef>{{ 'ticket.assignee' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              <div class="user-cell" *ngIf="ticket.assigneeName">
                <mat-icon>support_agent</mat-icon>
                {{ ticket.assigneeName }}
              </div>
              <span *ngIf="!ticket.assigneeName" class="unassigned">Unassigned</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'ticket.created' | translate }}</th>
            <td mat-cell *matCellDef="let ticket">
              {{ ticket.createdAt | date:'short' }}
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let ticket">
              <button mat-icon-button [matMenuTriggerFor]="actionMenu" matTooltip="Actions">
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #actionMenu="matMenu">
                <button mat-menu-item [routerLink]="['/tickets', ticket.id]">
                  <mat-icon>visibility</mat-icon>
                  View Details
                </button>
                <button mat-menu-item (click)="assignToMe(ticket)">
                  <mat-icon>person_add</mat-icon>
                  Assign to Me
                </button>
                <button mat-menu-item (click)="closeTicket(ticket)">
                  <mat-icon>check_circle</mat-icon>
                  Close Ticket
                </button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="ticket-row"></tr>
        </table>

        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 25, 50, 100]"
          (page)="onPageChange($event)">
        </mat-paginator>
      </mat-card>
    </div>
  `,
  styles: [`
    .ticket-list {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;

        h1 {
          margin: 0;
          font-weight: 500;
        }
      }
    }

    .filters-card {
      margin-bottom: 16px;
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

    .quick-filters {
      margin-bottom: 16px;
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 60px;
    }

    table {
      width: 100%;
    }

    .ticket-link {
      color: #1a237e;
      text-decoration: none;
      font-weight: 500;
    }

    .ticket-link:hover {
      text-decoration: underline;
    }

    .ticket-row {
      cursor: pointer;
    }

    .ticket-row:hover {
      background-color: #f5f5f5;
    }

    .subject-cell {
      .subject {
        display: block;
        max-width: 300px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .tags {
        display: flex;
        gap: 4px;
        margin-top: 4px;
      }

      .tag-chip {
        font-size: 10px;
        min-height: 20px;
        padding: 0 8px;
      }
    }

    .user-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: #666;
      }
    }

    .unassigned {
      color: #999;
      font-style: italic;
    }

    .priority-badge {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 500;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }
    }

    .priority-low { background: #e8f5e9; color: #2e7d32; }
    .priority-medium { background: #fff3e0; color: #ef6c00; }
    .priority-high { background: #ffebee; color: #c62828; }
    .priority-urgent { background: #d32f2f; color: white; }

    mat-chip {
      font-size: 11px;
    }

    .status-open { background-color: #e3f2fd !important; color: #1565c0 !important; }
    .status-in_progress { background-color: #fff3e0 !important; color: #ef6c00 !important; }
    .status-pending { background-color: #f3e5f5 !important; color: #7b1fa2 !important; }
    .status-resolved { background-color: #e8f5e9 !important; color: #2e7d32 !important; }
    .status-closed { background-color: #eceff1 !important; color: #546e7a !important; }
  `]
})
export class TicketListComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<Ticket>();
  displayedColumns = ['ticketNumber', 'subject', 'status', 'priority', 'requester', 'assignee', 'createdAt', 'actions'];

  filterForm: FormGroup;
  loading = true;
  totalElements = 0;
  pageSize = 20;
  activeQuickFilter = 'all';

  statuses = Object.values(TicketStatus);
  priorities = Object.values(TicketPriority);

  constructor(
    private ticketService: TicketService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      search: [''],
      statuses: [[]],
      priorities: [[]]
    });
  }

  ngOnInit(): void {
    this.loadTickets();

    this.filterForm.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => this.loadTickets());
  }

  loadTickets(): void {
    this.loading = true;
    const filter: TicketFilter = {
      ...this.filterForm.value,
      page: this.paginator?.pageIndex || 0,
      size: this.paginator?.pageSize || this.pageSize,
      sortBy: this.sort?.active || 'createdAt',
      sortDirection: (this.sort?.direction?.toUpperCase() as 'ASC' | 'DESC') || 'DESC'
    };

    this.ticketService.getTickets(filter).subscribe({
      next: (response) => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  applyQuickFilter(filter: string): void {
    this.activeQuickFilter = filter;
    this.filterForm.patchValue({
      search: '',
      statuses: filter === 'all' ? [] : ['OPEN', 'IN_PROGRESS'],
      priorities: []
    });

    if (filter === 'unassigned') {
      // Load unassigned tickets
      this.ticketService.getUnassignedTickets().subscribe(response => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
      });
    } else if (filter === 'overdue') {
      this.ticketService.getOverdueTickets().subscribe(response => {
        this.dataSource.data = response.content;
        this.totalElements = response.totalElements;
      });
    } else {
      this.loadTickets();
    }
  }

  resetFilters(): void {
    this.filterForm.reset({ search: '', statuses: [], priorities: [] });
    this.activeQuickFilter = 'all';
  }

  onPageChange(event: any): void {
    this.loadTickets();
  }

  assignToMe(ticket: Ticket): void {
    // Get current user ID from auth service
    // this.ticketService.assignTicket(ticket.id, currentUserId).subscribe(...);
  }

  closeTicket(ticket: Ticket): void {
    this.ticketService.closeTicket(ticket.id).subscribe(() => {
      this.loadTickets();
    });
  }
}
