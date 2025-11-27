import { createAction, props } from '@ngrx/store';
import { Ticket, TicketFilter, CreateTicketRequest, AddCommentRequest, TicketComment } from '../../../core/models/ticket.model';
import { PageRequest } from '../../../core/models/api-response.model';

export const loadTickets = createAction(
  '[Ticket] Load Tickets',
  props<{ filter?: TicketFilter; pageRequest?: PageRequest }>()
);

export const loadTicketsSuccess = createAction(
  '[Ticket] Load Tickets Success',
  props<{ tickets: Ticket[]; totalElements: number; totalPages: number; page: number }>()
);

export const loadTicketsFailure = createAction(
  '[Ticket] Load Tickets Failure',
  props<{ error: string }>()
);

export const loadTicket = createAction(
  '[Ticket] Load Ticket',
  props<{ ticketId: string }>()
);

export const loadTicketSuccess = createAction(
  '[Ticket] Load Ticket Success',
  props<{ ticket: Ticket }>()
);

export const loadTicketFailure = createAction(
  '[Ticket] Load Ticket Failure',
  props<{ error: string }>()
);

export const createTicket = createAction(
  '[Ticket] Create Ticket',
  props<{ request: CreateTicketRequest }>()
);

export const createTicketSuccess = createAction(
  '[Ticket] Create Ticket Success',
  props<{ ticket: Ticket }>()
);

export const createTicketFailure = createAction(
  '[Ticket] Create Ticket Failure',
  props<{ error: string }>()
);

export const addComment = createAction(
  '[Ticket] Add Comment',
  props<{ ticketId: string; request: AddCommentRequest }>()
);

export const addCommentSuccess = createAction(
  '[Ticket] Add Comment Success',
  props<{ ticketId: string; comment: TicketComment }>()
);

export const addCommentFailure = createAction(
  '[Ticket] Add Comment Failure',
  props<{ error: string }>()
);

export const updateTicketSuccess = createAction(
  '[Ticket] Update Ticket Success',
  props<{ ticket: Ticket }>()
);

export const selectTicket = createAction(
  '[Ticket] Select Ticket',
  props<{ ticketId: string }>()
);

export const clearSelectedTicket = createAction('[Ticket] Clear Selected Ticket');
