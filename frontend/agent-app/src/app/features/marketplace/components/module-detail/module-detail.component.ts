import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable, switchMap, tap } from 'rxjs';
import { MarketplaceService } from '../../services/marketplace.service';
import {
  MarketplaceModule,
  ModuleInstallation,
  PricingModel,
  CategoryLabels
} from '../../models/marketplace.models';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'app-module-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatDividerModule,
    TranslateModule,
    MarkdownModule
  ],
  template: `
    <div class="module-detail" *ngIf="module$ | async as module; else loading">
      <!-- Back button -->
      <button mat-button class="back-btn" routerLink="/marketplace">
        <mat-icon>arrow_back</mat-icon>
        {{ 'common.back' | translate }}
      </button>

      <div class="detail-content">
        <!-- Main content -->
        <div class="main-content">
          <!-- Header -->
          <div class="module-header">
            <div class="header-icon" *ngIf="module.icon">
              <img [src]="module.icon" [alt]="module.name">
            </div>
            <div class="header-icon placeholder" *ngIf="!module.icon">
              <mat-icon>extension</mat-icon>
            </div>
            <div class="header-info">
              <h1>
                {{ module.name }}
                <mat-icon *ngIf="module.verified" class="badge verified">verified</mat-icon>
                <mat-icon *ngIf="module.official" class="badge official">shield</mat-icon>
              </h1>
              <p class="author">
                {{ 'marketplace.by' | translate }} <a [href]="module.authorUrl" target="_blank">{{ module.author }}</a>
              </p>
              <div class="meta">
                <span class="rating">
                  <mat-icon>star</mat-icon>
                  {{ module.averageRating | number:'1.1-1' }}
                  ({{ module.reviewCount }} {{ 'marketplace.reviews' | translate }})
                </span>
                <span class="installs">
                  <mat-icon>download</mat-icon>
                  {{ module.installCount | number }} {{ 'marketplace.installs' | translate }}
                </span>
                <span class="version">
                  v{{ module.latestVersion }}
                </span>
              </div>
            </div>
          </div>

          <!-- Screenshots -->
          <div class="screenshots" *ngIf="module.screenshots?.length">
            <div class="screenshot" *ngFor="let screenshot of module.screenshots">
              <img [src]="screenshot" alt="Screenshot">
            </div>
          </div>

          <!-- Tabs -->
          <mat-tab-group>
            <mat-tab [label]="'marketplace.overview' | translate">
              <div class="tab-content">
                <div class="description" [innerHTML]="module.description"></div>
              </div>
            </mat-tab>

            <mat-tab [label]="'marketplace.reviews' | translate">
              <div class="tab-content">
                <p class="placeholder-text">{{ 'marketplace.reviewsComingSoon' | translate }}</p>
              </div>
            </mat-tab>

            <mat-tab [label]="'marketplace.changelog' | translate">
              <div class="tab-content">
                <p class="placeholder-text">{{ 'marketplace.changelogComingSoon' | translate }}</p>
              </div>
            </mat-tab>
          </mat-tab-group>
        </div>

        <!-- Sidebar -->
        <aside class="sidebar">
          <!-- Install card -->
          <mat-card class="install-card">
            <div class="price" [class.free]="module.pricingModel === 'FREE'">
              {{ getPriceLabel(module) }}
            </div>

            <ng-container *ngIf="!module.installed">
              <button mat-flat-button color="primary" class="install-btn"
                      (click)="installModule(module)"
                      [disabled]="installing">
                <mat-icon>download</mat-icon>
                {{ 'marketplace.install' | translate }}
              </button>

              <p class="trial-info" *ngIf="module.trialDays > 0">
                {{ module.trialDays }}-{{ 'marketplace.dayTrial' | translate }}
              </p>
            </ng-container>

            <ng-container *ngIf="module.installed">
              <div class="installed-info">
                <mat-icon class="check">check_circle</mat-icon>
                <span>{{ 'marketplace.installed' | translate }}</span>
              </div>

              <div class="installed-version">
                v{{ module.installedVersion }}
                <span *ngIf="module.updateAvailable" class="update-available">
                  → v{{ module.latestVersion }}
                </span>
              </div>

              <button mat-flat-button color="primary" class="install-btn"
                      *ngIf="module.updateAvailable"
                      (click)="updateModule(module)"
                      [disabled]="updating">
                <mat-icon>update</mat-icon>
                {{ 'marketplace.update' | translate }}
              </button>

              <button mat-stroked-button class="manage-btn"
                      [routerLink]="['/modules', module.moduleId, 'settings']">
                <mat-icon>settings</mat-icon>
                {{ 'marketplace.configure' | translate }}
              </button>

              <button mat-button color="warn" class="uninstall-btn"
                      (click)="uninstallModule(module)"
                      [disabled]="uninstalling">
                {{ 'marketplace.uninstall' | translate }}
              </button>
            </ng-container>
          </mat-card>

          <!-- Info card -->
          <mat-card class="info-card">
            <h3>{{ 'marketplace.information' | translate }}</h3>
            <dl>
              <dt>{{ 'marketplace.category' | translate }}</dt>
              <dd>{{ getCategoryLabel(module.category) }}</dd>

              <dt>{{ 'marketplace.version' | translate }}</dt>
              <dd>{{ module.latestVersion }}</dd>

              <dt>{{ 'marketplace.published' | translate }}</dt>
              <dd>{{ module.publishedAt | date:'mediumDate' }}</dd>

              <dt>{{ 'marketplace.updated' | translate }}</dt>
              <dd>{{ module.updatedAt | date:'mediumDate' }}</dd>

              <dt>{{ 'marketplace.platformVersion' | translate }}</dt>
              <dd>{{ module.minimumPlatformVersion || 'Any' }}</dd>
            </dl>

            <mat-divider></mat-divider>

            <div class="links">
              <a mat-button *ngIf="module.documentationUrl" [href]="module.documentationUrl" target="_blank">
                <mat-icon>description</mat-icon>
                {{ 'marketplace.documentation' | translate }}
              </a>
              <a mat-button *ngIf="module.supportUrl" [href]="module.supportUrl" target="_blank">
                <mat-icon>help</mat-icon>
                {{ 'marketplace.support' | translate }}
              </a>
              <a mat-button *ngIf="module.repositoryUrl" [href]="module.repositoryUrl" target="_blank">
                <mat-icon>code</mat-icon>
                {{ 'marketplace.sourceCode' | translate }}
              </a>
            </div>
          </mat-card>

          <!-- Tags -->
          <mat-card class="tags-card" *ngIf="module.tags?.length">
            <h3>{{ 'marketplace.tags' | translate }}</h3>
            <mat-chip-set>
              <mat-chip *ngFor="let tag of module.tags">{{ tag }}</mat-chip>
            </mat-chip-set>
          </mat-card>

          <!-- Dependencies -->
          <mat-card class="deps-card" *ngIf="module.dependencies?.length">
            <h3>{{ 'marketplace.dependencies' | translate }}</h3>
            <ul>
              <li *ngFor="let dep of module.dependencies">
                <a [routerLink]="['/marketplace', dep]">{{ dep }}</a>
              </li>
            </ul>
          </mat-card>
        </aside>
      </div>
    </div>

    <ng-template #loading>
      <div class="loading">
        <mat-spinner diameter="48"></mat-spinner>
      </div>
    </ng-template>
  `,
  styles: [`
    .module-detail {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }

    .back-btn {
      margin-bottom: 16px;
    }

    .detail-content {
      display: grid;
      grid-template-columns: 1fr 300px;
      gap: 24px;

      @media (max-width: 900px) {
        grid-template-columns: 1fr;
      }
    }

    .main-content {
      min-width: 0;
    }

    .module-header {
      display: flex;
      gap: 20px;
      margin-bottom: 24px;

      .header-icon {
        width: 80px;
        height: 80px;
        border-radius: 16px;
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
            font-size: 40px;
            width: 40px;
            height: 40px;
          }
        }
      }

      .header-info {
        h1 {
          margin: 0 0 8px;
          font-size: 28px;
          font-weight: 500;
          display: flex;
          align-items: center;
          gap: 8px;

          .badge {
            font-size: 20px;
            width: 20px;
            height: 20px;

            &.verified { color: #2196f3; }
            &.official { color: #4caf50; }
          }
        }

        .author {
          margin: 0 0 12px;
          color: rgba(0, 0, 0, 0.6);

          a {
            color: #3f51b5;
            text-decoration: none;

            &:hover {
              text-decoration: underline;
            }
          }
        }

        .meta {
          display: flex;
          gap: 16px;
          font-size: 14px;
          color: rgba(0, 0, 0, 0.6);

          span {
            display: flex;
            align-items: center;
            gap: 4px;

            mat-icon {
              font-size: 18px;
              width: 18px;
              height: 18px;
            }
          }
        }
      }
    }

    .screenshots {
      display: flex;
      gap: 12px;
      overflow-x: auto;
      margin-bottom: 24px;
      padding-bottom: 8px;

      .screenshot {
        flex-shrink: 0;
        width: 280px;
        height: 180px;
        border-radius: 8px;
        overflow: hidden;
        border: 1px solid rgba(0, 0, 0, 0.12);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }

    .tab-content {
      padding: 24px 0;

      .description {
        line-height: 1.7;
      }

      .placeholder-text {
        color: rgba(0, 0, 0, 0.5);
        text-align: center;
        padding: 40px;
      }
    }

    .sidebar {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .install-card {
      padding: 20px;

      .price {
        font-size: 24px;
        font-weight: 500;
        margin-bottom: 16px;

        &.free {
          color: #4caf50;
        }
      }

      .install-btn, .manage-btn {
        width: 100%;
        margin-bottom: 8px;
      }

      .trial-info {
        text-align: center;
        font-size: 13px;
        color: rgba(0, 0, 0, 0.6);
        margin: 8px 0 0;
      }

      .installed-info {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #4caf50;
        margin-bottom: 8px;

        .check {
          font-size: 20px;
        }
      }

      .installed-version {
        font-size: 13px;
        color: rgba(0, 0, 0, 0.6);
        margin-bottom: 12px;

        .update-available {
          color: #ff9800;
          font-weight: 500;
        }
      }

      .uninstall-btn {
        width: 100%;
        margin-top: 8px;
      }
    }

    .info-card {
      padding: 20px;

      h3 {
        margin: 0 0 16px;
        font-size: 16px;
        font-weight: 500;
      }

      dl {
        margin: 0;

        dt {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.5);
          margin-bottom: 2px;
        }

        dd {
          margin: 0 0 12px;
          font-size: 14px;
        }
      }

      mat-divider {
        margin: 16px 0;
      }

      .links {
        display: flex;
        flex-direction: column;

        a {
          justify-content: flex-start;
        }
      }
    }

    .tags-card, .deps-card {
      padding: 20px;

      h3 {
        margin: 0 0 12px;
        font-size: 16px;
        font-weight: 500;
      }

      mat-chip {
        font-size: 12px;
      }

      ul {
        margin: 0;
        padding-left: 20px;

        a {
          color: #3f51b5;
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }
      }
    }

    .loading {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 400px;
    }
  `]
})
export class ModuleDetailComponent implements OnInit {
  module$!: Observable<MarketplaceModule>;
  installing = false;
  updating = false;
  uninstalling = false;

  CategoryLabels = CategoryLabels;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private marketplaceService: MarketplaceService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.module$ = this.route.paramMap.pipe(
      switchMap(params => {
        const moduleId = params.get('moduleId')!;
        return this.marketplaceService.getModuleDetails(moduleId);
      })
    );
  }

  getPriceLabel(module: MarketplaceModule): string {
    switch (module.pricingModel) {
      case PricingModel.FREE:
        return 'Free';
      case PricingModel.ONE_TIME:
        return `${module.priceCurrency} ${module.price}`;
      case PricingModel.SUBSCRIPTION_MONTHLY:
        return `${module.priceCurrency} ${module.price}/mo`;
      case PricingModel.SUBSCRIPTION_YEARLY:
        return `${module.priceCurrency} ${module.price}/yr`;
      case PricingModel.CONTACT_US:
        return 'Contact Us';
      default:
        return '';
    }
  }

  getCategoryLabel(category: string): string {
    return CategoryLabels[category as keyof typeof CategoryLabels] || category;
  }

  installModule(module: MarketplaceModule): void {
    this.installing = true;
    this.marketplaceService.installModule({
      moduleId: module.moduleId,
      enableTrial: module.pricingModel !== PricingModel.FREE && module.trialDays > 0
    }).subscribe({
      next: () => {
        this.installing = false;
        this.snackBar.open(
          this.translate.instant('marketplace.installSuccess'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
        // Refresh module details
        this.ngOnInit();
      },
      error: (err) => {
        this.installing = false;
        this.snackBar.open(
          this.translate.instant('marketplace.installError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }

  updateModule(module: MarketplaceModule): void {
    this.updating = true;
    this.marketplaceService.updateModule(module.moduleId).subscribe({
      next: () => {
        this.updating = false;
        this.snackBar.open(
          this.translate.instant('marketplace.updateSuccess'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
        this.ngOnInit();
      },
      error: (err) => {
        this.updating = false;
        this.snackBar.open(
          this.translate.instant('marketplace.updateError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }

  uninstallModule(module: MarketplaceModule): void {
    if (!confirm(this.translate.instant('marketplace.confirmUninstall'))) {
      return;
    }

    this.uninstalling = true;
    this.marketplaceService.uninstallModule(module.moduleId).subscribe({
      next: () => {
        this.uninstalling = false;
        this.snackBar.open(
          this.translate.instant('marketplace.uninstallSuccess'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
        this.router.navigate(['/marketplace']);
      },
      error: (err) => {
        this.uninstalling = false;
        this.snackBar.open(
          this.translate.instant('marketplace.uninstallError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }
}
