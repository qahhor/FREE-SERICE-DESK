import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatTabsModule, LayoutComponent],
  template: `
    <app-layout>
      <div class="analytics-container">
        <div class="header">
          <h1>Analytics & Reports</h1>
          <button mat-raised-button color="primary">
            <mat-icon>download</mat-icon>
            Export Report
          </button>
        </div>

        <mat-tab-group>
          <mat-tab label="Overview">
            <div class="tab-content">
              <div class="stats-grid">
                <mat-card class="stat-card">
                  <div class="stat-value">1,234</div>
                  <div class="stat-label">Total Tickets</div>
                  <div class="stat-change positive">+12% from last month</div>
                </mat-card>

                <mat-card class="stat-card">
                  <div class="stat-value">2.5h</div>
                  <div class="stat-label">Avg Response Time</div>
                  <div class="stat-change negative">-15% from last month</div>
                </mat-card>

                <mat-card class="stat-card">
                  <div class="stat-value">87%</div>
                  <div class="stat-label">Resolution Rate</div>
                  <div class="stat-change positive">+5% from last month</div>
                </mat-card>

                <mat-card class="stat-card">
                  <div class="stat-value">4.8</div>
                  <div class="stat-label">Customer Satisfaction</div>
                  <div class="stat-change positive">+0.3 from last month</div>
                </mat-card>
              </div>

              <mat-card class="chart-card">
                <mat-card-header>
                  <mat-card-title>Tickets Over Time</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <div class="chart-placeholder">
                    <mat-icon>bar_chart</mat-icon>
                    <p>Chart visualization would appear here</p>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>

          <mat-tab label="Performance">
            <div class="tab-content">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>Agent Performance</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <div class="chart-placeholder">
                    <mat-icon>leaderboard</mat-icon>
                    <p>Agent performance metrics would appear here</p>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>

          <mat-tab label="Channels">
            <div class="tab-content">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>Channel Distribution</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <div class="chart-placeholder">
                    <mat-icon>pie_chart</mat-icon>
                    <p>Channel distribution chart would appear here</p>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>
        </mat-tab-group>
      </div>
    </app-layout>
  `,
  styles: [`
    .analytics-container {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;

        h1 { margin: 0; }
      }

      .tab-content {
        padding: 24px 0;
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 16px;
        margin-bottom: 24px;

        .stat-card {
          padding: 24px;
          text-align: center;

          .stat-value {
            font-size: 36px;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
          }

          .stat-label {
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
          }

          .stat-change {
            font-size: 12px;

            &.positive { color: #4caf50; }
            &.negative { color: #f44336; }
          }
        }
      }

      .chart-card {
        margin-top: 24px;

        .chart-placeholder {
          min-height: 300px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          background: #f5f5f5;
          border-radius: 8px;
          color: #999;

          mat-icon {
            font-size: 64px;
            width: 64px;
            height: 64px;
            margin-bottom: 16px;
          }

          p {
            margin: 0;
            font-size: 14px;
          }
        }
      }
    }
  `]
})
export class AnalyticsComponent {}
