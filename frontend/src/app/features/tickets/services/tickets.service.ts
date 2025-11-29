import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { PaginatedResponse } from '@core/models/api-response.model';
import { Ticket, CreateTicketRequest, UpdateTicketRequest, TicketComment } from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketsService {
  private apiService = inject(ApiService);
  private readonly baseEndpoint = '/tickets';

  /**
   * Get all tickets with pagination and filters
   */
  getTickets(params?: {
    page?: number;
    size?: number;
    status?: string;
    priority?: string;
    search?: string;
    assignedAgentId?: string;
  }): Observable<PaginatedResponse<Ticket>> {
    return this.apiService.get<PaginatedResponse<Ticket>>(this.baseEndpoint, params);
  }

  /**
   * Get ticket by ID
   */
  getTicketById(id: string): Observable<Ticket> {
    return this.apiService.get<Ticket>(`${this.baseEndpoint}/${id}`);
  }

  /**
   * Create new ticket
   */
  createTicket(data: CreateTicketRequest): Observable<Ticket> {
    return this.apiService.post<Ticket>(this.baseEndpoint, data);
  }

  /**
   * Update ticket
   */
  updateTicket(id: string, data: UpdateTicketRequest): Observable<Ticket> {
    return this.apiService.put<Ticket>(`${this.baseEndpoint}/${id}`, data);
  }

  /**
   * Delete ticket
   */
  deleteTicket(id: string): Observable<void> {
    return this.apiService.delete<void>(`${this.baseEndpoint}/${id}`);
  }

  /**
   * Assign ticket to agent
   */
  assignTicket(id: string, agentId: string): Observable<Ticket> {
    return this.apiService.post<Ticket>(`${this.baseEndpoint}/${id}/assign`, { agentId });
  }

  /**
   * Close ticket
   */
  closeTicket(id: string): Observable<Ticket> {
    return this.apiService.post<Ticket>(`${this.baseEndpoint}/${id}/close`, {});
  }

  /**
   * Reopen ticket
   */
  reopenTicket(id: string): Observable<Ticket> {
    return this.apiService.post<Ticket>(`${this.baseEndpoint}/${id}/reopen`, {});
  }

  /**
   * Get ticket comments
   */
  getComments(ticketId: string): Observable<TicketComment[]> {
    return this.apiService.get<TicketComment[]>(`${this.baseEndpoint}/${ticketId}/comments`);
  }

  /**
   * Add comment to ticket
   */
  addComment(ticketId: string, content: string, isInternal: boolean = false): Observable<TicketComment> {
    return this.apiService.post<TicketComment>(`${this.baseEndpoint}/${ticketId}/comments`, {
      content,
      isInternal
    });
  }

  /**
   * Upload attachment
   */
  uploadAttachment(ticketId: string, file: File): Observable<any> {
    return this.apiService.uploadFile(`${this.baseEndpoint}/${ticketId}/attachments`, file);
  }
}
