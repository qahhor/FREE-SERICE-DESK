import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>{{ 'auth.login_title' | translate }}</mat-card-title>
          <mat-card-subtitle>{{ 'auth.login_subtitle' | translate }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.email' | translate }}</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email">
              <mat-icon matSuffix>email</mat-icon>
              <mat-error *ngIf="loginForm.get('email')?.hasError('required')">
                {{ 'validation.email_required' | translate }}
              </mat-error>
              <mat-error *ngIf="loginForm.get('email')?.hasError('email')">
                {{ 'validation.email_invalid' | translate }}
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.password' | translate }}</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="password" autocomplete="current-password">
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                {{ 'validation.password_required' | translate }}
              </mat-error>
            </mat-form-field>

            <div class="form-options">
              <mat-checkbox formControlName="rememberMe">{{ 'auth.remember_me' | translate }}</mat-checkbox>
              <a routerLink="/forgot-password">{{ 'auth.forgot_password' | translate }}</a>
            </div>

            <div class="error-message" *ngIf="error">
              {{ error }}
            </div>

            <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="isLoading || loginForm.invalid">
              <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
              <span *ngIf="!isLoading">{{ 'auth.login' | translate }}</span>
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <p class="register-link">
            {{ 'auth.no_account' | translate }}
            <a routerLink="/register">{{ 'auth.register' | translate }}</a>
          </p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 200px);
      padding: 24px;
    }

    .login-card {
      max-width: 400px;
      width: 100%;

      mat-card-header {
        display: block;
        text-align: center;
        padding-bottom: 16px;

        mat-card-title {
          font-size: 24px;
          margin-bottom: 8px;
        }
      }
    }

    .full-width {
      width: 100%;
    }

    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 8px 0 16px;

      a {
        color: #673ab7;
        text-decoration: none;
        font-size: 14px;

        &:hover {
          text-decoration: underline;
        }
      }
    }

    .error-message {
      background: #ffebee;
      color: #c62828;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 16px;
      font-size: 14px;
    }

    button[type="submit"] {
      height: 48px;
      font-size: 16px;

      mat-spinner {
        display: inline-block;
        margin-right: 8px;
      }
    }

    mat-card-actions {
      text-align: center;
      padding-top: 16px;

      .register-link {
        margin: 0;
        color: #666;

        a {
          color: #673ab7;
          text-decoration: none;
          font-weight: 500;

          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  hidePassword = true;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.error = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
          this.router.navigateByUrl(returnUrl);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'Login failed. Please check your credentials.';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
