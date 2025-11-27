import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule,
    TranslateModule
  ],
  template: `
    <mat-sidenav-container class="sidenav-container" *ngIf="isAuthenticated; else loginContent">
      <!-- Sidebar -->
      <mat-sidenav #sidenav mode="side" opened class="sidenav">
        <div class="logo">
          <mat-icon>support_agent</mat-icon>
          <span>ServiceDesk</span>
        </div>

        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>{{ 'dashboard.title' | translate }}</span>
          </a>

          <a mat-list-item routerLink="/tickets" routerLinkActive="active">
            <mat-icon matListItemIcon>confirmation_number</mat-icon>
            <span matListItemTitle>{{ 'ticket.title' | translate }}</span>
          </a>

          <a mat-list-item routerLink="/tickets/new">
            <mat-icon matListItemIcon>add_circle</mat-icon>
            <span matListItemTitle>{{ 'ticket.create' | translate }}</span>
          </a>

          <mat-divider></mat-divider>

          <a mat-list-item routerLink="/users" routerLinkActive="active" *ngIf="isAgentOrAbove">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>{{ 'user.profile' | translate }}s</span>
          </a>

          <a mat-list-item routerLink="/settings" routerLinkActive="active">
            <mat-icon matListItemIcon>settings</mat-icon>
            <span matListItemTitle>{{ 'user.settings' | translate }}</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>

      <!-- Main content -->
      <mat-sidenav-content class="main-content">
        <!-- Top toolbar -->
        <mat-toolbar color="primary" class="toolbar">
          <button mat-icon-button (click)="sidenav.toggle()">
            <mat-icon>menu</mat-icon>
          </button>

          <span class="spacer"></span>

          <!-- Language selector -->
          <button mat-icon-button [matMenuTriggerFor]="langMenu">
            <mat-icon>language</mat-icon>
          </button>
          <mat-menu #langMenu="matMenu">
            <button mat-menu-item (click)="switchLanguage('en')">English</button>
            <button mat-menu-item (click)="switchLanguage('ru')">Русский</button>
            <button mat-menu-item (click)="switchLanguage('uz')">O'zbek</button>
            <button mat-menu-item (click)="switchLanguage('kk')">Қазақша</button>
            <button mat-menu-item (click)="switchLanguage('ar')">العربية</button>
          </mat-menu>

          <!-- Theme toggle -->
          <button mat-icon-button (click)="toggleTheme()">
            <mat-icon>{{ isDarkTheme ? 'light_mode' : 'dark_mode' }}</mat-icon>
          </button>

          <!-- Notifications -->
          <button mat-icon-button>
            <mat-icon matBadge="3" matBadgeColor="warn">notifications</mat-icon>
          </button>

          <!-- User menu -->
          <button mat-button [matMenuTriggerFor]="userMenu" class="user-button">
            <mat-icon>account_circle</mat-icon>
            <span class="user-name">{{ currentUser?.fullName }}</span>
            <mat-icon>arrow_drop_down</mat-icon>
          </button>
          <mat-menu #userMenu="matMenu">
            <button mat-menu-item routerLink="/settings">
              <mat-icon>person</mat-icon>
              <span>{{ 'user.profile' | translate }}</span>
            </button>
            <button mat-menu-item routerLink="/settings">
              <mat-icon>settings</mat-icon>
              <span>{{ 'user.settings' | translate }}</span>
            </button>
            <mat-divider></mat-divider>
            <button mat-menu-item (click)="logout()">
              <mat-icon>exit_to_app</mat-icon>
              <span>{{ 'auth.logout' | translate }}</span>
            </button>
          </mat-menu>
        </mat-toolbar>

        <!-- Page content -->
        <div class="page-content">
          <ng-content></ng-content>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>

    <ng-template #loginContent>
      <ng-content></ng-content>
    </ng-template>
  `,
  styles: [`
    .sidenav-container {
      height: 100%;
    }

    .sidenav {
      width: 260px;
      background: #1a237e;
      color: white;
    }

    .dark-theme .sidenav {
      background: #0d47a1;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 20px;
      font-size: 20px;
      font-weight: 600;
    }

    .logo mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .sidenav mat-nav-list a {
      color: rgba(255, 255, 255, 0.8);
    }

    .sidenav mat-nav-list a:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .sidenav mat-nav-list a.active {
      background: rgba(255, 255, 255, 0.15);
      color: white;
    }

    .sidenav mat-divider {
      border-color: rgba(255, 255, 255, 0.2);
      margin: 8px 0;
    }

    .main-content {
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    .toolbar {
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .spacer {
      flex: 1;
    }

    .user-button {
      text-transform: none;
    }

    .user-name {
      margin: 0 8px;
    }

    .page-content {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
    }

    @media (max-width: 768px) {
      .user-name {
        display: none;
      }
    }
  `]
})
export class LayoutComponent implements OnInit {
  currentUser: User | null = null;
  isDarkTheme = false;

  constructor(
    private authService: AuthService,
    private translate: TranslateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
    this.isDarkTheme = localStorage.getItem('darkMode') === 'true';
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get isAgentOrAbove(): boolean {
    const role = this.currentUser?.role;
    return role === 'ADMIN' || role === 'MANAGER' || role === 'AGENT';
  }

  switchLanguage(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem('lang', lang);

    if (lang === 'ar') {
      document.documentElement.dir = 'rtl';
    } else {
      document.documentElement.dir = 'ltr';
    }
  }

  toggleTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    document.body.classList.toggle('dark-theme', this.isDarkTheme);
    localStorage.setItem('darkMode', String(this.isDarkTheme));
  }

  logout(): void {
    this.authService.logout();
  }
}
