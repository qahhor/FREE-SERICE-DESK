export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: ApiError[];
  timestamp: Date;
}

export interface ApiError {
  code: string;
  field?: string;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}
