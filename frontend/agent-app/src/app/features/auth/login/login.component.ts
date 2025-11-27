import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
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
    ReactiveFormsModule,
    MatCardModule,
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
          <div class="logo">
            <mat-icon>support_agent</mat-icon>
            <h1>ServiceDesk</h1>
          </div>
          <p class="subtitle">{{ 'auth.login' | translate }}</p>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.email' | translate }}</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email">
              <mat-icon matPrefix>email</mat-icon>
              <mat-error *ngIf="loginForm.get('email')?.hasError('required')">
                {{ 'validation.required' | translate }}
              </mat-error>
              <mat-error *ngIf="loginForm.get('email')?.hasError('email')">
                {{ 'validation.email.invalid' | translate }}
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.password' | translate }}</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'"
                     formControlName="password" autocomplete="current-password">
              <mat-icon matPrefix>lock</mat-icon>
              <button mat-icon-button matSuffix type="button"
                      (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                {{ 'validation.required' | translate }}
              </mat-error>
            </mat-form-field>

            <div class="options">
              <mat-checkbox formControlName="rememberMe">
                {{ 'auth.remember.me' | translate }}
              </mat-checkbox>
              <a href="#" class="forgot-password">{{ 'auth.forgot.password' | translate }}</a>
            </div>

            <button mat-raised-button color="primary" type="submit"
                    class="full-width submit-btn" [disabled]="loading || loginForm.invalid">
              <mat-spinner *ngIf="loading" diameter="20"></mat-spinner>
              <span *ngIf="!loading">{{ 'auth.login' | translate }}</span>
            </button>

            <p class="error-message" *ngIf="errorMessage">{{ errorMessage }}</p>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1a237e 0%, #3949ab 100%);
    }

    .login-card {
      width: 100%;
      max-width: 400px;
      padding: 32px;
      margin: 16px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      justify-content: center;
      width: 100%;
      margin-bottom: 8px;
    }

    .logo mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #1a237e;
    }

    .logo h1 {
      font-size: 28px;
      font-weight: 600;
      color: #1a237e;
      margin: 0;
    }

    .subtitle {
      text-align: center;
      color: #666;
      margin-bottom: 24px;
    }

    .full-width {
      width: 100%;
    }

    .options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .forgot-password {
      color: #1a237e;
      text-decoration: none;
      font-size: 14px;
    }

    .forgot-password:hover {
      text-decoration: underline;
    }

    .submit-btn {
      height: 48px;
      font-size: 16px;
    }

    .submit-btn mat-spinner {
      display: inline-block;
    }

    .error-message {
      color: #f44336;
      text-align: center;
      margin-top: 16px;
    }

    mat-card-header {
      display: block;
      text-align: center;
    }
  `]
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  loading = false;
  errorMessage = '';

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

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Login failed. Please try again.';
      }
    });
  }
}
