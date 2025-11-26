export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: ErrorDetails;
  timestamp: string;
}

export interface ErrorDetails {
  code: string;
  field?: string;
  rejectedValue?: any;
  details?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
