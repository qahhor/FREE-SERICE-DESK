import { Routes } from '@angular/router';

export const TICKETS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./tickets-list/tickets-list.component').then(m => m.TicketsListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./ticket-create/ticket-create.component').then(m => m.TicketCreateComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent)
  }
];
