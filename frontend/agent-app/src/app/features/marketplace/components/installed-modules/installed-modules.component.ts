import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { MarketplaceService } from '../../services/marketplace.service';
import {
  ModuleInstallation,
  InstallationStatus,
  HealthStatus
} from '../../models/marketplace.models';

@Component({
  selector: 'app-installed-modules',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatMenuModule,
    TranslateModule
  ],
  template: `
    <div class="installed-modules">
      <div class="page-header">
        <h1>{{ 'modules.installed' | translate }}</h1>
        <button mat-flat-button color="primary" routerLink="/marketplace">
          <mat-icon>add</mat-icon>
          {{ 'modules.browseMarketplace' | translate }}
        </button>
      </div>

      <div class="modules-list" *ngIf="modules$ | async as modules; else loading">
        <ng-container *ngIf="modules.length > 0; else empty">
          <mat-card *ngFor="let module of modules" class="module-item">
            <div class="module-icon" *ngIf="module.moduleIcon">
              <img [src]="module.moduleIcon" [alt]="module.moduleName">
            </div>
            <div class="module-icon placeholder" *ngIf="!module.moduleIcon">
              <mat-icon>extension</mat-icon>
            </div>

            <div class="module-info">
              <h3 [routerLink]="['/marketplace', module.moduleId]">
                {{ module.moduleName || module.moduleId }}
              </h3>
              <div class="meta">
                <span class="version">v{{ module.installedVersion }}</span>
                <mat-chip *ngIf="module.updateAvailable" class="update-chip">
                  {{ 'modules.updateAvailable' | translate }}: v{{ module.latestVersion }}
                </mat-chip>
                <mat-chip *ngIf="module.inTrial" class="trial-chip">
                  {{ 'modules.trial' | translate }}
                </mat-chip>
              </div>

              <div class="status-row">
                <span class="health" [class]="module.healthStatus?.toLowerCase()">
                  <mat-icon>{{ getHealthIcon(module.healthStatus) }}</mat-icon>
                  {{ module.healthStatus || 'UNKNOWN' }}
                </span>
                <span class="status" [class]="module.status.toLowerCase()">
                  {{ module.status }}
                </span>
              </div>

              <p class="error" *ngIf="module.errorMessage">
                <mat-icon>error</mat-icon>
                {{ module.errorMessage }}
              </p>
            </div>

            <div class="module-actions">
              <mat-slide-toggle
                [checked]="module.enabled"
                [disabled]="isLoading(module.moduleId)"
                (change)="toggleModule(module, $event.checked)"
                [matTooltip]="module.enabled ? ('modules.disable' | translate) : ('modules.enable' | translate)">
              </mat-slide-toggle>

              <button mat-icon-button [matMenuTriggerFor]="menu">
                <mat-icon>more_vert</mat-icon>
              </button>

              <mat-menu #menu="matMenu">
                <button mat-menu-item [routerLink]="['/modules', module.moduleId, 'settings']">
                  <mat-icon>settings</mat-icon>
                  <span>{{ 'modules.configure' | translate }}</span>
                </button>

                <button mat-menu-item
                        *ngIf="module.updateAvailable"
                        (click)="updateModule(module)">
                  <mat-icon>update</mat-icon>
                  <span>{{ 'modules.update' | translate }}</span>
                </button>

                <button mat-menu-item [routerLink]="['/marketplace', module.moduleId]">
                  <mat-icon>info</mat-icon>
                  <span>{{ 'modules.viewDetails' | translate }}</span>
                </button>

                <mat-divider></mat-divider>

                <button mat-menu-item class="danger" (click)="uninstallModule(module)">
                  <mat-icon>delete</mat-icon>
                  <span>{{ 'modules.uninstall' | translate }}</span>
                </button>
              </mat-menu>
            </div>
          </mat-card>
        </ng-container>

        <ng-template #empty>
          <div class="empty-state">
            <mat-icon>extension_off</mat-icon>
            <h2>{{ 'modules.noModulesInstalled' | translate }}</h2>
            <p>{{ 'modules.browseMarketplaceMessage' | translate }}</p>
            <button mat-flat-button color="primary" routerLink="/marketplace">
              {{ 'modules.browseMarketplace' | translate }}
            </button>
          </div>
        </ng-template>
      </div>

      <ng-template #loading>
        <div class="loading">
          <mat-spinner diameter="48"></mat-spinner>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .installed-modules {
      max-width: 900px;
      margin: 0 auto;
      padding: 24px;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
      }
    }

    .modules-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .module-item {
      display: flex;
      align-items: flex-start;
      padding: 20px;
      gap: 16px;
    }

    .module-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
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
          font-size: 28px;
          width: 28px;
          height: 28px;
        }
      }
    }

    .module-info {
      flex: 1;
      min-width: 0;

      h3 {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 500;
        cursor: pointer;

        &:hover {
          color: #3f51b5;
        }
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .version {
          font-size: 13px;
          color: rgba(0, 0, 0, 0.6);
        }

        .update-chip {
          background: #ff9800;
          color: white;
          font-size: 11px;
        }

        .trial-chip {
          background: #2196f3;
          color: white;
          font-size: 11px;
        }
      }

      .status-row {
        display: flex;
        gap: 16px;
        font-size: 13px;

        .health, .status {
          display: flex;
          align-items: center;
          gap: 4px;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
          }
        }

        .health {
          &.healthy {
            color: #4caf50;
          }
          &.degraded {
            color: #ff9800;
          }
          &.unhealthy {
            color: #f44336;
          }
          &.unknown {
            color: rgba(0, 0, 0, 0.5);
          }
        }

        .status {
          &.active {
            color: #4caf50;
          }
          &.disabled {
            color: rgba(0, 0, 0, 0.5);
          }
          &.updating, &.installing {
            color: #2196f3;
          }
          &.failed {
            color: #f44336;
          }
        }
      }

      .error {
        margin: 8px 0 0;
        padding: 8px 12px;
        background: #ffebee;
        color: #c62828;
        border-radius: 4px;
        font-size: 13px;
        display: flex;
        align-items: center;
        gap: 8px;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
        }
      }
    }

    .module-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .danger {
      color: #f44336;
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: rgba(0, 0, 0, 0.3);
        margin-bottom: 16px;
      }

      h2 {
        margin: 0 0 8px;
        font-size: 20px;
        font-weight: 500;
      }

      p {
        margin: 0 0 24px;
        color: rgba(0, 0, 0, 0.6);
      }
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 60px;
    }
  `]
})
export class InstalledModulesComponent implements OnInit {
  modules$!: Observable<ModuleInstallation[]>;
  loadingModules = new Set<string>();

  constructor(
    private marketplaceService: MarketplaceService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.modules$ = this.marketplaceService.installedModules$;
    this.marketplaceService.refreshInstalledModules();
  }

  isLoading(moduleId: string): boolean {
    return this.loadingModules.has(moduleId);
  }

  getHealthIcon(health: HealthStatus | undefined): string {
    switch (health) {
      case HealthStatus.HEALTHY:
        return 'check_circle';
      case HealthStatus.DEGRADED:
        return 'warning';
      case HealthStatus.UNHEALTHY:
        return 'error';
      default:
        return 'help';
    }
  }

  toggleModule(module: ModuleInstallation, enabled: boolean): void {
    this.loadingModules.add(module.moduleId);

    const action = enabled
      ? this.marketplaceService.enableModule(module.moduleId)
      : this.marketplaceService.disableModule(module.moduleId);

    action.subscribe({
      next: () => {
        this.loadingModules.delete(module.moduleId);
        const messageKey = enabled ? 'modules.enableSuccess' : 'modules.disableSuccess';
        this.snackBar.open(
          this.translate.instant(messageKey),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
      error: () => {
        this.loadingModules.delete(module.moduleId);
        this.snackBar.open(
          this.translate.instant('modules.toggleError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }

  updateModule(module: ModuleInstallation): void {
    this.loadingModules.add(module.moduleId);

    this.marketplaceService.updateModule(module.moduleId).subscribe({
      next: () => {
        this.loadingModules.delete(module.moduleId);
        this.snackBar.open(
          this.translate.instant('modules.updateSuccess'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
      error: () => {
        this.loadingModules.delete(module.moduleId);
        this.snackBar.open(
          this.translate.instant('modules.updateError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }

  uninstallModule(module: ModuleInstallation): void {
    if (!confirm(this.translate.instant('modules.confirmUninstall'))) {
      return;
    }

    this.loadingModules.add(module.moduleId);

    this.marketplaceService.uninstallModule(module.moduleId).subscribe({
      next: () => {
        this.loadingModules.delete(module.moduleId);
        this.snackBar.open(
          this.translate.instant('modules.uninstallSuccess'),
          this.translate.instant('common.close'),
          { duration: 3000 }
        );
      },
      error: () => {
        this.loadingModules.delete(module.moduleId);
        this.snackBar.open(
          this.translate.instant('modules.uninstallError'),
          this.translate.instant('common.close'),
          { duration: 5000 }
        );
      }
    });
  }
}
