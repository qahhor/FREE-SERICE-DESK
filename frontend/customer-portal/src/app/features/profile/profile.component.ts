import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="profile-container">
      <h1>{{ 'profile.title' | translate }}</h1>

      <mat-tab-group>
        <!-- Personal Info -->
        <mat-tab [label]="'profile.personal_info' | translate">
          <mat-card>
            <mat-card-content>
              <form [formGroup]="profileForm" (ngSubmit)="updateProfile()">
                <div class="form-row">
                  <mat-form-field appearance="outline">
                    <mat-label>{{ 'auth.first_name' | translate }}</mat-label>
                    <input matInput formControlName="firstName">
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>{{ 'auth.last_name' | translate }}</mat-label>
                    <input matInput formControlName="lastName">
                  </mat-form-field>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'auth.email' | translate }}</mat-label>
                  <input matInput formControlName="email" disabled>
                  <mat-icon matSuffix>email</mat-icon>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'auth.phone' | translate }}</mat-label>
                  <input matInput formControlName="phone">
                  <mat-icon matSuffix>phone</mat-icon>
                </mat-form-field>

                <div class="form-row">
                  <mat-form-field appearance="outline">
                    <mat-label>{{ 'profile.language' | translate }}</mat-label>
                    <mat-select formControlName="language">
                      <mat-option value="en">English</mat-option>
                      <mat-option value="ru">Русский</mat-option>
                      <mat-option value="uz">O'zbek</mat-option>
                      <mat-option value="kk">Қазақ</mat-option>
                      <mat-option value="ar">العربية</mat-option>
                    </mat-select>
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>{{ 'profile.timezone' | translate }}</mat-label>
                    <mat-select formControlName="timezone">
                      <mat-option value="UTC">UTC</mat-option>
                      <mat-option value="Asia/Tashkent">Asia/Tashkent</mat-option>
                      <mat-option value="Europe/Moscow">Europe/Moscow</mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>

                <button mat-raised-button color="primary" type="submit" [disabled]="isUpdating || profileForm.invalid">
                  <mat-spinner *ngIf="isUpdating" diameter="20"></mat-spinner>
                  <span *ngIf="!isUpdating">{{ 'profile.save_changes' | translate }}</span>
                </button>
              </form>
            </mat-card-content>
          </mat-card>
        </mat-tab>

        <!-- Change Password -->
        <mat-tab [label]="'profile.change_password' | translate">
          <mat-card>
            <mat-card-content>
              <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'profile.current_password' | translate }}</mat-label>
                  <input matInput type="password" formControlName="currentPassword">
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'profile.new_password' | translate }}</mat-label>
                  <input matInput type="password" formControlName="newPassword">
                  <mat-hint>{{ 'profile.password_hint' | translate }}</mat-hint>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>{{ 'profile.confirm_password' | translate }}</mat-label>
                  <input matInput type="password" formControlName="confirmPassword">
                </mat-form-field>

                <button mat-raised-button color="primary" type="submit" [disabled]="isChangingPassword || passwordForm.invalid">
                  <mat-spinner *ngIf="isChangingPassword" diameter="20"></mat-spinner>
                  <span *ngIf="!isChangingPassword">{{ 'profile.update_password' | translate }}</span>
                </button>
              </form>
            </mat-card-content>
          </mat-card>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .profile-container {
      max-width: 700px;
      margin: 0 auto;

      h1 { margin: 0 0 24px; color: #333; }
    }

    mat-card { margin-top: 16px; }

    .form-row {
      display: flex;
      gap: 16px;

      mat-form-field { flex: 1; }
    }

    .full-width { width: 100%; }

    button[type="submit"] {
      margin-top: 16px;

      mat-spinner { display: inline-block; margin-right: 8px; }
    }

    @media (max-width: 600px) {
      .form-row { flex-direction: column; }
    }
  `]
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  isUpdating = false;
  isChangingPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private translate: TranslateService,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [''],
      phone: [''],
      language: ['en'],
      timezone: ['UTC']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.profileForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          phone: user.phone,
          language: user.language,
          timezone: user.timezone
        });
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  updateProfile(): void {
    if (this.profileForm.invalid) return;

    this.isUpdating = true;
    const { email, ...updateData } = this.profileForm.value;

    this.authService.updateProfile(updateData).subscribe({
      next: () => {
        this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
        if (updateData.language !== this.translate.currentLang) {
          this.translate.use(updateData.language);
          localStorage.setItem('language', updateData.language);
        }
      },
      error: () => {
        this.snackBar.open('Failed to update profile', 'Close', { duration: 5000 });
      },
      complete: () => this.isUpdating = false
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    this.isChangingPassword = true;
    this.authService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.snackBar.open('Password changed successfully!', 'Close', { duration: 3000 });
        this.passwordForm.reset();
      },
      error: () => {
        this.snackBar.open('Failed to change password', 'Close', { duration: 5000 });
      },
      complete: () => this.isChangingPassword = false
    });
  }
}
