import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, LayoutComponent],
  template: `
    <app-layout>
      <div class="marketplace-container">
        <h1>Marketplace</h1>
        <p>Extend ServiceDesk with integrations and plugins</p>

        <div class="modules-grid">
          <mat-card *ngFor="let module of modules" class="module-card">
            <mat-card-header>
              <div class="module-icon">
                <mat-icon>{{ module.icon }}</mat-icon>
              </div>
              <div>
                <mat-card-title>{{ module.name }}</mat-card-title>
                <mat-card-subtitle>{{ module.author }}</mat-card-subtitle>
              </div>
            </mat-card-header>
            <mat-card-content>
              <p>{{ module.description }}</p>
              <div class="module-meta">
                <mat-chip-set>
                  <mat-chip *ngFor="let tag of module.tags">{{ tag }}</mat-chip>
                </mat-chip-set>
                <div class="module-stats">
                  <span><mat-icon>download</mat-icon> {{ module.downloads }}</span>
                  <span><mat-icon>star</mat-icon> {{ module.rating }}</span>
                </div>
              </div>
            </mat-card-content>
            <mat-card-actions>
              <button mat-raised-button [color]="module.installed ? 'accent' : 'primary'">
                {{ module.installed ? 'Installed' : 'Install' }}
              </button>
              <button mat-button>View Details</button>
            </mat-card-actions>
          </mat-card>
        </div>
      </div>
    </app-layout>
  `,
  styles: [`
    .marketplace-container {
      h1 { margin-bottom: 8px; }
      p { color: #666; margin-bottom: 32px; }

      .modules-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
        gap: 24px;

        .module-card {
          mat-card-header {
            margin-bottom: 16px;

            .module-icon {
              width: 48px;
              height: 48px;
              background: #3f51b5;
              border-radius: 8px;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-right: 16px;

              mat-icon {
                color: white;
                font-size: 32px;
                width: 32px;
                height: 32px;
              }
            }
          }

          mat-card-content {
            p {
              min-height: 60px;
              color: #666;
              margin-bottom: 16px;
            }

            .module-meta {
              .module-stats {
                display: flex;
                gap: 16px;
                margin-top: 12px;
                font-size: 12px;
                color: #666;

                span {
                  display: flex;
                  align-items: center;
                  gap: 4px;

                  mat-icon {
                    font-size: 16px;
                    width: 16px;
                    height: 16px;
                  }
                }
              }
            }
          }
        }
      }
    }
  `]
})
export class MarketplaceComponent {
  modules = [
    {
      name: 'Slack Integration',
      author: 'ServiceDesk Team',
      description: 'Receive ticket notifications in Slack channels',
      tags: ['Communication', 'Notifications'],
      downloads: '5.2k',
      rating: '4.8',
      icon: 'chat',
      installed: true
    },
    {
      name: 'Jira Sync',
      author: 'Atlassian',
      description: 'Synchronize tickets with Jira issues',
      tags: ['Project Management', 'Integration'],
      downloads: '3.1k',
      rating: '4.5',
      icon: 'sync',
      installed: false
    },
    {
      name: 'Advanced Analytics',
      author: 'Analytics Pro',
      description: 'Advanced reporting and custom dashboards',
      tags: ['Analytics', 'Reports'],
      downloads: '2.8k',
      rating: '4.9',
      icon: 'insights',
      installed: false
    }
  ];
}
