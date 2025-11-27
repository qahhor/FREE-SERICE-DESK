import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';
import { TicketService } from '../../../core/services/ticket.service';
import * as TicketActions from './ticket.actions';

@Injectable()
export class TicketEffects {
  loadTickets$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.loadTickets),
      mergeMap(({ filter }) =>
        this.ticketService.getTickets(filter || {}).pipe(
          map(response => TicketActions.loadTicketsSuccess({
            tickets: response.content,
            totalElements: response.totalElements
          })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  loadTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.loadTicket),
      mergeMap(({ ticketId }) =>
        this.ticketService.getTicket(ticketId).pipe(
          map(ticket => TicketActions.loadTicketSuccess({ ticket })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  createTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.createTicket),
      mergeMap(({ request }) =>
        this.ticketService.createTicket(request).pipe(
          map(ticket => TicketActions.createTicketSuccess({ ticket })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  updateTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.updateTicket),
      mergeMap(({ ticketId, request }) =>
        this.ticketService.updateTicket(ticketId, request).pipe(
          map(ticket => TicketActions.updateTicketSuccess({ ticket })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  deleteTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.deleteTicket),
      mergeMap(({ ticketId }) =>
        this.ticketService.deleteTicket(ticketId).pipe(
          map(() => TicketActions.deleteTicketSuccess({ ticketId })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.message
          })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private ticketService: TicketService
  ) {}
}
