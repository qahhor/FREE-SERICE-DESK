import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { KnowledgeBaseService } from '../../../core/services/knowledge-base.service';
import { Article, ArticleSummary } from '../../../core/models/knowledge-base.model';

@Component({
  selector: 'app-kb-article',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule
  ],
  template: `
    <div class="article-container" *ngIf="article; else loading">
      <div class="header">
        <a mat-icon-button [routerLink]="['/knowledge-base/category', article.categoryId]">
          <mat-icon>arrow_back</mat-icon>
        </a>
        <div class="breadcrumb">
          <a routerLink="/knowledge-base">{{ 'kb.title' | translate }}</a>
          <mat-icon>chevron_right</mat-icon>
          <a [routerLink]="['/knowledge-base/category', article.categoryId]">{{ article.categoryName }}</a>
        </div>
      </div>

      <article class="article-content">
        <h1>{{ article.title }}</h1>

        <div class="article-meta">
          <span><mat-icon>person</mat-icon> {{ article.author }}</span>
          <span><mat-icon>calendar_today</mat-icon> {{ article.publishedAt | date:'mediumDate' }}</span>
          <span><mat-icon>visibility</mat-icon> {{ article.viewCount }}</span>
        </div>

        <div class="tags" *ngIf="article.tags && article.tags.length > 0">
          <mat-chip-set>
            <mat-chip *ngFor="let tag of article.tags">{{ tag }}</mat-chip>
          </mat-chip-set>
        </div>

        <div class="content" [innerHTML]="article.content"></div>

        <!-- Feedback -->
        <div class="feedback-section" *ngIf="!hasGivenFeedback">
          <h3>{{ 'kb.was_helpful' | translate }}</h3>
          <div class="feedback-buttons">
            <button mat-stroked-button (click)="submitFeedback(true)">
              <mat-icon>thumb_up</mat-icon> {{ 'kb.yes' | translate }}
            </button>
            <button mat-stroked-button (click)="submitFeedback(false)">
              <mat-icon>thumb_down</mat-icon> {{ 'kb.no' | translate }}
            </button>
          </div>
        </div>

        <div class="feedback-thanks" *ngIf="hasGivenFeedback">
          <mat-icon>check_circle</mat-icon>
          <span>{{ 'kb.thanks_feedback' | translate }}</span>
        </div>
      </article>

      <!-- Related Articles -->
      <aside class="related-articles" *ngIf="relatedArticles.length > 0">
        <h3>{{ 'kb.related_articles' | translate }}</h3>
        <mat-card *ngFor="let related of relatedArticles"
                  class="related-card"
                  [routerLink]="['/knowledge-base/article', related.id]">
          <h4>{{ related.title }}</h4>
          <p>{{ related.summary }}</p>
        </mat-card>
      </aside>
    </div>

    <ng-template #loading>
      <div class="loading"><mat-spinner diameter="40"></mat-spinner></div>
    </ng-template>
  `,
  styles: [`
    .article-container {
      max-width: 900px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
    }

    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;

      a {
        color: #673ab7;
        text-decoration: none;

        &:hover { text-decoration: underline; }
      }

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: #999;
      }
    }

    .article-content {
      background: white;
      border-radius: 8px;
      padding: 32px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      h1 {
        margin: 0 0 16px;
        color: #333;
        font-size: 28px;
      }

      .article-meta {
        display: flex;
        gap: 24px;
        color: #666;
        font-size: 14px;
        margin-bottom: 16px;

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

      .tags {
        margin-bottom: 24px;
      }

      .content {
        line-height: 1.8;
        color: #444;

        h2, h3, h4 { color: #333; margin-top: 24px; }
        p { margin: 16px 0; }
        ul, ol { margin: 16px 0; padding-left: 24px; }
        code {
          background: #f5f5f5;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: monospace;
        }
        pre {
          background: #f5f5f5;
          padding: 16px;
          border-radius: 8px;
          overflow-x: auto;
        }
        img { max-width: 100%; border-radius: 8px; }
      }
    }

    .feedback-section {
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #eee;
      text-align: center;

      h3 { margin: 0 0 16px; color: #666; font-weight: normal; }

      .feedback-buttons {
        display: flex;
        justify-content: center;
        gap: 16px;
      }
    }

    .feedback-thanks {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #4caf50;
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #eee;
    }

    .related-articles {
      margin-top: 32px;

      h3 {
        margin: 0 0 16px;
        color: #333;
      }
    }

    .related-card {
      padding: 16px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: box-shadow 0.2s;

      &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }

      h4 { margin: 0 0 4px; color: #673ab7; font-size: 15px; }
      p { margin: 0; color: #666; font-size: 13px; }
    }

    .loading { display: flex; justify-content: center; padding: 48px; }
  `]
})
export class KbArticleComponent implements OnInit {
  @Input() id!: string;

  article: Article | null = null;
  relatedArticles: ArticleSummary[] = [];
  hasGivenFeedback = false;

  constructor(
    private kbService: KnowledgeBaseService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadArticle();
  }

  private loadArticle(): void {
    this.kbService.getArticle(this.id).subscribe(response => {
      if (response.success && response.data) {
        this.article = response.data;
        this.kbService.incrementViewCount(this.id).subscribe();
        this.loadRelatedArticles();
      }
    });
  }

  private loadRelatedArticles(): void {
    this.kbService.getRelatedArticles(this.id).subscribe(response => {
      if (response.success && response.data) {
        this.relatedArticles = response.data;
      }
    });
  }

  submitFeedback(helpful: boolean): void {
    this.kbService.submitFeedback({
      articleId: this.id,
      helpful
    }).subscribe(() => {
      this.hasGivenFeedback = true;
      this.snackBar.open('Thank you for your feedback!', 'Close', { duration: 3000 });
    });
  }
}
