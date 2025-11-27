import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, PageResponse, PageRequest } from '../models/api-response.model';
import {
  Article,
  ArticleSummary,
  KbCategory,
  SearchRequest,
  SearchResult,
  ArticleFeedback
} from '../models/knowledge-base.model';

@Injectable({
  providedIn: 'root'
})
export class KnowledgeBaseService {
  private basePath = '/api/v1/knowledge';

  constructor(private api: ApiService) {}

  getCategories(): Observable<ApiResponse<KbCategory[]>> {
    return this.api.get<KbCategory[]>(`${this.basePath}/categories`);
  }

  getCategory(id: string): Observable<ApiResponse<KbCategory>> {
    return this.api.get<KbCategory>(`${this.basePath}/categories/${id}`);
  }

  getCategoryBySlug(slug: string): Observable<ApiResponse<KbCategory>> {
    return this.api.get<KbCategory>(`${this.basePath}/categories/slug/${slug}`);
  }

  getArticlesByCategory(categoryId: string, pageRequest?: PageRequest): Observable<ApiResponse<PageResponse<ArticleSummary>>> {
    return this.api.getPage<ArticleSummary>(`${this.basePath}/categories/${categoryId}/articles`, pageRequest);
  }

  getArticle(id: string): Observable<ApiResponse<Article>> {
    return this.api.get<Article>(`${this.basePath}/articles/${id}`);
  }

  getArticleBySlug(slug: string): Observable<ApiResponse<Article>> {
    return this.api.get<Article>(`${this.basePath}/articles/slug/${slug}`);
  }

  getPopularArticles(limit: number = 10): Observable<ApiResponse<ArticleSummary[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.api.get<ArticleSummary[]>(`${this.basePath}/articles/popular`, params);
  }

  getRecentArticles(limit: number = 10): Observable<ApiResponse<ArticleSummary[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.api.get<ArticleSummary[]>(`${this.basePath}/articles/recent`, params);
  }

  getRelatedArticles(articleId: string, limit: number = 5): Observable<ApiResponse<ArticleSummary[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.api.get<ArticleSummary[]>(`${this.basePath}/articles/${articleId}/related`, params);
  }

  search(request: SearchRequest): Observable<ApiResponse<SearchResult>> {
    let params = new HttpParams().set('query', request.query);

    if (request.categoryId) {
      params = params.set('categoryId', request.categoryId);
    }
    if (request.page !== undefined) {
      params = params.set('page', request.page.toString());
    }
    if (request.size !== undefined) {
      params = params.set('size', request.size.toString());
    }

    return this.api.get<SearchResult>(`${this.basePath}/search`, params);
  }

  submitFeedback(feedback: ArticleFeedback): Observable<ApiResponse<void>> {
    return this.api.post<void>(`${this.basePath}/articles/${feedback.articleId}/feedback`, feedback);
  }

  incrementViewCount(articleId: string): Observable<ApiResponse<void>> {
    return this.api.post<void>(`${this.basePath}/articles/${articleId}/view`, {});
  }

  getSuggestions(query: string): Observable<ApiResponse<string[]>> {
    const params = new HttpParams().set('query', query);
    return this.api.get<string[]>(`${this.basePath}/suggestions`, params);
  }
}
