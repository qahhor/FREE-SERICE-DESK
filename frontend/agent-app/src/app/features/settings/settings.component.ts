import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Store } from '@ngrx/store';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="settings-container">
      <h1>{{ 'user.settings' | translate }}</h1>

      <mat-tab-group>
        <!-- Profile Settings -->
        <mat-tab [label]="'user.profile' | translate">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>{{ 'user.profile' | translate }}</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <form [formGroup]="profileForm" (ngSubmit)="saveProfile()">
                  <div class="avatar-section">
                    <div class="avatar-preview">
                      <span class="avatar-initials">{{ getInitials() }}</span>
                    </div>
                    <button mat-stroked-button type="button">
                      <mat-icon>upload</mat-icon>
                      Change Avatar
                    </button>
                  </div>

                  <div class="form-row two-columns">
                    <mat-form-field appearance="outline">
                      <mat-label>{{ 'user.first.name' | translate }}</mat-label>
                      <input matInput formControlName="firstName">
                    </mat-form-field>

                    <mat-form-field appearance="outline">
                      <mat-label>{{ 'user.last.name' | translate }}</mat-label>
                      <input matInput formControlName="lastName">
                    </mat-form-field>
                  </div>

                  <div class="form-row">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>{{ 'auth.email' | translate }}</mat-label>
                      <input matInput formControlName="email" type="email" readonly>
                    </mat-form-field>
                  </div>

                  <div class="form-row">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>{{ 'user.phone' | translate }}</mat-label>
                      <input matInput formControlName="phone">
                    </mat-form-field>
                  </div>

                  <div class="form-actions">
                    <button mat-raised-button color="primary" type="submit"
                            [disabled]="profileForm.invalid">
                      {{ 'common.save' | translate }}
                    </button>
                  </div>
                </form>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Security Settings -->
        <mat-tab label="Security">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Change Password</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
                  <div class="form-row">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>Current Password</mat-label>
                      <input matInput formControlName="currentPassword" type="password">
                    </mat-form-field>
                  </div>

                  <div class="form-row">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>{{ 'auth.password' | translate }}</mat-label>
                      <input matInput formControlName="newPassword" type="password">
                      <mat-error *ngIf="passwordForm.get('newPassword')?.hasError('minlength')">
                        {{ 'validation.password.min' | translate }}
                      </mat-error>
                    </mat-form-field>
                  </div>

                  <div class="form-row">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>{{ 'auth.confirm.password' | translate }}</mat-label>
                      <input matInput formControlName="confirmPassword" type="password">
                      <mat-error *ngIf="passwordForm.hasError('mismatch')">
                        {{ 'validation.password.mismatch' | translate }}
                      </mat-error>
                    </mat-form-field>
                  </div>

                  <div class="form-actions">
                    <button mat-raised-button color="primary" type="submit"
                            [disabled]="passwordForm.invalid">
                      Update Password
                    </button>
                  </div>
                </form>
              </mat-card-content>
            </mat-card>

            <mat-card>
              <mat-card-header>
                <mat-card-title>Two-Factor Authentication</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="security-option">
                  <div class="option-info">
                    <h4>Authenticator App</h4>
                    <p>Use an authenticator app to generate verification codes.</p>
                  </div>
                  <mat-slide-toggle [(ngModel)]="twoFactorEnabled">
                  </mat-slide-toggle>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Notification Settings -->
        <mat-tab label="Notifications">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Email Notifications</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <mat-list>
                  <mat-list-item>
                    <span matListItemTitle>New ticket assigned</span>
                    <mat-slide-toggle matListItemMeta [(ngModel)]="notifications.newTicket">
                    </mat-slide-toggle>
                  </mat-list-item>
                  <mat-divider></mat-divider>

                  <mat-list-item>
                    <span matListItemTitle>Ticket updates</span>
                    <mat-slide-toggle matListItemMeta [(ngModel)]="notifications.ticketUpdate">
                    </mat-slide-toggle>
                  </mat-list-item>
                  <mat-divider></mat-divider>

                  <mat-list-item>
                    <span matListItemTitle>Ticket comments</span>
                    <mat-slide-toggle matListItemMeta [(ngModel)]="notifications.ticketComment">
                    </mat-slide-toggle>
                  </mat-list-item>
                  <mat-divider></mat-divider>

                  <mat-list-item>
                    <span matListItemTitle>Mention notifications</span>
                    <mat-slide-toggle matListItemMeta [(ngModel)]="notifications.mentions">
                    </mat-slide-toggle>
                  </mat-list-item>
                  <mat-divider></mat-divider>

                  <mat-list-item>
                    <span matListItemTitle>Daily digest</span>
                    <mat-slide-toggle matListItemMeta [(ngModel)]="notifications.dailyDigest">
                    </mat-slide-toggle>
                  </mat-list-item>
                </mat-list>

                <div class="form-actions">
                  <button mat-raised-button color="primary" (click)="saveNotifications()">
                    {{ 'common.save' | translate }}
                  </button>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Appearance Settings -->
        <mat-tab label="Appearance">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Display Preferences</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Language</mat-label>
                    <mat-select [(ngModel)]="selectedLanguage" (selectionChange)="changeLanguage()">
                      <mat-option value="en">English</mat-option>
                      <mat-option value="ru">Русский</mat-option>
                      <mat-option value="uz">O'zbekcha</mat-option>
                      <mat-option value="kk">Қазақша</mat-option>
                      <mat-option value="ar">العربية</mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>

                <div class="form-row">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Theme</mat-label>
                    <mat-select [(ngModel)]="selectedTheme">
                      <mat-option value="light">Light</mat-option>
                      <mat-option value="dark">Dark</mat-option>
                      <mat-option value="system">System</mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>

                <div class="form-row">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Timezone</mat-label>
                    <mat-select [(ngModel)]="selectedTimezone">
                      <mat-option value="Asia/Tashkent">Tashkent (UTC+5)</mat-option>
                      <mat-option value="Asia/Almaty">Almaty (UTC+6)</mat-option>
                      <mat-option value="Europe/Moscow">Moscow (UTC+3)</mat-option>
                      <mat-option value="UTC">UTC</mat-option>
                    </mat-select>
                  </mat-form-field>
                </div>

                <div class="form-actions">
                  <button mat-raised-button color="primary" (click)="saveAppearance()">
                    {{ 'common.save' | translate }}
                  </button>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .settings-container {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }

    .settings-container h1 {
      margin-bottom: 24px;
    }

    .tab-content {
      padding: 24px 0;
    }

    .tab-content mat-card {
      margin-bottom: 24px;
    }

    .avatar-section {
      display: flex;
      align-items: center;
      gap: 24px;
      margin-bottom: 24px;
    }

    .avatar-preview {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background-color: #1976d2;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .avatar-initials {
      color: white;
      font-size: 28px;
      font-weight: 500;
    }

    .form-row {
      margin-bottom: 16px;
    }

    .form-row.two-columns {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    .full-width {
      width: 100%;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 24px;
    }

    .security-option {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
    }

    .option-info h4 {
      margin: 0 0 4px 0;
    }

    .option-info p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }

    mat-list-item {
      height: 64px !important;
    }
  `]
})
export class SettingsComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  twoFactorEnabled = false;
  notifications = {
    newTicket: true,
    ticketUpdate: true,
    ticketComment: true,
    mentions: true,
    dailyDigest: false
  };

  selectedLanguage = 'en';
  selectedTheme = 'light';
  selectedTimezone = 'Asia/Tashkent';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private translate: TranslateService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForms();
    this.loadCurrentUser();
    this.selectedLanguage = this.translate.currentLang || 'en';
  }

  private initForms(): void {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  private loadCurrentUser(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      this.profileForm.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone || ''
      });
    }
  }

  getInitials(): string {
    const firstName = this.profileForm.get('firstName')?.value || '';
    const lastName = this.profileForm.get('lastName')?.value || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;

    this.snackBar.open(this.translate.instant('common.success'), '', { duration: 3000 });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) return;

    this.snackBar.open('Password updated successfully', '', { duration: 3000 });
    this.passwordForm.reset();
  }

  saveNotifications(): void {
    this.snackBar.open(this.translate.instant('common.success'), '', { duration: 3000 });
  }

  changeLanguage(): void {
    this.translate.use(this.selectedLanguage);
    localStorage.setItem('language', this.selectedLanguage);
  }

  saveAppearance(): void {
    localStorage.setItem('theme', this.selectedTheme);
    localStorage.setItem('timezone', this.selectedTimezone);
    this.snackBar.open(this.translate.instant('common.success'), '', { duration: 3000 });
  }
}
