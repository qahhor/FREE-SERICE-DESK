import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { KnowledgeBaseService } from '../../../core/services/knowledge-base.service';
import { KbCategory, ArticleSummary } from '../../../core/models/knowledge-base.model';

@Component({
  selector: 'app-kb-category',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="category-container" *ngIf="category; else loading">
      <div class="header">
        <a mat-icon-button routerLink="/knowledge-base">
          <mat-icon>arrow_back</mat-icon>
        </a>
        <div class="header-info">
          <mat-icon class="category-icon">{{ category.icon || 'folder' }}</mat-icon>
          <div>
            <h1>{{ category.name }}</h1>
            <p>{{ category.description }}</p>
          </div>
        </div>
      </div>

      <div class="articles-list">
        <mat-card *ngFor="let article of articles" class="article-card" [routerLink]="['/knowledge-base/article', article.id]">
          <h3>{{ article.title }}</h3>
          <p>{{ article.summary }}</p>
          <div class="article-meta">
            <span><mat-icon>visibility</mat-icon> {{ article.viewCount }}</span>
            <span>{{ article.createdAt | date:'mediumDate' }}</span>
          </div>
        </mat-card>

        <div class="empty-state" *ngIf="articles.length === 0 && !isLoading">
          <mat-icon>article</mat-icon>
          <h3>{{ 'kb.no_articles' | translate }}</h3>
        </div>
      </div>

      <mat-paginator *ngIf="totalCount > 0"
                     [length]="totalCount"
                     [pageSize]="pageSize"
                     [pageIndex]="currentPage"
                     (page)="onPageChange($event)"
                     showFirstLastButtons>
      </mat-paginator>
    </div>

    <ng-template #loading>
      <div class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    </ng-template>
  `,
  styles: [`
    .category-container { max-width: 900px; margin: 0 auto; }

    .header {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 32px;

      .header-info {
        display: flex;
        gap: 16px;
        align-items: center;
      }

      .category-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #673ab7;
      }

      h1 { margin: 0 0 4px; color: #333; }
      p { margin: 0; color: #666; }
    }

    .articles-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }

    .article-card {
      padding: 20px;
      cursor: pointer;
      transition: box-shadow 0.2s;

      &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }

      h3 { margin: 0 0 8px; color: #333; }
      p { margin: 0 0 12px; color: #666; }

      .article-meta {
        display: flex;
        gap: 16px;
        color: #999;
        font-size: 12px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;

          mat-icon { font-size: 14px; width: 14px; height: 14px; }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 48px;

      mat-icon { font-size: 64px; width: 64px; height: 64px; color: #ccc; }
      h3 { margin: 16px 0 0; color: #666; }
    }

    .loading { display: flex; justify-content: center; padding: 48px; }
  `]
})
export class KbCategoryComponent implements OnInit {
  @Input() id!: string;

  category: KbCategory | null = null;
  articles: ArticleSummary[] = [];
  totalCount = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = true;

  constructor(private kbService: KnowledgeBaseService) {}

  ngOnInit(): void {
    this.loadCategory();
    this.loadArticles();
  }

  private loadCategory(): void {
    this.kbService.getCategory(this.id).subscribe(response => {
      if (response.success && response.data) {
        this.category = response.data;
      }
    });
  }

  private loadArticles(): void {
    this.isLoading = true;
    this.kbService.getArticlesByCategory(this.id, {
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.articles = response.data.content;
          this.totalCount = response.data.totalElements;
        }
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadArticles();
  }
}
