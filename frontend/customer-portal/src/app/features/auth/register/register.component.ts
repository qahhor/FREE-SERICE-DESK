import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
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
  selector: 'app-register',
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
    <div class="register-container">
      <mat-card class="register-card">
        <mat-card-header>
          <mat-card-title>{{ 'auth.register_title' | translate }}</mat-card-title>
          <mat-card-subtitle>{{ 'auth.register_subtitle' | translate }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            <div class="name-fields">
              <mat-form-field appearance="outline">
                <mat-label>{{ 'auth.first_name' | translate }}</mat-label>
                <input matInput formControlName="firstName">
                <mat-error *ngIf="registerForm.get('firstName')?.hasError('required')">
                  {{ 'validation.first_name_required' | translate }}
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>{{ 'auth.last_name' | translate }}</mat-label>
                <input matInput formControlName="lastName">
                <mat-error *ngIf="registerForm.get('lastName')?.hasError('required')">
                  {{ 'validation.last_name_required' | translate }}
                </mat-error>
              </mat-form-field>
            </div>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.email' | translate }}</mat-label>
              <input matInput type="email" formControlName="email">
              <mat-icon matSuffix>email</mat-icon>
              <mat-error *ngIf="registerForm.get('email')?.hasError('required')">
                {{ 'validation.email_required' | translate }}
              </mat-error>
              <mat-error *ngIf="registerForm.get('email')?.hasError('email')">
                {{ 'validation.email_invalid' | translate }}
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.phone' | translate }}</mat-label>
              <input matInput type="tel" formControlName="phone">
              <mat-icon matSuffix>phone</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.password' | translate }}</mat-label>
              <input matInput [type]="hidePassword ? 'password' : 'text'" formControlName="password">
              <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="registerForm.get('password')?.hasError('required')">
                {{ 'validation.password_required' | translate }}
              </mat-error>
              <mat-error *ngIf="registerForm.get('password')?.hasError('minlength')">
                {{ 'validation.password_min_length' | translate }}
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.confirm_password' | translate }}</mat-label>
              <input matInput [type]="hideConfirmPassword ? 'password' : 'text'" formControlName="confirmPassword">
              <button mat-icon-button matSuffix type="button" (click)="hideConfirmPassword = !hideConfirmPassword">
                <mat-icon>{{ hideConfirmPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="registerForm.get('confirmPassword')?.hasError('required')">
                {{ 'validation.confirm_password_required' | translate }}
              </mat-error>
              <mat-error *ngIf="registerForm.hasError('passwordMismatch')">
                {{ 'validation.passwords_not_match' | translate }}
              </mat-error>
            </mat-form-field>

            <div class="error-message" *ngIf="error">
              {{ error }}
            </div>

            <button mat-raised-button color="primary" type="submit" class="full-width" [disabled]="isLoading || registerForm.invalid">
              <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
              <span *ngIf="!isLoading">{{ 'auth.create_account' | translate }}</span>
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <p class="login-link">
            {{ 'auth.have_account' | translate }}
            <a routerLink="/login">{{ 'auth.login' | translate }}</a>
          </p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 200px);
      padding: 24px;
    }

    .register-card {
      max-width: 450px;
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

    .name-fields {
      display: flex;
      gap: 16px;

      mat-form-field {
        flex: 1;
      }
    }

    .full-width {
      width: 100%;
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
      margin-top: 8px;

      mat-spinner {
        display: inline-block;
        margin-right: 8px;
      }
    }

    mat-card-actions {
      text-align: center;
      padding-top: 16px;

      .login-link {
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
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  hidePassword = true;
  hideConfirmPassword = true;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.error = '';

    const { confirmPassword, ...registerData } = this.registerForm.value;

    this.authService.register(registerData).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'Registration failed. Please try again.';
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
