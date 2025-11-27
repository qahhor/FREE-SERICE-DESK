export interface Article {
  id: string;
  title: string;
  slug: string;
  content: string;
  summary?: string;
  categoryId: string;
  categoryName: string;
  author: string;
  authorId: string;
  status: ArticleStatus;
  viewCount: number;
  helpfulCount: number;
  notHelpfulCount: number;
  tags?: string[];
  relatedArticles?: ArticleSummary[];
  attachments?: ArticleAttachment[];
  createdAt: Date;
  updatedAt: Date;
  publishedAt?: Date;
}

export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface ArticleSummary {
  id: string;
  title: string;
  slug: string;
  summary?: string;
  categoryName: string;
  viewCount: number;
  createdAt: Date;
}

export interface ArticleAttachment {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  url: string;
}

export interface KbCategory {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  slug: string;
  parentId?: string;
  articleCount: number;
  children?: KbCategory[];
  order: number;
}

export interface SearchRequest {
  query: string;
  categoryId?: string;
  page?: number;
  size?: number;
}

export interface SearchResult {
  articles: ArticleSummary[];
  totalCount: number;
  page: number;
  totalPages: number;
  suggestions?: string[];
}

export interface ArticleFeedback {
  articleId: string;
  helpful: boolean;
  comment?: string;
}
