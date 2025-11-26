import { createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { Ticket } from '../../../core/models/ticket.model';
import * as TicketActions from './ticket.actions';

export interface TicketState extends EntityState<Ticket> {
  selectedTicketId: string | null;
  loading: boolean;
  error: string | null;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}

export const adapter: EntityAdapter<Ticket> = createEntityAdapter<Ticket>({
  selectId: (ticket: Ticket) => ticket.id,
  sortComparer: (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
});

export const initialState: TicketState = adapter.getInitialState({
  selectedTicketId: null,
  loading: false,
  error: null,
  totalElements: 0,
  currentPage: 0,
  pageSize: 20
});

export const ticketReducer = createReducer(
  initialState,
  on(TicketActions.loadTickets, state => ({ ...state, loading: true })),
  on(TicketActions.loadTicketsSuccess, (state, { tickets, totalElements }) =>
    adapter.setAll(tickets, { ...state, loading: false, totalElements })
  ),
  on(TicketActions.loadTicketsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  on(TicketActions.selectTicket, (state, { ticketId }) => ({
    ...state,
    selectedTicketId: ticketId
  })),
  on(TicketActions.createTicketSuccess, (state, { ticket }) =>
    adapter.addOne(ticket, state)
  ),
  on(TicketActions.updateTicketSuccess, (state, { ticket }) =>
    adapter.updateOne({ id: ticket.id, changes: ticket }, state)
  ),
  on(TicketActions.deleteTicketSuccess, (state, { ticketId }) =>
    adapter.removeOne(ticketId, state)
  )
);

export const {
  selectIds,
  selectEntities,
  selectAll,
  selectTotal
} = adapter.getSelectors();
