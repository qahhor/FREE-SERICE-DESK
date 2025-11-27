import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  OnboardingProgress,
  OnboardingStep,
  OnboardingStats,
  UserRole,
  OnboardingStatus
} from '../models/onboarding.models';

@Injectable({
  providedIn: 'root'
})
export class OnboardingService {
  private readonly apiUrl = `${environment.apiUrl}/onboarding`;

  private progressSubject = new BehaviorSubject<OnboardingProgress | null>(null);
  public progress$ = this.progressSubject.asObservable();

  private tourActiveSubject = new BehaviorSubject<boolean>(false);
  public tourActive$ = this.tourActiveSubject.asObservable();

  private currentTourStepSubject = new BehaviorSubject<number>(0);
  public currentTourStep$ = this.currentTourStepSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Initialize onboarding on app load
   */
  initOnboarding(role: UserRole = UserRole.AGENT): Observable<OnboardingProgress> {
    return this.http.get<OnboardingProgress>(`${this.apiUrl}/progress`, {
      params: { role }
    }).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Get current progress
   */
  getProgress(): OnboardingProgress | null {
    return this.progressSubject.getValue();
  }

  /**
   * Refresh progress from server
   */
  refreshProgress(): Observable<OnboardingProgress> {
    return this.http.get<OnboardingProgress>(`${this.apiUrl}/progress`).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Mark a step as completed
   */
  completeStep(stepId: string): Observable<OnboardingProgress> {
    return this.http.post<OnboardingProgress>(`${this.apiUrl}/steps/${stepId}/complete`, {}).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Mark a tour as completed
   */
  completeTour(tourId: string): Observable<OnboardingProgress> {
    return this.http.post<OnboardingProgress>(`${this.apiUrl}/tours/${tourId}/complete`, {}).pipe(
      tap(progress => {
        this.progressSubject.next(progress);
        this.tourActiveSubject.next(false);
        this.currentTourStepSubject.next(0);
      })
    );
  }

  /**
   * Dismiss welcome wizard
   */
  dismissWelcome(): Observable<OnboardingProgress> {
    return this.http.post<OnboardingProgress>(`${this.apiUrl}/welcome/dismiss`, {}).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Skip onboarding entirely
   */
  skipOnboarding(): Observable<OnboardingProgress> {
    return this.http.post<OnboardingProgress>(`${this.apiUrl}/skip`, {}).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Toggle contextual hints
   */
  toggleHints(enabled: boolean): Observable<OnboardingProgress> {
    return this.http.put<OnboardingProgress>(`${this.apiUrl}/hints`, {}, {
      params: { enabled: enabled.toString() }
    }).pipe(
      tap(progress => this.progressSubject.next(progress))
    );
  }

  /**
   * Get steps for a specific role
   */
  getSteps(role: UserRole): Observable<OnboardingStep[]> {
    return this.http.get<OnboardingStep[]>(`${this.apiUrl}/steps`, {
      params: { role }
    });
  }

  /**
   * Get tour configuration for a step
   */
  getTourConfig(stepId: string): Observable<OnboardingStep> {
    return this.http.get<OnboardingStep>(`${this.apiUrl}/tours/${stepId}`);
  }

  /**
   * Get onboarding statistics
   */
  getStats(): Observable<OnboardingStats> {
    return this.http.get<OnboardingStats>(`${this.apiUrl}/stats`);
  }

  /**
   * Track user action for auto-completion
   */
  trackAction(actionType: string, data: Record<string, any> = {}): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/action`, {
      actionType,
      data
    });
  }

  // Tour control methods

  /**
   * Start a guided tour
   */
  startTour(stepId: string): void {
    this.tourActiveSubject.next(true);
    this.currentTourStepSubject.next(0);
  }

  /**
   * Stop the current tour
   */
  stopTour(): void {
    this.tourActiveSubject.next(false);
    this.currentTourStepSubject.next(0);
  }

  /**
   * Go to next tour step
   */
  nextTourStep(): void {
    const current = this.currentTourStepSubject.getValue();
    this.currentTourStepSubject.next(current + 1);
  }

  /**
   * Go to previous tour step
   */
  prevTourStep(): void {
    const current = this.currentTourStepSubject.getValue();
    if (current > 0) {
      this.currentTourStepSubject.next(current - 1);
    }
  }

  /**
   * Check if onboarding should be shown
   */
  shouldShowOnboarding(): boolean {
    const progress = this.progressSubject.getValue();
    if (!progress) return false;

    return progress.status !== OnboardingStatus.COMPLETED
      && progress.status !== OnboardingStatus.SKIPPED
      && !progress.welcomeDismissed;
  }

  /**
   * Check if checklist should be shown
   */
  shouldShowChecklist(): boolean {
    const progress = this.progressSubject.getValue();
    if (!progress) return false;

    return progress.status !== OnboardingStatus.COMPLETED
      && progress.status !== OnboardingStatus.SKIPPED
      && progress.welcomeDismissed
      && !progress.checklistCompleted;
  }

  /**
   * Check if hints are enabled
   */
  areHintsEnabled(): boolean {
    const progress = this.progressSubject.getValue();
    return progress?.hintsEnabled ?? true;
  }
}
