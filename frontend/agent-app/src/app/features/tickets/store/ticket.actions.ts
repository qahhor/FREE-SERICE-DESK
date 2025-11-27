import { createAction, props } from '@ngrx/store';
import { Ticket, TicketFilter, CreateTicketRequest, UpdateTicketRequest } from '../../../core/models/ticket.model';

export const loadTickets = createAction(
  '[Ticket] Load Tickets',
  props<{ filter?: TicketFilter }>()
);

export const loadTicketsSuccess = createAction(
  '[Ticket] Load Tickets Success',
  props<{ tickets: Ticket[]; totalElements: number }>()
);

export const loadTicketsFailure = createAction(
  '[Ticket] Load Tickets Failure',
  props<{ error: string }>()
);

export const selectTicket = createAction(
  '[Ticket] Select Ticket',
  props<{ ticketId: string }>()
);

export const loadTicket = createAction(
  '[Ticket] Load Ticket',
  props<{ ticketId: string }>()
);

export const loadTicketSuccess = createAction(
  '[Ticket] Load Ticket Success',
  props<{ ticket: Ticket }>()
);

export const createTicket = createAction(
  '[Ticket] Create Ticket',
  props<{ request: CreateTicketRequest }>()
);

export const createTicketSuccess = createAction(
  '[Ticket] Create Ticket Success',
  props<{ ticket: Ticket }>()
);

export const updateTicket = createAction(
  '[Ticket] Update Ticket',
  props<{ ticketId: string; request: UpdateTicketRequest }>()
);

export const updateTicketSuccess = createAction(
  '[Ticket] Update Ticket Success',
  props<{ ticket: Ticket }>()
);

export const deleteTicket = createAction(
  '[Ticket] Delete Ticket',
  props<{ ticketId: string }>()
);

export const deleteTicketSuccess = createAction(
  '[Ticket] Delete Ticket Success',
  props<{ ticketId: string }>()
);
