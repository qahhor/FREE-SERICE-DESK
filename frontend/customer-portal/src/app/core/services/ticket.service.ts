import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, PageResponse, PageRequest } from '../models/api-response.model';
import {
  Ticket,
  TicketComment,
  CreateTicketRequest,
  AddCommentRequest,
  TicketRatingRequest,
  TicketFilter,
  Category
} from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private basePath = '/api/v1/customer/tickets';

  constructor(private api: ApiService) {}

  getMyTickets(filter?: TicketFilter, pageRequest?: PageRequest): Observable<ApiResponse<PageResponse<Ticket>>> {
    let params = new HttpParams();

    if (filter) {
      if (filter.status && filter.status.length > 0) {
        params = params.set('status', filter.status.join(','));
      }
      if (filter.priority && filter.priority.length > 0) {
        params = params.set('priority', filter.priority.join(','));
      }
      if (filter.type && filter.type.length > 0) {
        params = params.set('type', filter.type.join(','));
      }
      if (filter.dateFrom) {
        params = params.set('dateFrom', filter.dateFrom.toISOString());
      }
      if (filter.dateTo) {
        params = params.set('dateTo', filter.dateTo.toISOString());
      }
      if (filter.search) {
        params = params.set('search', filter.search);
      }
    }

    return this.api.getPage<Ticket>(this.basePath, pageRequest, params);
  }

  getTicket(id: string): Observable<ApiResponse<Ticket>> {
    return this.api.get<Ticket>(`${this.basePath}/${id}`);
  }

  createTicket(request: CreateTicketRequest): Observable<ApiResponse<Ticket>> {
    if (request.attachments && request.attachments.length > 0) {
      const formData = new FormData();
      formData.append('subject', request.subject);
      formData.append('description', request.description);
      if (request.priority) formData.append('priority', request.priority);
      if (request.type) formData.append('type', request.type);
      if (request.categoryId) formData.append('categoryId', request.categoryId);

      request.attachments.forEach((file, index) => {
        formData.append('attachments', file, file.name);
      });

      return this.api.upload<Ticket>(this.basePath, formData);
    }

    return this.api.post<Ticket>(this.basePath, request);
  }

  addComment(ticketId: string, request: AddCommentRequest): Observable<ApiResponse<TicketComment>> {
    if (request.attachments && request.attachments.length > 0) {
      const formData = new FormData();
      formData.append('content', request.content);

      request.attachments.forEach((file) => {
        formData.append('attachments', file, file.name);
      });

      return this.api.upload<TicketComment>(`${this.basePath}/${ticketId}/comments`, formData);
    }

    return this.api.post<TicketComment>(`${this.basePath}/${ticketId}/comments`, request);
  }

  getComments(ticketId: string): Observable<ApiResponse<TicketComment[]>> {
    return this.api.get<TicketComment[]>(`${this.basePath}/${ticketId}/comments`);
  }

  rateTicket(ticketId: string, request: TicketRatingRequest): Observable<ApiResponse<Ticket>> {
    return this.api.post<Ticket>(`${this.basePath}/${ticketId}/rate`, request);
  }

  reopenTicket(ticketId: string, reason: string): Observable<ApiResponse<Ticket>> {
    return this.api.post<Ticket>(`${this.basePath}/${ticketId}/reopen`, { reason });
  }

  cancelTicket(ticketId: string, reason: string): Observable<ApiResponse<Ticket>> {
    return this.api.post<Ticket>(`${this.basePath}/${ticketId}/cancel`, { reason });
  }

  getCategories(): Observable<ApiResponse<Category[]>> {
    return this.api.get<Category[]>('/api/v1/categories');
  }

  getTicketStats(): Observable<ApiResponse<TicketStats>> {
    return this.api.get<TicketStats>(`${this.basePath}/stats`);
  }

  downloadAttachment(ticketId: string, attachmentId: string): Observable<Blob> {
    return this.api.download(`${this.basePath}/${ticketId}/attachments/${attachmentId}`);
  }
}

export interface TicketStats {
  total: number;
  open: number;
  inProgress: number;
  pending: number;
  resolved: number;
  closed: number;
  averageResolutionTime?: number;
  averageResponseTime?: number;
}
