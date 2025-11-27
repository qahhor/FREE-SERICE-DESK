import { createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { Ticket } from '../../../core/models/ticket.model';
import * as TicketActions from './ticket.actions';

export interface TicketState extends EntityState<Ticket> {
  selectedTicketId: string | null;
  isLoading: boolean;
  error: string | null;
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export const ticketAdapter: EntityAdapter<Ticket> = createEntityAdapter<Ticket>({
  selectId: (ticket: Ticket) => ticket.id,
  sortComparer: (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
});

export const initialState: TicketState = ticketAdapter.getInitialState({
  selectedTicketId: null,
  isLoading: false,
  error: null,
  totalElements: 0,
  totalPages: 0,
  currentPage: 0
});

export const ticketReducer = createReducer(
  initialState,
  on(TicketActions.loadTickets, state => ({
    ...state,
    isLoading: true,
    error: null
  })),
  on(TicketActions.loadTicketsSuccess, (state, { tickets, totalElements, totalPages, page }) =>
    ticketAdapter.setAll(tickets, {
      ...state,
      isLoading: false,
      totalElements,
      totalPages,
      currentPage: page
    })
  ),
  on(TicketActions.loadTicketsFailure, (state, { error }) => ({
    ...state,
    isLoading: false,
    error
  })),
  on(TicketActions.loadTicket, state => ({
    ...state,
    isLoading: true
  })),
  on(TicketActions.loadTicketSuccess, (state, { ticket }) =>
    ticketAdapter.upsertOne(ticket, {
      ...state,
      selectedTicketId: ticket.id,
      isLoading: false
    })
  ),
  on(TicketActions.createTicketSuccess, (state, { ticket }) =>
    ticketAdapter.addOne(ticket, {
      ...state,
      totalElements: state.totalElements + 1
    })
  ),
  on(TicketActions.updateTicketSuccess, (state, { ticket }) =>
    ticketAdapter.updateOne({ id: ticket.id, changes: ticket }, state)
  ),
  on(TicketActions.selectTicket, (state, { ticketId }) => ({
    ...state,
    selectedTicketId: ticketId
  })),
  on(TicketActions.clearSelectedTicket, state => ({
    ...state,
    selectedTicketId: null
  }))
);

export const {
  selectIds,
  selectEntities,
  selectAll,
  selectTotal
} = ticketAdapter.getSelectors();
