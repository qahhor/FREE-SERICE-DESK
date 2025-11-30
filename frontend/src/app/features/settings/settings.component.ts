import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTabsModule } from '@angular/material/tabs';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSlideToggleModule,
    MatTabsModule,
    LayoutComponent
  ],
  template: `
    <app-layout>
      <div class="settings-container">
        <h1>Settings</h1>

        <mat-tab-group>
          <mat-tab label="Profile">
            <div class="tab-content">
              <mat-card>
                <mat-card-content>
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>First Name</mat-label>
                    <input matInput value="John">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Last Name</mat-label>
                    <input matInput value="Doe">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Email</mat-label>
                    <input matInput type="email" value="john.doe@example.com">
                  </mat-form-field>

                  <button mat-raised-button color="primary">Save Changes</button>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>

          <mat-tab label="Notifications">
            <div class="tab-content">
              <mat-card>
                <mat-card-content>
                  <div class="setting-item">
                    <div class="setting-info">
                      <h3>Email Notifications</h3>
                      <p>Receive email notifications for ticket updates</p>
                    </div>
                    <mat-slide-toggle checked></mat-slide-toggle>
                  </div>

                  <div class="setting-item">
                    <div class="setting-info">
                      <h3>Push Notifications</h3>
                      <p>Receive push notifications in browser</p>
                    </div>
                    <mat-slide-toggle checked></mat-slide-toggle>
                  </div>

                  <div class="setting-item">
                    <div class="setting-info">
                      <h3>Slack Notifications</h3>
                      <p>Send notifications to Slack channel</p>
                    </div>
                    <mat-slide-toggle></mat-slide-toggle>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>

          <mat-tab label="Security">
            <div class="tab-content">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>Change Password</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Current Password</mat-label>
                    <input matInput type="password">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>New Password</mat-label>
                    <input matInput type="password">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Confirm New Password</mat-label>
                    <input matInput type="password">
                  </mat-form-field>

                  <button mat-raised-button color="primary">Update Password</button>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>
        </mat-tab-group>
      </div>
    </app-layout>
  `,
  styles: [`
    .settings-container {
      h1 { margin-bottom: 24px; }

      .tab-content {
        padding: 24px 0;
        max-width: 600px;

        mat-card-content {
          display: flex;
          flex-direction: column;
          gap: 16px;

          .setting-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid #e0e0e0;

            &:last-child {
              border-bottom: none;
            }

            .setting-info {
              flex: 1;

              h3 {
                margin: 0 0 4px 0;
                font-size: 16px;
                font-weight: 500;
              }

              p {
                margin: 0;
                font-size: 14px;
                color: #666;
              }
            }
          }
        }
      }
    }
  `]
})
export class SettingsComponent {}
