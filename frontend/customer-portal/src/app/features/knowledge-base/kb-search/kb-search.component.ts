import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { KnowledgeBaseService } from '../../../core/services/knowledge-base.service';
import { ArticleSummary, SearchResult } from '../../../core/models/knowledge-base.model';

@Component({
  selector: 'app-kb-search',
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
    MatPaginatorModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="search-container">
      <div class="search-header">
        <a mat-icon-button routerLink="/knowledge-base">
          <mat-icon>arrow_back</mat-icon>
        </a>
        <mat-form-field appearance="outline" class="search-field">
          <mat-icon matPrefix>search</mat-icon>
          <input matInput [(ngModel)]="query" (keyup.enter)="search()" [placeholder]="'kb.search_placeholder' | translate">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="search()">
          {{ 'kb.search' | translate }}
        </button>
      </div>

      <div class="search-results">
        <div class="results-header" *ngIf="!isLoading && hasSearched">
          <h2>{{ 'kb.results_for' | translate }} "{{ query }}"</h2>
          <span>{{ totalCount }} {{ 'kb.results' | translate }}</span>
        </div>

        <mat-spinner *ngIf="isLoading" diameter="40"></mat-spinner>

        <div class="results-list" *ngIf="!isLoading">
          <mat-card *ngFor="let article of results" class="result-card" [routerLink]="['/knowledge-base/article', article.id]">
            <h3>{{ article.title }}</h3>
            <p>{{ article.summary }}</p>
            <div class="result-meta">
              <span>{{ article.categoryName }}</span>
              <span>{{ article.viewCount }} {{ 'kb.views' | translate }}</span>
            </div>
          </mat-card>

          <div class="no-results" *ngIf="results.length === 0 && hasSearched">
            <mat-icon>search_off</mat-icon>
            <h3>{{ 'kb.no_results' | translate }}</h3>
            <p>{{ 'kb.no_results_message' | translate }}</p>
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
    </div>
  `,
  styles: [`
    .search-container {
      max-width: 900px;
      margin: 0 auto;
    }

    .search-header {
      display: flex;
      gap: 12px;
      align-items: center;
      margin-bottom: 24px;

      .search-field {
        flex: 1;
      }
    }

    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h2 {
        margin: 0;
        color: #333;
      }

      span {
        color: #666;
      }
    }

    .results-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }

    .result-card {
      padding: 20px;
      cursor: pointer;
      transition: box-shadow 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      h3 {
        margin: 0 0 8px;
        color: #673ab7;
      }

      p {
        margin: 0 0 12px;
        color: #666;
      }

      .result-meta {
        display: flex;
        gap: 16px;
        color: #999;
        font-size: 12px;
      }
    }

    .no-results {
      text-align: center;
      padding: 48px;

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #ccc;
      }

      h3 {
        margin: 16px 0 8px;
        color: #333;
      }

      p {
        color: #666;
      }
    }
  `]
})
export class KbSearchComponent implements OnInit {
  query = '';
  results: ArticleSummary[] = [];
  totalCount = 0;
  currentPage = 0;
  pageSize = 10;
  isLoading = false;
  hasSearched = false;

  constructor(
    private route: ActivatedRoute,
    private kbService: KnowledgeBaseService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['q']) {
        this.query = params['q'];
        this.search();
      }
    });
  }

  search(): void {
    if (!this.query.trim()) return;

    this.isLoading = true;
    this.hasSearched = true;

    this.kbService.search({
      query: this.query,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.results = response.data.articles;
          this.totalCount = response.data.totalCount;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.search();
  }
}
