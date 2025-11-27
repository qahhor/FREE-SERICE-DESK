import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket, TicketComment } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
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
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="ticket-detail-container" *ngIf="ticket; else loading">
      <div class="header">
        <a mat-icon-button routerLink="/tickets">
          <mat-icon>arrow_back</mat-icon>
        </a>
        <div class="header-info">
          <span class="ticket-number">#{{ ticket.number }}</span>
          <h1>{{ ticket.subject }}</h1>
        </div>
        <div class="header-badges">
          <span class="status-badge" [class]="ticket.status.toLowerCase()">
            {{ 'ticket.status.' + ticket.status.toLowerCase() | translate }}
          </span>
          <span class="priority-badge" [class]="ticket.priority.toLowerCase()">
            {{ ticket.priority }}
          </span>
        </div>
      </div>

      <div class="content-grid">
        <!-- Main Content -->
        <div class="main-content">
          <mat-card class="description-card">
            <mat-card-header>
              <mat-card-title>{{ 'tickets.description' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p class="description">{{ ticket.description }}</p>
            </mat-card-content>
          </mat-card>

          <!-- Attachments -->
          <mat-card *ngIf="ticket.attachments && ticket.attachments.length > 0" class="attachments-card">
            <mat-card-header>
              <mat-card-title>{{ 'tickets.attachments' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="attachment-list">
                <div class="attachment-item" *ngFor="let attachment of ticket.attachments">
                  <mat-icon>description</mat-icon>
                  <span>{{ attachment.fileName }}</span>
                  <button mat-icon-button (click)="downloadAttachment(attachment.id)">
                    <mat-icon>download</mat-icon>
                  </button>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Comments -->
          <mat-card class="comments-card">
            <mat-card-header>
              <mat-card-title>{{ 'tickets.comments' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="comment-list">
                <div *ngFor="let comment of comments"
                     class="comment"
                     [class.customer]="comment.authorType === 'CUSTOMER'"
                     [class.agent]="comment.authorType === 'AGENT'"
                     [class.system]="comment.authorType === 'SYSTEM'">
                  <div class="comment-header">
                    <span class="author">
                      <mat-icon>{{ comment.authorType === 'CUSTOMER' ? 'person' : 'support_agent' }}</mat-icon>
                      {{ comment.authorName }}
                    </span>
                    <span class="time">{{ comment.createdAt | date:'medium' }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                </div>

                <div class="empty-comments" *ngIf="comments.length === 0">
                  <mat-icon>chat_bubble_outline</mat-icon>
                  <p>{{ 'tickets.no_comments' | translate }}</p>
                </div>
              </div>

              <!-- Add Comment -->
              <div class="add-comment" *ngIf="canComment">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'tickets.add_comment' | translate }}</mat-label>
                  <textarea matInput [(ngModel)]="newComment" rows="3" [placeholder]="'tickets.comment_placeholder' | translate"></textarea>
                </mat-form-field>
                <div class="comment-actions">
                  <button mat-raised-button color="primary" (click)="submitComment()" [disabled]="!newComment.trim() || isSubmitting">
                    <mat-spinner *ngIf="isSubmitting" diameter="20"></mat-spinner>
                    <span *ngIf="!isSubmitting">{{ 'tickets.send' | translate }}</span>
                  </button>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Rating -->
          <mat-card *ngIf="ticket.status === 'RESOLVED' && !ticket.rating" class="rating-card">
            <mat-card-header>
              <mat-card-title>{{ 'tickets.rate_support' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <p>{{ 'tickets.rate_message' | translate }}</p>
              <div class="stars">
                <button *ngFor="let star of [1,2,3,4,5]" mat-icon-button (click)="rateTicket(star)">
                  <mat-icon>{{ star <= selectedRating ? 'star' : 'star_border' }}</mat-icon>
                </button>
              </div>
              <mat-form-field appearance="outline" class="full-width" *ngIf="selectedRating > 0">
                <mat-label>{{ 'tickets.feedback' | translate }}</mat-label>
                <textarea matInput [(ngModel)]="feedback" rows="3"></textarea>
              </mat-form-field>
              <button mat-raised-button color="primary" *ngIf="selectedRating > 0" (click)="submitRating()">
                {{ 'tickets.submit_rating' | translate }}
              </button>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Sidebar -->
        <div class="sidebar">
          <mat-card class="info-card">
            <mat-card-header>
              <mat-card-title>{{ 'tickets.details' | translate }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="info-row">
                <span class="label">{{ 'tickets.created' | translate }}</span>
                <span class="value">{{ ticket.createdAt | date:'medium' }}</span>
              </div>
              <div class="info-row">
                <span class="label">{{ 'tickets.updated' | translate }}</span>
                <span class="value">{{ ticket.updatedAt | date:'medium' }}</span>
              </div>
              <div class="info-row" *ngIf="ticket.categoryName">
                <span class="label">{{ 'tickets.category' | translate }}</span>
                <span class="value">{{ ticket.categoryName }}</span>
              </div>
              <div class="info-row">
                <span class="label">{{ 'tickets.type' | translate }}</span>
                <span class="value">{{ 'ticket.type.' + ticket.type.toLowerCase() | translate }}</span>
              </div>
              <div class="info-row" *ngIf="ticket.assignedAgentName">
                <span class="label">{{ 'tickets.assigned_to' | translate }}</span>
                <span class="value">{{ ticket.assignedAgentName }}</span>
              </div>
              <div class="info-row" *ngIf="ticket.rating">
                <span class="label">{{ 'tickets.your_rating' | translate }}</span>
                <span class="value stars">
                  <mat-icon *ngFor="let s of [1,2,3,4,5]">{{ s <= ticket.rating! ? 'star' : 'star_border' }}</mat-icon>
                </span>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>

    <ng-template #loading>
      <div class="loading">
        <mat-spinner diameter="40"></mat-spinner>
      </div>
    </ng-template>
  `,
  styles: [`
    .ticket-detail-container {
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 24px;

      .header-info {
        flex: 1;

        .ticket-number {
          color: #666;
          font-size: 14px;
        }

        h1 {
          margin: 4px 0 0;
          color: #333;
        }
      }

      .header-badges {
        display: flex;
        gap: 8px;
      }
    }

    .content-grid {
      display: grid;
      grid-template-columns: 1fr 300px;
      gap: 24px;
    }

    .main-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .description {
      white-space: pre-wrap;
      line-height: 1.6;
    }

    .attachment-list {
      .attachment-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px;
        background: #f5f5f5;
        border-radius: 4px;
        margin-bottom: 8px;

        span { flex: 1; }
      }
    }

    .comment-list {
      .comment {
        padding: 16px;
        border-radius: 8px;
        margin-bottom: 12px;

        &.customer {
          background: #e8f5e9;
          margin-left: 48px;
        }

        &.agent {
          background: #e3f2fd;
          margin-right: 48px;
        }

        &.system {
          background: #fafafa;
          text-align: center;
          font-style: italic;
        }

        .comment-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;
          font-size: 13px;

          .author {
            display: flex;
            align-items: center;
            gap: 4px;
            font-weight: 500;

            mat-icon {
              font-size: 16px;
              width: 16px;
              height: 16px;
            }
          }

          .time { color: #666; }
        }

        .comment-content {
          white-space: pre-wrap;
        }
      }
    }

    .empty-comments {
      text-align: center;
      padding: 32px;
      color: #666;

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        opacity: 0.5;
      }
    }

    .add-comment {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #eee;

      .comment-actions {
        display: flex;
        justify-content: flex-end;
      }
    }

    .rating-card {
      .stars {
        margin: 16px 0;

        button { color: #ffc107; }
      }
    }

    .info-card {
      .info-row {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child { border-bottom: none; }

        .label {
          color: #666;
          font-size: 13px;
        }

        .value {
          font-weight: 500;

          &.stars {
            display: flex;
            color: #ffc107;

            mat-icon {
              font-size: 16px;
              width: 16px;
              height: 16px;
            }
          }
        }
      }
    }

    .full-width { width: 100%; }

    .loading {
      display: flex;
      justify-content: center;
      padding: 48px;
    }

    @media (max-width: 900px) {
      .content-grid {
        grid-template-columns: 1fr;
      }

      .sidebar {
        order: -1;
      }

      .comment-list .comment {
        &.customer, &.agent {
          margin-left: 0;
          margin-right: 0;
        }
      }
    }
  `]
})
export class TicketDetailComponent implements OnInit {
  @Input() id!: string;

  ticket: Ticket | null = null;
  comments: TicketComment[] = [];
  newComment = '';
  isSubmitting = false;
  selectedRating = 0;
  feedback = '';

  constructor(
    private ticketService: TicketService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTicket();
  }

  get canComment(): boolean {
    return this.ticket?.status !== 'CLOSED' && this.ticket?.status !== 'CANCELLED';
  }

  private loadTicket(): void {
    this.ticketService.getTicket(this.id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.ticket = response.data;
          this.comments = response.data.comments || [];
        }
      }
    });
  }

  submitComment(): void {
    if (!this.newComment.trim()) return;

    this.isSubmitting = true;
    this.ticketService.addComment(this.id, { content: this.newComment }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.comments.push(response.data);
          this.newComment = '';
          this.snackBar.open('Comment added', 'Close', { duration: 3000 });
        }
      },
      error: () => {
        this.snackBar.open('Failed to add comment', 'Close', { duration: 5000 });
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }

  rateTicket(stars: number): void {
    this.selectedRating = stars;
  }

  submitRating(): void {
    this.ticketService.rateTicket(this.id, {
      rating: this.selectedRating,
      feedback: this.feedback
    }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.ticket = response.data;
          this.snackBar.open('Thank you for your feedback!', 'Close', { duration: 3000 });
        }
      }
    });
  }

  downloadAttachment(attachmentId: string): void {
    this.ticketService.downloadAttachment(this.id, attachmentId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'attachment';
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
