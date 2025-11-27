import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
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
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="forgot-password-container">
      <mat-card class="forgot-password-card">
        <mat-card-header>
          <mat-card-title>{{ 'auth.forgot_password_title' | translate }}</mat-card-title>
          <mat-card-subtitle>{{ 'auth.forgot_password_subtitle' | translate }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div class="success-message" *ngIf="success">
            <mat-icon>check_circle</mat-icon>
            <p>{{ 'auth.reset_email_sent' | translate }}</p>
          </div>

          <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()" *ngIf="!success">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.email' | translate }}</mat-label>
              <input matInput type="email" formControlName="email">
              <mat-icon matSuffix>email</mat-icon>
              <mat-error *ngIf="forgotForm.get('email')?.hasError('required')">
                {{ 'validation.email_required' | translate }}
              </mat-error>
              <mat-error *ngIf="forgotForm.get('email')?.hasError('email')">
                {{ 'validation.email_invalid' | translate }}
              </mat-error>
            </mat-form-field>

            <div class="error-message" *ngIf="error">
              {{ error }}
            </div>

            <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="isLoading || forgotForm.invalid">
              <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
              <span *ngIf="!isLoading">{{ 'auth.send_reset_link' | translate }}</span>
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <a mat-button routerLink="/login">
            <mat-icon>arrow_back</mat-icon>
            {{ 'auth.back_to_login' | translate }}
          </a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .forgot-password-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 200px);
      padding: 24px;
    }

    .forgot-password-card {
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

    .success-message {
      text-align: center;
      padding: 24px;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #4caf50;
      }

      p {
        color: #666;
        margin-top: 16px;
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
    }
  `]
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  isLoading = false;
  success = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotForm.invalid) return;

    this.isLoading = true;
    this.error = '';

    this.authService.forgotPassword(this.forgotForm.value).subscribe({
      next: () => {
        this.success = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'Failed to send reset email. Please try again.';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
