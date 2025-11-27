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
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
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
    path: 'knowledge-base',
    children: [
      {
        path: '',
        loadComponent: () => import('./features/knowledge-base/kb-home/kb-home.component').then(m => m.KbHomeComponent)
      },
      {
        path: 'search',
        loadComponent: () => import('./features/knowledge-base/kb-search/kb-search.component').then(m => m.KbSearchComponent)
      },
      {
        path: 'category/:id',
        loadComponent: () => import('./features/knowledge-base/kb-category/kb-category.component').then(m => m.KbCategoryComponent)
      },
      {
        path: 'article/:id',
        loadComponent: () => import('./features/knowledge-base/kb-article/kb-article.component').then(m => m.KbArticleComponent)
      }
    ]
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },
  {
    path: 'chat',
    loadComponent: () => import('./features/chat/chat.component').then(m => m.ChatComponent)
  },
  {
    path: '**',
    loadComponent: () => import('./shared/components/not-found/not-found.component').then(m => m.NotFoundComponent)
  }
];
