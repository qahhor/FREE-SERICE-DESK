import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
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
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatSidenavModule,
    MatListModule,
    MatDividerModule,
    TranslateModule
  ],
  template: `
    <div class="layout-container">
      <!-- Header -->
      <mat-toolbar color="primary" class="header">
        <div class="header-content">
          <a routerLink="/" class="logo">
            <mat-icon>support_agent</mat-icon>
            <span>Support Portal</span>
          </a>

          <nav class="nav-links" *ngIf="!isMobile">
            <a mat-button routerLink="/knowledge-base" routerLinkActive="active">
              <mat-icon>library_books</mat-icon>
              {{ 'nav.knowledge_base' | translate }}
            </a>
            <ng-container *ngIf="isAuthenticated">
              <a mat-button routerLink="/tickets" routerLinkActive="active">
                <mat-icon>confirmation_number</mat-icon>
                {{ 'nav.my_tickets' | translate }}
              </a>
              <a mat-button routerLink="/tickets/new" class="new-ticket-btn">
                <mat-icon>add</mat-icon>
                {{ 'nav.new_ticket' | translate }}
              </a>
            </ng-container>
          </nav>

          <div class="header-actions">
            <!-- Language selector -->
            <button mat-icon-button [matMenuTriggerFor]="langMenu">
              <mat-icon>language</mat-icon>
            </button>
            <mat-menu #langMenu="matMenu">
              <button mat-menu-item (click)="changeLanguage('en')">English</button>
              <button mat-menu-item (click)="changeLanguage('ru')">Русский</button>
              <button mat-menu-item (click)="changeLanguage('uz')">O'zbek</button>
              <button mat-menu-item (click)="changeLanguage('kk')">Қазақ</button>
              <button mat-menu-item (click)="changeLanguage('ar')">العربية</button>
            </mat-menu>

            <!-- User menu -->
            <ng-container *ngIf="isAuthenticated; else loginButton">
              <button mat-icon-button [matMenuTriggerFor]="userMenu">
                <mat-icon>account_circle</mat-icon>
              </button>
              <mat-menu #userMenu="matMenu">
                <div class="user-info" *ngIf="currentUser">
                  <strong>{{ currentUser.firstName }} {{ currentUser.lastName }}</strong>
                  <small>{{ currentUser.email }}</small>
                </div>
                <mat-divider></mat-divider>
                <button mat-menu-item routerLink="/dashboard">
                  <mat-icon>dashboard</mat-icon>
                  {{ 'nav.dashboard' | translate }}
                </button>
                <button mat-menu-item routerLink="/profile">
                  <mat-icon>person</mat-icon>
                  {{ 'nav.profile' | translate }}
                </button>
                <mat-divider></mat-divider>
                <button mat-menu-item (click)="logout()">
                  <mat-icon>logout</mat-icon>
                  {{ 'nav.logout' | translate }}
                </button>
              </mat-menu>
            </ng-container>
            <ng-template #loginButton>
              <a mat-button routerLink="/login">
                <mat-icon>login</mat-icon>
                {{ 'nav.login' | translate }}
              </a>
            </ng-template>

            <!-- Mobile menu -->
            <button mat-icon-button *ngIf="isMobile" [matMenuTriggerFor]="mobileMenu">
              <mat-icon>menu</mat-icon>
            </button>
            <mat-menu #mobileMenu="matMenu">
              <a mat-menu-item routerLink="/knowledge-base">
                <mat-icon>library_books</mat-icon>
                {{ 'nav.knowledge_base' | translate }}
              </a>
              <ng-container *ngIf="isAuthenticated">
                <a mat-menu-item routerLink="/tickets">
                  <mat-icon>confirmation_number</mat-icon>
                  {{ 'nav.my_tickets' | translate }}
                </a>
                <a mat-menu-item routerLink="/tickets/new">
                  <mat-icon>add</mat-icon>
                  {{ 'nav.new_ticket' | translate }}
                </a>
              </ng-container>
            </mat-menu>
          </div>
        </div>
      </mat-toolbar>

      <!-- Main content -->
      <main class="main-content">
        <ng-content></ng-content>
      </main>

      <!-- Footer -->
      <footer class="footer">
        <div class="footer-content">
          <p>&copy; {{ currentYear }} Service Desk. {{ 'footer.rights' | translate }}</p>
          <div class="footer-links">
            <a routerLink="/knowledge-base">{{ 'footer.help' | translate }}</a>
            <a href="#">{{ 'footer.privacy' | translate }}</a>
            <a href="#">{{ 'footer.terms' | translate }}</a>
          </div>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .layout-container {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .header {
      position: sticky;
      top: 0;
      z-index: 1000;
    }

    .header-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 16px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      color: white;
      text-decoration: none;
      font-size: 20px;
      font-weight: 500;

      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
    }

    .nav-links {
      display: flex;
      gap: 8px;

      a {
        color: rgba(255, 255, 255, 0.9);

        &.active {
          background: rgba(255, 255, 255, 0.1);
        }

        &.new-ticket-btn {
          background: rgba(255, 255, 255, 0.2);

          &:hover {
            background: rgba(255, 255, 255, 0.3);
          }
        }
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-info {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;

      strong {
        font-size: 14px;
      }

      small {
        color: #666;
        font-size: 12px;
      }
    }

    .main-content {
      flex: 1;
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
      width: 100%;
      box-sizing: border-box;
    }

    .footer {
      background: #333;
      color: white;
      padding: 24px;
      margin-top: auto;
    }

    .footer-content {
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;

      p {
        margin: 0;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.7);
      }
    }

    .footer-links {
      display: flex;
      gap: 24px;

      a {
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        font-size: 14px;

        &:hover {
          color: white;
        }
      }
    }

    @media (max-width: 768px) {
      .main-content {
        padding: 16px;
      }

      .footer-content {
        flex-direction: column;
        gap: 16px;
        text-align: center;
      }
    }
  `]
})
export class LayoutComponent implements OnInit {
  currentUser: User | null = null;
  isAuthenticated = false;
  isMobile = false;
  currentYear = new Date().getFullYear();

  constructor(
    private authService: AuthService,
    private translate: TranslateService,
    private router: Router
  ) {
    this.checkMobile();
    window.addEventListener('resize', () => this.checkMobile());
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.isAuthenticated = !!user;
    });
  }

  private checkMobile(): void {
    this.isMobile = window.innerWidth < 768;
  }

  changeLanguage(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem('language', lang);

    // Handle RTL for Arabic
    document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
  }

  logout(): void {
    this.authService.logout();
  }
}
