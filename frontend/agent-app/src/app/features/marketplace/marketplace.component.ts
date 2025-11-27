import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, BehaviorSubject, combineLatest } from 'rxjs';
import { map, debounceTime, distinctUntilChanged, switchMap, startWith } from 'rxjs/operators';
import { MarketplaceService } from './services/marketplace.service';
import {
  MarketplaceModule,
  CategoryInfo,
  ModuleCategory,
  CategoryLabels
} from './models/marketplace.models';
import { ModuleCardComponent } from './components/module-card/module-card.component';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatChipsModule,
    MatTabsModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MatTooltipModule,
    TranslateModule,
    ModuleCardComponent
  ],
  template: `
    <div class="marketplace-container">
      <!-- Header -->
      <div class="marketplace-header">
        <div class="header-content">
          <h1>{{ 'marketplace.title' | translate }}</h1>
          <p class="subtitle">{{ 'marketplace.subtitle' | translate }}</p>
        </div>

        <!-- Search -->
        <div class="search-container">
          <mat-form-field appearance="outline" class="search-field">
            <mat-icon matPrefix>search</mat-icon>
            <input matInput
                   [(ngModel)]="searchQuery"
                   (ngModelChange)="onSearchChange($event)"
                   [placeholder]="'marketplace.searchPlaceholder' | translate">
            <button mat-icon-button matSuffix *ngIf="searchQuery" (click)="clearSearch()">
              <mat-icon>close</mat-icon>
            </button>
          </mat-form-field>
        </div>
      </div>

      <!-- Categories bar -->
      <div class="categories-bar">
        <button mat-button
                [class.active]="!selectedCategory"
                (click)="selectCategory(null)">
          {{ 'marketplace.allModules' | translate }}
        </button>
        <button mat-button
                *ngFor="let cat of categories$ | async"
                [class.active]="selectedCategory === cat.category"
                (click)="selectCategory(cat.category)">
          {{ getCategoryLabel(cat.category) }}
          <span class="count">{{ cat.moduleCount }}</span>
        </button>
      </div>

      <!-- Content -->
      <div class="marketplace-content" *ngIf="!loading; else loadingTpl">
        <!-- No category selected: show sections -->
        <ng-container *ngIf="!selectedCategory && !searchQuery">
          <!-- Featured modules -->
          <section class="module-section" *ngIf="featuredModules$ | async as featured">
            <div class="section-header">
              <h2>
                <mat-icon>star</mat-icon>
                {{ 'marketplace.featured' | translate }}
              </h2>
              <button mat-button color="primary" routerLink="featured">
                {{ 'common.viewAll' | translate }}
              </button>
            </div>
            <div class="modules-grid">
              <app-module-card *ngFor="let module of featured"
                               [module]="module"
                               (install)="onInstall($event)"
                               (viewDetails)="onViewDetails($event)">
              </app-module-card>
            </div>
          </section>

          <!-- Popular modules -->
          <section class="module-section" *ngIf="popularModules$ | async as popular">
            <div class="section-header">
              <h2>
                <mat-icon>trending_up</mat-icon>
                {{ 'marketplace.popular' | translate }}
              </h2>
              <button mat-button color="primary" routerLink="popular">
                {{ 'common.viewAll' | translate }}
              </button>
            </div>
            <div class="modules-grid">
              <app-module-card *ngFor="let module of popular"
                               [module]="module"
                               (install)="onInstall($event)"
                               (viewDetails)="onViewDetails($event)">
              </app-module-card>
            </div>
          </section>

          <!-- Newest modules -->
          <section class="module-section" *ngIf="newestModules$ | async as newest">
            <div class="section-header">
              <h2>
                <mat-icon>new_releases</mat-icon>
                {{ 'marketplace.newest' | translate }}
              </h2>
              <button mat-button color="primary" routerLink="newest">
                {{ 'common.viewAll' | translate }}
              </button>
            </div>
            <div class="modules-grid">
              <app-module-card *ngFor="let module of newest"
                               [module]="module"
                               (install)="onInstall($event)"
                               (viewDetails)="onViewDetails($event)">
              </app-module-card>
            </div>
          </section>

          <!-- Categories overview -->
          <section class="categories-section">
            <h2>{{ 'marketplace.browseByCategory' | translate }}</h2>
            <div class="categories-grid">
              <mat-card *ngFor="let cat of categories$ | async"
                        class="category-card"
                        (click)="selectCategory(cat.category)">
                <mat-icon>{{ getCategoryIcon(cat.category) }}</mat-icon>
                <h3>{{ getCategoryLabel(cat.category) }}</h3>
                <p>{{ cat.description }}</p>
                <span class="module-count">{{ cat.moduleCount }} {{ 'marketplace.modules' | translate }}</span>
              </mat-card>
            </div>
          </section>
        </ng-container>

        <!-- Search results or category listing -->
        <ng-container *ngIf="selectedCategory || searchQuery">
          <div class="results-header">
            <h2 *ngIf="selectedCategory">{{ getCategoryLabel(selectedCategory) }}</h2>
            <h2 *ngIf="searchQuery && !selectedCategory">
              {{ 'marketplace.searchResults' | translate }}: "{{ searchQuery }}"
            </h2>
          </div>

          <div class="modules-grid" *ngIf="searchResults$ | async as results">
            <app-module-card *ngFor="let module of results"
                             [module]="module"
                             (install)="onInstall($event)"
                             (viewDetails)="onViewDetails($event)">
            </app-module-card>

            <div class="no-results" *ngIf="results.length === 0">
              <mat-icon>search_off</mat-icon>
              <p>{{ 'marketplace.noResults' | translate }}</p>
            </div>
          </div>
        </ng-container>
      </div>

      <ng-template #loadingTpl>
        <div class="loading-container">
          <mat-spinner diameter="48"></mat-spinner>
          <p>{{ 'common.loading' | translate }}</p>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .marketplace-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .marketplace-header {
      margin-bottom: 24px;
    }

    .header-content {
      margin-bottom: 16px;

      h1 {
        margin: 0 0 8px;
        font-size: 28px;
        font-weight: 500;
      }

      .subtitle {
        margin: 0;
        color: rgba(0, 0, 0, 0.6);
      }
    }

    .search-container {
      max-width: 600px;
    }

    .search-field {
      width: 100%;
    }

    .categories-bar {
      display: flex;
      gap: 8px;
      overflow-x: auto;
      padding-bottom: 16px;
      margin-bottom: 24px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);

      button {
        white-space: nowrap;
        border-radius: 20px;

        &.active {
          background: rgba(63, 81, 181, 0.1);
          color: #3f51b5;
        }

        .count {
          margin-left: 4px;
          font-size: 12px;
          opacity: 0.7;
        }
      }
    }

    .module-section {
      margin-bottom: 40px;

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h2 {
          display: flex;
          align-items: center;
          gap: 8px;
          margin: 0;
          font-size: 20px;
          font-weight: 500;
        }
      }
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
    }

    .categories-section {
      margin-top: 40px;

      h2 {
        margin-bottom: 20px;
        font-size: 20px;
        font-weight: 500;
      }
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;
    }

    .category-card {
      padding: 20px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
        color: #3f51b5;
        margin-bottom: 12px;
      }

      h3 {
        margin: 0 0 8px;
        font-size: 16px;
        font-weight: 500;
      }

      p {
        margin: 0 0 12px;
        font-size: 13px;
        color: rgba(0, 0, 0, 0.6);
      }

      .module-count {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.5);
      }
    }

    .results-header {
      margin-bottom: 20px;

      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
      }
    }

    .no-results {
      grid-column: 1 / -1;
      text-align: center;
      padding: 60px 20px;
      color: rgba(0, 0, 0, 0.5);

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        margin-bottom: 16px;
      }

      p {
        font-size: 16px;
      }
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      gap: 16px;
      color: rgba(0, 0, 0, 0.5);
    }
  `]
})
export class MarketplaceComponent implements OnInit {
  searchQuery = '';
  selectedCategory: ModuleCategory | null = null;
  loading = false;

  private searchSubject = new BehaviorSubject<string>('');

  categories$!: Observable<CategoryInfo[]>;
  featuredModules$!: Observable<MarketplaceModule[]>;
  popularModules$!: Observable<MarketplaceModule[]>;
  newestModules$!: Observable<MarketplaceModule[]>;
  searchResults$!: Observable<MarketplaceModule[]>;

  CategoryLabels = CategoryLabels;

  private categoryIcons: Record<ModuleCategory, string> = {
    [ModuleCategory.INTEGRATION]: 'hub',
    [ModuleCategory.ANALYTICS]: 'analytics',
    [ModuleCategory.AUTOMATION]: 'auto_fix_high',
    [ModuleCategory.COMMUNICATION]: 'forum',
    [ModuleCategory.PRODUCTIVITY]: 'speed',
    [ModuleCategory.CUSTOMER_EXPERIENCE]: 'sentiment_satisfied',
    [ModuleCategory.SECURITY]: 'security',
    [ModuleCategory.AI_ML]: 'psychology',
    [ModuleCategory.FINANCE]: 'attach_money',
    [ModuleCategory.UTILITIES]: 'build',
    [ModuleCategory.THEMES]: 'palette',
    [ModuleCategory.LANGUAGES]: 'translate'
  };

  constructor(private marketplaceService: MarketplaceService) {}

  ngOnInit(): void {
    this.categories$ = this.marketplaceService.getCategories();
    this.featuredModules$ = this.marketplaceService.getFeaturedModules();
    this.popularModules$ = this.marketplaceService.getPopularModules();
    this.newestModules$ = this.marketplaceService.getNewestModules();

    // Search with debounce
    this.searchResults$ = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query && !this.selectedCategory) {
          return [];
        }
        return this.marketplaceService.searchModules({
          query: query || undefined,
          categories: this.selectedCategory ? [this.selectedCategory] : undefined,
          page: 0,
          size: 50
        }).pipe(map(page => page.content));
      }),
      startWith([])
    );
  }

  onSearchChange(query: string): void {
    this.searchSubject.next(query);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchSubject.next('');
  }

  selectCategory(category: ModuleCategory | null): void {
    this.selectedCategory = category;
    this.searchSubject.next(this.searchQuery);
  }

  getCategoryLabel(category: ModuleCategory): string {
    return CategoryLabels[category] || category;
  }

  getCategoryIcon(category: ModuleCategory): string {
    return this.categoryIcons[category] || 'extension';
  }

  onInstall(module: MarketplaceModule): void {
    // Navigate to module detail page for installation
    // Or open install dialog
  }

  onViewDetails(module: MarketplaceModule): void {
    // Navigate to module detail page
  }
}
