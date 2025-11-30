import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';
import { TimeAgoPipe } from '@shared/pipes/time-ago.pipe';
import { TicketsService } from '../services/tickets.service';
import { Ticket, TicketComment } from '../models/ticket.model';

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
    MatChipsModule,
    MatDividerModule,
    LayoutComponent,
    LoadingSpinnerComponent,
    TimeAgoPipe
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss'
})
export class TicketDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private ticketsService = inject(TicketsService);

  ticket: Ticket | null = null;
  comments: TicketComment[] = [];
  loading = true;
  newComment = '';
  addingComment = false;

  ngOnInit(): void {
    const ticketId = this.route.snapshot.paramMap.get('id');
    if (ticketId) {
      this.loadTicket(ticketId);
      this.loadComments(ticketId);
    }
  }

  loadTicket(id: string): void {
    this.loading = true;
    this.ticketsService.getTicketById(id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading ticket:', err);
        this.loading = false;
      }
    });
  }

  loadComments(ticketId: string): void {
    this.ticketsService.getComments(ticketId).subscribe({
      next: (comments) => {
        this.comments = comments;
      },
      error: (err) => {
        console.error('Error loading comments:', err);
      }
    });
  }

  addComment(): void {
    if (!this.newComment.trim() || !this.ticket) {
      return;
    }

    this.addingComment = true;
    this.ticketsService.addComment(this.ticket.id, this.newComment).subscribe({
      next: (comment) => {
        this.comments.push(comment);
        this.newComment = '';
        this.addingComment = false;
      },
      error: (err) => {
        console.error('Error adding comment:', err);
        this.addingComment = false;
      }
    });
  }

  closeTicket(): void {
    if (!this.ticket) return;

    this.ticketsService.closeTicket(this.ticket.id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
      },
      error: (err) => {
        console.error('Error closing ticket:', err);
      }
    });
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace(/_/g, '-')}`;
  }

  getPriorityClass(priority: string): string {
    return `priority-${priority.toLowerCase()}`;
  }
}
