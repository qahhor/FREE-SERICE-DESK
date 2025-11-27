import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { KnowledgeBaseService } from '../../../core/services/knowledge-base.service';
import { KbCategory, ArticleSummary } from '../../../core/models/knowledge-base.model';

@Component({
  selector: 'app-kb-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="kb-home">
      <div class="hero-section">
        <h1>{{ 'kb.title' | translate }}</h1>
        <p>{{ 'kb.subtitle' | translate }}</p>

        <div class="search-box">
          <mat-form-field appearance="outline" class="search-field">
            <mat-icon matPrefix>search</mat-icon>
            <input matInput [(ngModel)]="searchQuery"
                   (keyup.enter)="search()"
                   [placeholder]="'kb.search_placeholder' | translate">
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="search()">
            {{ 'kb.search' | translate }}
          </button>
        </div>
      </div>

      <div class="content-section">
        <!-- Categories -->
        <section class="categories-section">
          <h2>{{ 'kb.categories' | translate }}</h2>
          <div class="categories-grid" *ngIf="!isLoading">
            <mat-card *ngFor="let category of categories"
                      class="category-card"
                      [routerLink]="['/knowledge-base/category', category.id]">
              <mat-icon>{{ category.icon || 'folder' }}</mat-icon>
              <h3>{{ category.name }}</h3>
              <p>{{ category.description }}</p>
              <span class="article-count">{{ category.articleCount }} {{ 'kb.articles' | translate }}</span>
            </mat-card>
          </div>
          <mat-spinner *ngIf="isLoading" diameter="40"></mat-spinner>
        </section>

        <!-- Popular Articles -->
        <section class="popular-section">
          <h2>{{ 'kb.popular_articles' | translate }}</h2>
          <div class="article-list">
            <mat-card *ngFor="let article of popularArticles"
                      class="article-card"
                      [routerLink]="['/knowledge-base/article', article.id]">
              <div class="article-info">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
              </div>
              <div class="article-meta">
                <span><mat-icon>visibility</mat-icon> {{ article.viewCount }}</span>
                <span>{{ article.categoryName }}</span>
              </div>
            </mat-card>
          </div>
        </section>

        <!-- Recent Articles -->
        <section class="recent-section">
          <h2>{{ 'kb.recent_articles' | translate }}</h2>
          <div class="article-list">
            <mat-card *ngFor="let article of recentArticles"
                      class="article-card"
                      [routerLink]="['/knowledge-base/article', article.id]">
              <div class="article-info">
                <h4>{{ article.title }}</h4>
                <p>{{ article.summary }}</p>
              </div>
              <div class="article-meta">
                <span>{{ article.createdAt | date:'mediumDate' }}</span>
                <span>{{ article.categoryName }}</span>
              </div>
            </mat-card>
          </div>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .kb-home {
      max-width: 1200px;
      margin: 0 auto;
    }

    .hero-section {
      text-align: center;
      padding: 48px 24px;
      background: linear-gradient(135deg, #673ab7 0%, #512da8 100%);
      color: white;
      border-radius: 12px;
      margin-bottom: 32px;

      h1 {
        margin: 0 0 8px;
        font-size: 36px;
      }

      p {
        margin: 0 0 24px;
        opacity: 0.9;
      }
    }

    .search-box {
      display: flex;
      gap: 12px;
      max-width: 600px;
      margin: 0 auto;

      .search-field {
        flex: 1;

        ::ng-deep .mat-mdc-form-field-subscript-wrapper {
          display: none;
        }

        ::ng-deep .mat-mdc-text-field-wrapper {
          background: white;
          border-radius: 8px;
        }
      }
    }

    .content-section {
      section {
        margin-bottom: 48px;

        h2 {
          margin: 0 0 24px;
          color: #333;
        }
      }
    }

    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .category-card {
      padding: 24px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #673ab7;
      }

      h3 {
        margin: 16px 0 8px;
        color: #333;
      }

      p {
        color: #666;
        font-size: 14px;
        margin: 0 0 8px;
      }

      .article-count {
        color: #999;
        font-size: 12px;
      }
    }

    .article-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .article-card {
      padding: 16px;
      cursor: pointer;
      transition: box-shadow 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .article-info {
        h4 {
          margin: 0 0 8px;
          color: #333;
        }

        p {
          margin: 0;
          color: #666;
          font-size: 14px;
        }
      }

      .article-meta {
        display: flex;
        justify-content: space-between;
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid #eee;
        color: #999;
        font-size: 12px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;

          mat-icon {
            font-size: 14px;
            width: 14px;
            height: 14px;
          }
        }
      }
    }

    @media (max-width: 768px) {
      .hero-section {
        padding: 32px 16px;

        h1 { font-size: 28px; }
      }

      .search-box {
        flex-direction: column;
      }
    }
  `]
})
export class KbHomeComponent implements OnInit {
  categories: KbCategory[] = [];
  popularArticles: ArticleSummary[] = [];
  recentArticles: ArticleSummary[] = [];
  searchQuery = '';
  isLoading = true;

  constructor(private kbService: KnowledgeBaseService) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.kbService.getCategories().subscribe(response => {
      if (response.success && response.data) {
        this.categories = response.data;
      }
      this.isLoading = false;
    });

    this.kbService.getPopularArticles(5).subscribe(response => {
      if (response.success && response.data) {
        this.popularArticles = response.data;
      }
    });

    this.kbService.getRecentArticles(5).subscribe(response => {
      if (response.success && response.data) {
        this.recentArticles = response.data;
      }
    });
  }

  search(): void {
    if (this.searchQuery.trim()) {
      window.location.href = `/knowledge-base/search?q=${encodeURIComponent(this.searchQuery)}`;
    }
  }
}
