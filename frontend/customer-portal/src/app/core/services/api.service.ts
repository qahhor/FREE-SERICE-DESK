import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse, PageRequest } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(path: string, params?: HttpParams): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(`${this.baseUrl}${path}`, { params });
  }

  getPage<T>(path: string, pageRequest?: PageRequest, params?: HttpParams): Observable<ApiResponse<PageResponse<T>>> {
    let httpParams = params || new HttpParams();

    if (pageRequest) {
      if (pageRequest.page !== undefined) {
        httpParams = httpParams.set('page', pageRequest.page.toString());
      }
      if (pageRequest.size !== undefined) {
        httpParams = httpParams.set('size', pageRequest.size.toString());
      }
      if (pageRequest.sort) {
        httpParams = httpParams.set('sort', pageRequest.sort);
      }
      if (pageRequest.direction) {
        httpParams = httpParams.set('direction', pageRequest.direction);
      }
    }

    return this.http.get<ApiResponse<PageResponse<T>>>(`${this.baseUrl}${path}`, { params: httpParams });
  }

  post<T>(path: string, body: any = {}): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(`${this.baseUrl}${path}`, body);
  }

  put<T>(path: string, body: any = {}): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(`${this.baseUrl}${path}`, body);
  }

  patch<T>(path: string, body: any = {}): Observable<ApiResponse<T>> {
    return this.http.patch<ApiResponse<T>>(`${this.baseUrl}${path}`, body);
  }

  delete<T>(path: string): Observable<ApiResponse<T>> {
    return this.http.delete<ApiResponse<T>>(`${this.baseUrl}${path}`);
  }

  upload<T>(path: string, formData: FormData): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(`${this.baseUrl}${path}`, formData);
  }

  download(path: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}${path}`, {
      responseType: 'blob'
    });
  }
}
