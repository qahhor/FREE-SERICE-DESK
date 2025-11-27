import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { PageResponse } from '../models/api-response.model';
import {
  Ticket,
  TicketComment,
  TicketAttachment,
  CreateTicketRequest,
  UpdateTicketRequest,
  TicketFilter,
  CreateCommentRequest
} from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private basePath = '/tickets';

  constructor(private api: ApiService) {}

  getTickets(filter: TicketFilter = {}): Observable<PageResponse<Ticket>> {
    return this.api.getPage<Ticket>(this.basePath, filter);
  }

  getTicket(id: string): Observable<Ticket> {
    return this.api.get<Ticket>(`${this.basePath}/${id}`);
  }

  getTicketByNumber(ticketNumber: string): Observable<Ticket> {
    return this.api.get<Ticket>(`${this.basePath}/number/${ticketNumber}`);
  }

  createTicket(request: CreateTicketRequest): Observable<Ticket> {
    return this.api.post<Ticket>(this.basePath, request);
  }

  updateTicket(id: string, request: UpdateTicketRequest): Observable<Ticket> {
    return this.api.patch<Ticket>(`${this.basePath}/${id}`, request);
  }

  deleteTicket(id: string): Observable<void> {
    return this.api.delete<void>(`${this.basePath}/${id}`);
  }

  assignTicket(id: string, assigneeId: string): Observable<Ticket> {
    return this.api.post<Ticket>(`${this.basePath}/${id}/assign?assigneeId=${assigneeId}`);
  }

  closeTicket(id: string): Observable<Ticket> {
    return this.api.post<Ticket>(`${this.basePath}/${id}/close`);
  }

  // Comments
  getComments(ticketId: string): Observable<TicketComment[]> {
    return this.api.get<TicketComment[]>(`${this.basePath}/${ticketId}/comments`);
  }

  addComment(ticketId: string, request: CreateCommentRequest): Observable<TicketComment> {
    return this.api.post<TicketComment>(`${this.basePath}/${ticketId}/comments`, request);
  }

  // Watchers
  addWatcher(ticketId: string, userId: string): Observable<Ticket> {
    return this.api.post<Ticket>(`${this.basePath}/${ticketId}/watchers?userId=${userId}`);
  }

  removeWatcher(ticketId: string, userId: string): Observable<Ticket> {
    return this.api.delete<Ticket>(`${this.basePath}/${ticketId}/watchers/${userId}`);
  }

  // Stats
  getMyOpenTickets(): Observable<PageResponse<Ticket>> {
    return this.getTickets({
      statuses: ['OPEN', 'IN_PROGRESS', 'PENDING'] as any,
      sortBy: 'createdAt',
      sortDirection: 'DESC',
      size: 100
    });
  }

  getUnassignedTickets(): Observable<PageResponse<Ticket>> {
    return this.getTickets({
      unassigned: true,
      statuses: ['OPEN'] as any,
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    });
  }

  getOverdueTickets(): Observable<PageResponse<Ticket>> {
    return this.getTickets({
      overdue: true,
      sortBy: 'dueDate',
      sortDirection: 'ASC'
    });
  }
}
