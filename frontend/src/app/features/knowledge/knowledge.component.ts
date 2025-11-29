import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LayoutComponent } from '@shared/components/layout/layout.component';

@Component({
  selector: 'app-knowledge',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, LayoutComponent],
  template: `
    <app-layout>
      <div class="knowledge-container">
        <div class="header">
          <h1>Knowledge Base</h1>
          <button mat-raised-button color="primary">
            <mat-icon>add</mat-icon>
            New Article
          </button>
        </div>

        <!-- Search -->
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Search articles</mat-label>
          <input matInput placeholder="Search...">
          <mat-icon matPrefix>search</mat-icon>
        </mat-form-field>

        <!-- Articles List -->
        <div class="articles-grid">
          <mat-card *ngFor="let article of sampleArticles" class="article-card">
            <mat-card-header>
              <mat-card-title>{{ article.title }}</mat-card-title>
              <mat-card-subtitle>{{ article.category }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <p>{{ article.excerpt }}</p>
            </mat-card-content>
            <mat-card-actions>
              <button mat-button color="primary">Read More</button>
              <button mat-icon-button><mat-icon>edit</mat-icon></button>
            </mat-card-actions>
          </mat-card>
        </div>
      </div>
    </app-layout>
  `,
  styles: [`
    .knowledge-container {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;

        h1 { margin: 0; }

        button {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .search-field {
        width: 100%;
        max-width: 600px;
        margin-bottom: 24px;
      }

      .articles-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
        gap: 16px;

        .article-card {
          mat-card-content {
            min-height: 80px;
            p { color: #666; }
          }
        }
      }
    }
  `]
})
export class KnowledgeComponent {
  sampleArticles = [
    { title: 'How to reset password', category: 'Account', excerpt: 'Learn how to reset your password...' },
    { title: 'Getting started guide', category: 'General', excerpt: 'Quick start guide for new users...' },
    { title: 'API documentation', category: 'Developer', excerpt: 'Complete API reference...' }
  ];
}
