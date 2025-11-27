import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, exhaustMap, catchError, tap } from 'rxjs/operators';
import { TicketService } from '../../../core/services/ticket.service';
import * as TicketActions from './ticket.actions';

@Injectable()
export class TicketEffects {
  loadTickets$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.loadTickets),
      exhaustMap(action =>
        this.ticketService.getMyTickets(action.filter, action.pageRequest).pipe(
          map(response => TicketActions.loadTicketsSuccess({
            tickets: response.data!.content,
            totalElements: response.data!.totalElements,
            totalPages: response.data!.totalPages,
            page: response.data!.page
          })),
          catchError(error => of(TicketActions.loadTicketsFailure({
            error: error.error?.message || 'Failed to load tickets'
          })))
        )
      )
    )
  );

  loadTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.loadTicket),
      exhaustMap(action =>
        this.ticketService.getTicket(action.ticketId).pipe(
          map(response => TicketActions.loadTicketSuccess({ ticket: response.data! })),
          catchError(error => of(TicketActions.loadTicketFailure({
            error: error.error?.message || 'Failed to load ticket'
          })))
        )
      )
    )
  );

  createTicket$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.createTicket),
      exhaustMap(action =>
        this.ticketService.createTicket(action.request).pipe(
          map(response => TicketActions.createTicketSuccess({ ticket: response.data! })),
          catchError(error => of(TicketActions.createTicketFailure({
            error: error.error?.message || 'Failed to create ticket'
          })))
        )
      )
    )
  );

  createTicketSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.createTicketSuccess),
      tap(action => this.router.navigate(['/tickets', action.ticket.id]))
    ),
    { dispatch: false }
  );

  addComment$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TicketActions.addComment),
      exhaustMap(action =>
        this.ticketService.addComment(action.ticketId, action.request).pipe(
          map(response => TicketActions.addCommentSuccess({
            ticketId: action.ticketId,
            comment: response.data!
          })),
          catchError(error => of(TicketActions.addCommentFailure({
            error: error.error?.message || 'Failed to add comment'
          })))
        )
      )
    )
  );

  constructor(
    private actions$: Actions,
    private ticketService: TicketService,
    private router: Router
  ) {}
}
