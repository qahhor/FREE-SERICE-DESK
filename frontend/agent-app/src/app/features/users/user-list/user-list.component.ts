import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FormsModule } from '@angular/forms';
import { User, UserRole, UserStatus } from '../../../core/models/user.model';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatMenuModule,
    MatDialogModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="user-list-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ 'user.title' | translate }}</mat-card-title>
          <div class="header-actions">
            <button mat-raised-button color="primary" (click)="openCreateDialog()">
              <mat-icon>add</mat-icon>
              {{ 'user.create' | translate }}
            </button>
          </div>
        </mat-card-header>

        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline">
              <mat-label>{{ 'common.search' | translate }}</mat-label>
              <input matInput [(ngModel)]="searchQuery" (input)="applyFilter()">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>{{ 'user.role' | translate }}</mat-label>
              <mat-select [(ngModel)]="roleFilter" (selectionChange)="applyFilter()">
                <mat-option [value]="null">All</mat-option>
                <mat-option *ngFor="let role of roles" [value]="role">{{ role }}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>{{ 'user.status' | translate }}</mat-label>
              <mat-select [(ngModel)]="statusFilter" (selectionChange)="applyFilter()">
                <mat-option [value]="null">All</mat-option>
                <mat-option *ngFor="let status of statuses" [value]="status">{{ status }}</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <table mat-table [dataSource]="dataSource" matSort class="users-table">
            <ng-container matColumnDef="avatar">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let user">
                <div class="user-avatar" [style.background-color]="getAvatarColor(user)">
                  {{ getInitials(user) }}
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'user.first.name' | translate }}</th>
              <td mat-cell *matCellDef="let user">
                {{ user.firstName }} {{ user.lastName }}
              </td>
            </ng-container>

            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'auth.email' | translate }}</th>
              <td mat-cell *matCellDef="let user">{{ user.email }}</td>
            </ng-container>

            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'user.role' | translate }}</th>
              <td mat-cell *matCellDef="let user">
                <mat-chip [color]="getRoleColor(user.role)" selected>
                  {{ user.role }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ 'user.status' | translate }}</th>
              <td mat-cell *matCellDef="let user">
                <span class="status-badge" [class]="'status-' + user.status.toLowerCase()">
                  {{ user.status }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="team">
              <th mat-header-cell *matHeaderCellDef>{{ 'ticket.team' | translate }}</th>
              <td mat-cell *matCellDef="let user">{{ user.team?.name || '-' }}</td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let user">
                <button mat-icon-button [matMenuTriggerFor]="menu">
                  <mat-icon>more_vert</mat-icon>
                </button>
                <mat-menu #menu="matMenu">
                  <button mat-menu-item (click)="editUser(user)">
                    <mat-icon>edit</mat-icon>
                    <span>{{ 'common.edit' | translate }}</span>
                  </button>
                  <button mat-menu-item (click)="toggleStatus(user)">
                    <mat-icon>{{ user.status === 'ACTIVE' ? 'block' : 'check_circle' }}</mat-icon>
                    <span>{{ user.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}</span>
                  </button>
                  <button mat-menu-item (click)="deleteUser(user)" class="delete-action">
                    <mat-icon>delete</mat-icon>
                    <span>{{ 'common.delete' | translate }}</span>
                  </button>
                </mat-menu>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <mat-paginator [pageSizeOptions]="[10, 25, 50, 100]"
                         showFirstLastButtons>
          </mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .user-list-container {
      padding: 24px;
    }

    mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
    }

    .filters mat-form-field {
      min-width: 200px;
    }

    .users-table {
      width: 100%;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 500;
      font-size: 14px;
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 16px;
      font-size: 12px;
      font-weight: 500;
    }

    .status-active {
      background-color: #e8f5e9;
      color: #2e7d32;
    }

    .status-inactive {
      background-color: #fafafa;
      color: #757575;
    }

    .status-suspended {
      background-color: #ffebee;
      color: #c62828;
    }

    .delete-action {
      color: #f44336;
    }
  `]
})
export class UserListComponent implements OnInit {
  displayedColumns: string[] = ['avatar', 'name', 'email', 'role', 'status', 'team', 'actions'];
  dataSource = new MatTableDataSource<User>([]);

  searchQuery = '';
  roleFilter: UserRole | null = null;
  statusFilter: UserStatus | null = null;

  roles = Object.values(UserRole);
  statuses = Object.values(UserStatus);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private avatarColors = [
    '#1976d2', '#388e3c', '#d32f2f', '#7b1fa2', '#c2185b',
    '#0288d1', '#00796b', '#f57c00', '#5d4037', '#455a64'
  ];

  constructor(
    private apiService: ApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadUsers(): void {
    this.apiService.get<User[]>('/users').subscribe({
      next: (users) => {
        this.dataSource.data = users;
      },
      error: (error) => {
        console.error('Failed to load users:', error);
      }
    });
  }

  applyFilter(): void {
    this.dataSource.filterPredicate = (data: User, filter: string) => {
      const matchesSearch = !this.searchQuery ||
        data.firstName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        data.lastName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        data.email.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesRole = !this.roleFilter || data.role === this.roleFilter;
      const matchesStatus = !this.statusFilter || data.status === this.statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    };
    this.dataSource.filter = 'trigger';
  }

  getInitials(user: User): string {
    return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
  }

  getAvatarColor(user: User): string {
    const index = user.id.charCodeAt(0) % this.avatarColors.length;
    return this.avatarColors[index];
  }

  getRoleColor(role: UserRole): string {
    switch (role) {
      case UserRole.ADMIN: return 'warn';
      case UserRole.MANAGER: return 'accent';
      case UserRole.AGENT: return 'primary';
      default: return '';
    }
  }

  openCreateDialog(): void {
    // TODO: Implement user creation dialog
  }

  editUser(user: User): void {
    // TODO: Implement user edit
  }

  toggleStatus(user: User): void {
    const newStatus = user.status === UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE;
    this.apiService.patch<User>(`/users/${user.id}/status`, { status: newStatus }).subscribe({
      next: () => {
        user.status = newStatus;
        this.snackBar.open(this.translate.instant('common.success'), '', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open(this.translate.instant('common.error'), '', { duration: 3000 });
      }
    });
  }

  deleteUser(user: User): void {
    if (confirm(this.translate.instant('common.confirm.delete'))) {
      this.apiService.delete(`/users/${user.id}`).subscribe({
        next: () => {
          this.dataSource.data = this.dataSource.data.filter(u => u.id !== user.id);
          this.snackBar.open(this.translate.instant('common.success'), '', { duration: 3000 });
        },
        error: (error) => {
          this.snackBar.open(this.translate.instant('common.error'), '', { duration: 3000 });
        }
      });
    }
  }
}
