import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(path: string, params: any = {}): Observable<T> {
    const httpParams = this.buildParams(params);
    return this.http.get<ApiResponse<T>>(`${this.baseUrl}${path}`, { params: httpParams })
      .pipe(map(response => response.data as T));
  }

  getPage<T>(path: string, params: any = {}): Observable<PageResponse<T>> {
    const httpParams = this.buildParams(params);
    return this.http.get<ApiResponse<PageResponse<T>>>(`${this.baseUrl}${path}`, { params: httpParams })
      .pipe(map(response => response.data as PageResponse<T>));
  }

  post<T>(path: string, body: any = {}): Observable<T> {
    return this.http.post<ApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(response => response.data as T));
  }

  put<T>(path: string, body: any = {}): Observable<T> {
    return this.http.put<ApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(response => response.data as T));
  }

  patch<T>(path: string, body: any = {}): Observable<T> {
    return this.http.patch<ApiResponse<T>>(`${this.baseUrl}${path}`, body)
      .pipe(map(response => response.data as T));
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<ApiResponse<T>>(`${this.baseUrl}${path}`)
      .pipe(map(response => response.data as T));
  }

  private buildParams(params: any): HttpParams {
    let httpParams = new HttpParams();

    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== null && value !== undefined && value !== '') {
        if (Array.isArray(value)) {
          value.forEach(v => {
            httpParams = httpParams.append(key, v);
          });
        } else {
          httpParams = httpParams.set(key, value.toString());
        }
      }
    });

    return httpParams;
  }
}
