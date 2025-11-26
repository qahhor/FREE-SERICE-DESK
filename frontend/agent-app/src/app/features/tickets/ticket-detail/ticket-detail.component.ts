import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket, TicketComment, TicketStatus, TicketPriority, CommentType } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatMenuModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="ticket-detail" *ngIf="ticket; else loadingTemplate">
      <!-- Header -->
      <div class="header">
        <div class="header-left">
          <button mat-icon-button routerLink="/tickets">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="ticket-info">
            <span class="ticket-number">{{ ticket.ticketNumber }}</span>
            <h1>{{ ticket.subject }}</h1>
            <div class="meta">
              <mat-chip [ngClass]="'status-' + ticket.status.toLowerCase()">
                {{ ticket.status | titlecase }}
              </mat-chip>
              <span class="priority" [ngClass]="'priority-' + ticket.priority.toLowerCase()">
                {{ ticket.priority }}
              </span>
              <span class="channel">
                <mat-icon>{{ getChannelIcon(ticket.channel) }}</mat-icon>
                {{ ticket.channel }}
              </span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <button mat-stroked-button [matMenuTriggerFor]="statusMenu">
            <mat-icon>edit</mat-icon>
            Change Status
          </button>
          <mat-menu #statusMenu="matMenu">
            <button mat-menu-item *ngFor="let status of statuses" (click)="updateStatus(status)">
              {{ status | titlecase }}
            </button>
          </mat-menu>

          <button mat-stroked-button [matMenuTriggerFor]="actionsMenu">
            <mat-icon>more_vert</mat-icon>
          </button>
          <mat-menu #actionsMenu="matMenu">
            <button mat-menu-item (click)="assignToMe()">
              <mat-icon>person_add</mat-icon>
              Assign to Me
            </button>
            <button mat-menu-item>
              <mat-icon>merge_type</mat-icon>
              Merge
            </button>
            <mat-divider></mat-divider>
            <button mat-menu-item class="danger" (click)="deleteTicket()">
              <mat-icon>delete</mat-icon>
              Delete
            </button>
          </mat-menu>
        </div>
      </div>

      <div class="content">
        <!-- Main Content -->
        <div class="main-panel">
          <mat-card class="description-card">
            <mat-card-header>
              <mat-card-title>Description</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p *ngIf="ticket.description">{{ ticket.description }}</p>
              <p *ngIf="!ticket.description" class="no-description">No description provided</p>
            </mat-card-content>
          </mat-card>

          <!-- Comments Section -->
          <mat-card class="comments-card">
            <mat-card-header>
              <mat-card-title>
                Conversation
                <span class="count">({{ comments.length }})</span>
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <!-- Comment Form -->
              <form [formGroup]="commentForm" (ngSubmit)="submitComment()" class="comment-form">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Add a comment</mat-label>
                  <textarea matInput formControlName="content" rows="3"
                            placeholder="Type your message..."></textarea>
                </mat-form-field>
                <div class="comment-actions">
                  <mat-button-toggle-group formControlName="type" class="comment-type">
                    <mat-button-toggle value="PUBLIC">
                      <mat-icon>public</mat-icon> Public
                    </mat-button-toggle>
                    <mat-button-toggle value="INTERNAL">
                      <mat-icon>lock</mat-icon> Internal
                    </mat-button-toggle>
                  </mat-button-toggle-group>
                  <button mat-raised-button color="primary" type="submit"
                          [disabled]="commentForm.invalid || submitting">
                    <mat-icon>send</mat-icon>
                    Send
                  </button>
                </div>
              </form>

              <mat-divider></mat-divider>

              <!-- Comments List -->
              <div class="comments-list">
                <div *ngFor="let comment of comments" class="comment"
                     [ngClass]="{'internal': comment.type === 'INTERNAL'}">
                  <div class="comment-header">
                    <div class="author">
                      <mat-icon>account_circle</mat-icon>
                      <span class="name">{{ comment.authorName }}</span>
                      <span class="date">{{ comment.createdAt | date:'medium' }}</span>
                    </div>
                    <mat-chip *ngIf="comment.type === 'INTERNAL'" class="internal-badge">
                      Internal Note
                    </mat-chip>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>

                <p *ngIf="!comments.length" class="no-comments">No comments yet</p>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Sidebar -->
        <div class="sidebar">
          <mat-card class="details-card">
            <mat-card-header>
              <mat-card-title>Details</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="detail-row">
                <span class="label">{{ 'ticket.requester' | translate }}</span>
                <div class="value user">
                  <mat-icon>person</mat-icon>
                  <div>
                    <span class="name">{{ ticket.requesterName }}</span>
                    <span class="email">{{ ticket.requesterEmail }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-row">
                <span class="label">{{ 'ticket.assignee' | translate }}</span>
                <div class="value user" *ngIf="ticket.assigneeName">
                  <mat-icon>support_agent</mat-icon>
                  <div>
                    <span class="name">{{ ticket.assigneeName }}</span>
                    <span class="email">{{ ticket.assigneeEmail }}</span>
                  </div>
                </div>
                <span *ngIf="!ticket.assigneeName" class="value unassigned">
                  Unassigned
                </span>
              </div>

              <mat-divider></mat-divider>

              <div class="detail-row">
                <span class="label">{{ 'ticket.priority' | translate }}</span>
                <mat-form-field appearance="outline">
                  <mat-select [(value)]="ticket.priority" (selectionChange)="updatePriority($event.value)">
                    <mat-option *ngFor="let p of priorities" [value]="p">{{ p }}</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="detail-row">
                <span class="label">{{ 'ticket.category' | translate }}</span>
                <span class="value">{{ ticket.categoryName || 'None' }}</span>
              </div>

              <div class="detail-row">
                <span class="label">{{ 'ticket.project' | translate }}</span>
                <span class="value">{{ ticket.projectName }}</span>
              </div>

              <mat-divider></mat-divider>

              <div class="detail-row">
                <span class="label">{{ 'ticket.created' | translate }}</span>
                <span class="value">{{ ticket.createdAt | date:'medium' }}</span>
              </div>

              <div class="detail-row">
                <span class="label">{{ 'ticket.updated' | translate }}</span>
                <span class="value">{{ ticket.updatedAt | date:'medium' }}</span>
              </div>

              <div class="detail-row" *ngIf="ticket.dueDate">
                <span class="label">{{ 'ticket.due.date' | translate }}</span>
                <span class="value" [class.overdue]="isOverdue">
                  {{ ticket.dueDate | date:'medium' }}
                </span>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Tags -->
          <mat-card class="tags-card" *ngIf="ticket.tags?.length">
            <mat-card-header>
              <mat-card-title>{{ 'ticket.tags' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <mat-chip-set>
                <mat-chip *ngFor="let tag of ticket.tags">{{ tag }}</mat-chip>
              </mat-chip-set>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>

    <ng-template #loadingTemplate>
      <div class="loading">
        <mat-spinner diameter="50"></mat-spinner>
      </div>
    </ng-template>
  `,
  styles: [`
    .ticket-detail {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 24px;
        gap: 16px;
      }

      .header-left {
        display: flex;
        gap: 16px;
        align-items: flex-start;
      }

      .ticket-number {
        color: #666;
        font-size: 14px;
      }

      h1 {
        margin: 4px 0 8px;
        font-size: 24px;
        font-weight: 500;
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .priority {
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }

      .channel {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #666;
        font-size: 13px;

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
        }
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .content {
      display: grid;
      grid-template-columns: 1fr 320px;
      gap: 24px;
    }

    .main-panel {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .description-card {
      .no-description {
        color: #999;
        font-style: italic;
      }
    }

    .comments-card {
      .count {
        color: #666;
        font-weight: normal;
        font-size: 14px;
      }
    }

    .comment-form {
      margin-bottom: 24px;

      .full-width {
        width: 100%;
      }

      .comment-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .comment-type {
        font-size: 12px;
      }
    }

    .comments-list {
      margin-top: 24px;
    }

    .comment {
      padding: 16px;
      background: #f9f9f9;
      border-radius: 8px;
      margin-bottom: 16px;

      &.internal {
        background: #fff8e1;
        border-left: 3px solid #ffc107;
      }
    }

    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .author {
        display: flex;
        align-items: center;
        gap: 8px;

        mat-icon {
          color: #666;
        }

        .name {
          font-weight: 500;
        }

        .date {
          color: #999;
          font-size: 12px;
        }
      }

      .internal-badge {
        font-size: 10px;
        background: #ffc107 !important;
      }
    }

    .comment-content {
      white-space: pre-wrap;
    }

    .no-comments {
      text-align: center;
      color: #999;
      padding: 40px;
    }

    .sidebar {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .details-card {
      .detail-row {
        margin-bottom: 16px;

        .label {
          display: block;
          color: #666;
          font-size: 12px;
          margin-bottom: 4px;
        }

        .value {
          font-weight: 500;
        }

        .user {
          display: flex;
          align-items: center;
          gap: 8px;

          mat-icon {
            color: #666;
          }

          .name {
            display: block;
          }

          .email {
            display: block;
            font-size: 12px;
            color: #666;
            font-weight: normal;
          }
        }

        .unassigned {
          color: #999;
          font-style: italic;
        }

        .overdue {
          color: #d32f2f;
        }

        mat-form-field {
          width: 100%;
        }
      }

      mat-divider {
        margin: 16px 0;
      }
    }

    .loading {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 400px;
    }

    .priority-low { background: #e8f5e9; color: #2e7d32; }
    .priority-medium { background: #fff3e0; color: #ef6c00; }
    .priority-high { background: #ffebee; color: #c62828; }
    .priority-urgent { background: #d32f2f; color: white; }

    .status-open { background-color: #e3f2fd !important; color: #1565c0 !important; }
    .status-in_progress { background-color: #fff3e0 !important; color: #ef6c00 !important; }
    .status-pending { background-color: #f3e5f5 !important; color: #7b1fa2 !important; }
    .status-resolved { background-color: #e8f5e9 !important; color: #2e7d32 !important; }
    .status-closed { background-color: #eceff1 !important; color: #546e7a !important; }

    .danger {
      color: #d32f2f !important;
    }

    @media (max-width: 1024px) {
      .content {
        grid-template-columns: 1fr;
      }

      .sidebar {
        order: -1;
      }
    }
  `]
})
export class TicketDetailComponent implements OnInit {
  ticket: Ticket | null = null;
  comments: TicketComment[] = [];
  commentForm: FormGroup;
  submitting = false;

  statuses = Object.values(TicketStatus);
  priorities = Object.values(TicketPriority);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ticketService: TicketService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.commentForm = this.fb.group({
      content: ['', Validators.required],
      type: [CommentType.PUBLIC]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadTicket(id);
      this.loadComments(id);
    }
  }

  loadTicket(id: string): void {
    this.ticketService.getTicket(id).subscribe(ticket => {
      this.ticket = ticket;
    });
  }

  loadComments(ticketId: string): void {
    this.ticketService.getComments(ticketId).subscribe(comments => {
      this.comments = comments;
    });
  }

  submitComment(): void {
    if (this.commentForm.invalid || !this.ticket) return;

    this.submitting = true;
    this.ticketService.addComment(this.ticket.id, this.commentForm.value).subscribe({
      next: (comment) => {
        this.comments.push(comment);
        this.commentForm.reset({ content: '', type: CommentType.PUBLIC });
        this.submitting = false;
        this.snackBar.open('Comment added', 'Close', { duration: 3000 });
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  updateStatus(status: TicketStatus): void {
    if (!this.ticket) return;

    this.ticketService.updateTicket(this.ticket.id, { status }).subscribe(ticket => {
      this.ticket = ticket;
      this.snackBar.open('Status updated', 'Close', { duration: 3000 });
    });
  }

  updatePriority(priority: TicketPriority): void {
    if (!this.ticket) return;

    this.ticketService.updateTicket(this.ticket.id, { priority }).subscribe(ticket => {
      this.ticket = ticket;
      this.snackBar.open('Priority updated', 'Close', { duration: 3000 });
    });
  }

  assignToMe(): void {
    // Implementation needed - get current user ID
  }

  deleteTicket(): void {
    if (!this.ticket || !confirm('Are you sure you want to delete this ticket?')) return;

    this.ticketService.deleteTicket(this.ticket.id).subscribe(() => {
      this.snackBar.open('Ticket deleted', 'Close', { duration: 3000 });
      this.router.navigate(['/tickets']);
    });
  }

  get isOverdue(): boolean {
    if (!this.ticket?.dueDate) return false;
    return new Date(this.ticket.dueDate) < new Date();
  }

  getChannelIcon(channel: string): string {
    const icons: Record<string, string> = {
      'WEB': 'language',
      'EMAIL': 'email',
      'TELEGRAM': 'telegram',
      'WHATSAPP': 'chat',
      'PHONE': 'phone',
      'CHAT': 'forum',
      'API': 'api'
    };
    return icons[channel] || 'help';
  }
}
