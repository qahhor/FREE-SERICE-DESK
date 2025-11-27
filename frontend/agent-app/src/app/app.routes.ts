import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'tickets',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/tickets/ticket-list/ticket-list.component').then(m => m.TicketListComponent)
      },
      {
        path: 'new',
        loadComponent: () => import('./features/tickets/ticket-create/ticket-create.component').then(m => m.TicketCreateComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./features/tickets/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent)
      }
    ]
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/user-list/user-list.component').then(m => m.UserListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
