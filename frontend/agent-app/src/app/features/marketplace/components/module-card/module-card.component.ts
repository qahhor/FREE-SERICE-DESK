import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';
import {
  MarketplaceModule,
  PricingModel,
  CategoryLabels
} from '../../models/marketplace.models';

@Component({
  selector: 'app-module-card',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
    MatBadgeModule,
    TranslateModule
  ],
  template: `
    <mat-card class="module-card" [routerLink]="['/marketplace', module.moduleId]">
      <!-- Header -->
      <div class="card-header">
        <div class="module-icon" *ngIf="module.icon">
          <img [src]="module.icon" [alt]="module.name">
        </div>
        <div class="module-icon placeholder" *ngIf="!module.icon">
          <mat-icon>extension</mat-icon>
        </div>
        <div class="module-info">
          <h3 class="module-name">
            {{ module.name }}
            <mat-icon *ngIf="module.verified" class="verified-badge" matTooltip="Verified">verified</mat-icon>
            <mat-icon *ngIf="module.official" class="official-badge" matTooltip="Official">shield</mat-icon>
          </h3>
          <span class="module-author">{{ module.author }}</span>
        </div>
      </div>

      <!-- Description -->
      <p class="module-description">{{ module.shortDescription || module.description }}</p>

      <!-- Stats -->
      <div class="module-stats">
        <div class="stat" matTooltip="Rating">
          <mat-icon>star</mat-icon>
          <span>{{ module.averageRating | number:'1.1-1' }}</span>
          <span class="count">({{ module.reviewCount }})</span>
        </div>
        <div class="stat" matTooltip="Installs">
          <mat-icon>download</mat-icon>
          <span>{{ formatInstallCount(module.installCount) }}</span>
        </div>
        <div class="stat price" [class.free]="module.pricingModel === 'FREE'">
          {{ getPriceLabel() }}
        </div>
      </div>

      <!-- Tags -->
      <div class="module-tags" *ngIf="module.tags?.length">
        <mat-chip-set>
          <mat-chip *ngFor="let tag of module.tags.slice(0, 3)" [disableRipple]="true">
            {{ tag }}
          </mat-chip>
        </mat-chip-set>
      </div>

      <!-- Actions -->
      <div class="card-actions">
        <ng-container *ngIf="!module.installed">
          <button mat-flat-button color="primary"
                  (click)="onInstall($event)"
                  [disabled]="installing">
            <mat-icon>download</mat-icon>
            {{ 'marketplace.install' | translate }}
          </button>
        </ng-container>

        <ng-container *ngIf="module.installed">
          <button mat-stroked-button color="primary" [routerLink]="['/modules', module.moduleId]"
                  (click)="$event.stopPropagation()">
            <mat-icon>settings</mat-icon>
            {{ 'marketplace.manage' | translate }}
          </button>
          <mat-chip *ngIf="module.updateAvailable" class="update-badge">
            {{ 'marketplace.updateAvailable' | translate }}
          </mat-chip>
        </ng-container>
      </div>
    </mat-card>
  `,
  styles: [`
    .module-card {
      display: flex;
      flex-direction: column;
      padding: 16px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      height: 100%;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }

    .card-header {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;
    }

    .module-icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      overflow: hidden;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &.placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;

        mat-icon {
          font-size: 24px;
          width: 24px;
          height: 24px;
        }
      }
    }

    .module-info {
      flex: 1;
      min-width: 0;
    }

    .module-name {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 4px;

      .verified-badge {
        font-size: 16px;
        width: 16px;
        height: 16px;
        color: #2196f3;
      }

      .official-badge {
        font-size: 16px;
        width: 16px;
        height: 16px;
        color: #4caf50;
      }
    }

    .module-author {
      font-size: 13px;
      color: rgba(0, 0, 0, 0.6);
    }

    .module-description {
      flex: 1;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.7);
      line-height: 1.5;
      margin: 0 0 12px;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .module-stats {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;
      font-size: 13px;

      .stat {
        display: flex;
        align-items: center;
        gap: 4px;
        color: rgba(0, 0, 0, 0.6);

        mat-icon {
          font-size: 16px;
          width: 16px;
          height: 16px;
        }

        .count {
          opacity: 0.7;
        }

        &.price {
          margin-left: auto;
          font-weight: 500;
          color: rgba(0, 0, 0, 0.87);

          &.free {
            color: #4caf50;
          }
        }
      }
    }

    .module-tags {
      margin-bottom: 12px;

      mat-chip {
        font-size: 11px;
        min-height: 24px;
      }
    }

    .card-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: auto;

      button {
        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
          margin-right: 4px;
        }
      }

      .update-badge {
        background: #ff9800;
        color: white;
        font-size: 11px;
        min-height: 24px;
      }
    }
  `]
})
export class ModuleCardComponent {
  @Input() module!: MarketplaceModule;
  @Output() install = new EventEmitter<MarketplaceModule>();
  @Output() viewDetails = new EventEmitter<MarketplaceModule>();

  installing = false;

  onInstall(event: Event): void {
    event.stopPropagation();
    this.install.emit(this.module);
  }

  getPriceLabel(): string {
    switch (this.module.pricingModel) {
      case PricingModel.FREE:
        return 'Free';
      case PricingModel.ONE_TIME:
        return `${this.module.priceCurrency} ${this.module.price}`;
      case PricingModel.SUBSCRIPTION_MONTHLY:
        return `${this.module.priceCurrency} ${this.module.price}/mo`;
      case PricingModel.SUBSCRIPTION_YEARLY:
        return `${this.module.priceCurrency} ${this.module.price}/yr`;
      case PricingModel.CONTACT_US:
        return 'Contact Us';
      default:
        return '';
    }
  }

  formatInstallCount(count: number): string {
    if (count >= 1000000) {
      return `${(count / 1000000).toFixed(1)}M`;
    }
    if (count >= 1000) {
      return `${(count / 1000).toFixed(1)}K`;
    }
    return count.toString();
  }
}
