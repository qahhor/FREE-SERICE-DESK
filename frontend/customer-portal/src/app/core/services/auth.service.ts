import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  UpdateProfileRequest,
  ChangePasswordRequest
} from '../models/user.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/api/v1/auth`;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
      } catch {
        this.clearStorage();
      }
    }
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  get isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/login`, request).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.handleAuthentication(response.data);
        }
      }),
      catchError(error => {
        console.error('Login failed:', error);
        return throwError(() => error);
      })
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/register`, request).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.handleAuthentication(response.data);
        }
      })
    );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/reset-password`, request);
  }

  refreshToken(): Observable<ApiResponse<LoginResponse>> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<ApiResponse<LoginResponse>>(`${this.baseUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.handleAuthentication(response.data);
        }
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    // Call backend logout if needed
    const token = this.getToken();
    if (token) {
      this.http.post(`${this.baseUrl}/logout`, {}).subscribe({
        complete: () => this.clearAndRedirect()
      });
    } else {
      this.clearAndRedirect();
    }
  }

  private clearAndRedirect(): void {
    this.clearStorage();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  updateProfile(request: UpdateProfileRequest): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.baseUrl}/profile`, request).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.currentUserSubject.next(response.data);
          localStorage.setItem(this.USER_KEY, JSON.stringify(response.data));
        }
      })
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/change-password`, request);
  }

  verifyEmail(token: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/verify-email`, { token });
  }

  resendVerificationEmail(): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/resend-verification`, {});
  }

  private handleAuthentication(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response.user));
    this.currentUserSubject.next(response.user);
  }

  private clearStorage(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }
}
