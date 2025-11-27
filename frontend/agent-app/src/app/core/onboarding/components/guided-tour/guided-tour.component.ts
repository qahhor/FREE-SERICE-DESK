import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, Renderer2, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { OnboardingService } from '../../services/onboarding.service';
import { TourStep, OnboardingStep } from '../../models/onboarding.models';

@Component({
  selector: 'app-guided-tour',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    TranslateModule
  ],
  template: `
    <div class="guided-tour-overlay" *ngIf="isActive" (click)="onOverlayClick($event)">
      <!-- Spotlight highlight -->
      <div class="spotlight" *ngIf="spotlightStyle" [ngStyle]="spotlightStyle"></div>

      <!-- Tour tooltip -->
      <div class="tour-tooltip"
           *ngIf="currentTourStep"
           [ngStyle]="tooltipStyle"
           [class]="'position-' + currentTourStep.position">
        <div class="tooltip-arrow"></div>

        <div class="tooltip-header">
          <h3>{{ currentTourStep.title }}</h3>
          <button mat-icon-button class="close-btn" (click)="closeTour()">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <div class="tooltip-content">
          <p>{{ currentTourStep.content }}</p>
        </div>

        <div class="tooltip-footer">
          <div class="progress-info">
            <span>{{ currentStepIndex + 1 }} / {{ totalSteps }}</span>
            <mat-progress-bar mode="determinate"
                              [value]="((currentStepIndex + 1) / totalSteps) * 100">
            </mat-progress-bar>
          </div>

          <div class="actions">
            <button mat-button
                    *ngIf="currentStepIndex > 0"
                    (click)="previousStep()">
              <mat-icon>arrow_back</mat-icon>
              {{ currentTourStep.prevButtonLabel || ('common.back' | translate) }}
            </button>

            <button mat-flat-button color="primary"
                    *ngIf="currentStepIndex < totalSteps - 1"
                    (click)="nextStep()">
              {{ currentTourStep.nextButtonLabel || ('common.next' | translate) }}
              <mat-icon>arrow_forward</mat-icon>
            </button>

            <button mat-flat-button color="primary"
                    *ngIf="currentStepIndex === totalSteps - 1"
                    (click)="completeTour()">
              {{ 'onboarding.tour.finish' | translate }}
              <mat-icon>check</mat-icon>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .guided-tour-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      background: rgba(0, 0, 0, 0.5);
    }

    .spotlight {
      position: absolute;
      box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
      border-radius: 8px;
      transition: all 0.3s ease;
      pointer-events: none;
    }

    .tour-tooltip {
      position: absolute;
      width: 360px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      z-index: 10000;
      animation: tooltipFadeIn 0.3s ease;

      .tooltip-arrow {
        position: absolute;
        width: 0;
        height: 0;
        border: 10px solid transparent;
      }

      &.position-bottom {
        .tooltip-arrow {
          top: -20px;
          left: 50%;
          transform: translateX(-50%);
          border-bottom-color: white;
        }
      }

      &.position-top {
        .tooltip-arrow {
          bottom: -20px;
          left: 50%;
          transform: translateX(-50%);
          border-top-color: white;
        }
      }

      &.position-left {
        .tooltip-arrow {
          right: -20px;
          top: 50%;
          transform: translateY(-50%);
          border-left-color: white;
        }
      }

      &.position-right {
        .tooltip-arrow {
          left: -20px;
          top: 50%;
          transform: translateY(-50%);
          border-right-color: white;
        }
      }

      &.position-bottom-left {
        .tooltip-arrow {
          top: -20px;
          left: 24px;
          border-bottom-color: white;
        }
      }

      &.position-bottom-right {
        .tooltip-arrow {
          top: -20px;
          right: 24px;
          border-bottom-color: white;
        }
      }
    }

    .tooltip-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 16px 8px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 500;
      }

      .close-btn {
        margin: -8px -8px 0 0;
      }
    }

    .tooltip-content {
      padding: 16px;

      p {
        margin: 0;
        color: rgba(0, 0, 0, 0.7);
        line-height: 1.6;
      }
    }

    .tooltip-footer {
      padding: 12px 16px 16px;
      border-top: 1px solid rgba(0, 0, 0, 0.08);

      .progress-info {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        span {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.5);
          white-space: nowrap;
        }

        mat-progress-bar {
          flex: 1;
          height: 4px;
        }
      }

      .actions {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
      }
    }

    @keyframes tooltipFadeIn {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `]
})
export class GuidedTourComponent implements OnInit, OnDestroy {
  @Input() stepId!: string;
  @Output() tourCompleted = new EventEmitter<string>();
  @Output() tourClosed = new EventEmitter<void>();

  private destroy$ = new Subject<void>();

  isActive = false;
  tourSteps: TourStep[] = [];
  currentStepIndex = 0;
  currentTourStep: TourStep | null = null;
  spotlightStyle: Record<string, string> | null = null;
  tooltipStyle: Record<string, string> = {};
  totalSteps = 0;

  constructor(
    private onboardingService: OnboardingService,
    private renderer: Renderer2
  ) {}

  ngOnInit(): void {
    if (this.stepId) {
      this.loadTourConfig();
    }

    this.onboardingService.tourActive$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(active => {
      this.isActive = active;
      if (active) {
        this.disableScroll();
      } else {
        this.enableScroll();
      }
    });

    this.onboardingService.currentTourStep$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(index => {
      this.currentStepIndex = index;
      this.updateCurrentStep();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.enableScroll();
  }

  loadTourConfig(): void {
    this.onboardingService.getTourConfig(this.stepId).subscribe({
      next: (step) => {
        this.tourSteps = step.tourSteps || [];
        this.totalSteps = this.tourSteps.length;
        if (this.tourSteps.length > 0) {
          this.onboardingService.startTour(this.stepId);
          this.updateCurrentStep();
        }
      },
      error: (err) => {
        console.error('Failed to load tour config', err);
      }
    });
  }

  startTour(steps: TourStep[]): void {
    this.tourSteps = steps;
    this.totalSteps = steps.length;
    this.currentStepIndex = 0;
    this.onboardingService.startTour(this.stepId);
    this.updateCurrentStep();
  }

  updateCurrentStep(): void {
    if (this.currentStepIndex < this.tourSteps.length) {
      this.currentTourStep = this.tourSteps[this.currentStepIndex];
      this.positionTooltip();
    }
  }

  positionTooltip(): void {
    if (!this.currentTourStep) return;

    const targetElement = document.querySelector(this.currentTourStep.elementSelector);
    if (!targetElement) {
      console.warn('Tour target element not found:', this.currentTourStep.elementSelector);
      return;
    }

    const rect = targetElement.getBoundingClientRect();
    const padding = 8;

    // Set spotlight
    this.spotlightStyle = {
      top: `${rect.top - padding}px`,
      left: `${rect.left - padding}px`,
      width: `${rect.width + padding * 2}px`,
      height: `${rect.height + padding * 2}px`
    };

    // Position tooltip based on specified position
    const tooltipWidth = 360;
    const tooltipHeight = 200; // Approximate
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    let top: number;
    let left: number;

    switch (this.currentTourStep.position) {
      case 'bottom':
        top = rect.bottom + 20;
        left = rect.left + rect.width / 2 - tooltipWidth / 2;
        break;
      case 'top':
        top = rect.top - tooltipHeight - 20;
        left = rect.left + rect.width / 2 - tooltipWidth / 2;
        break;
      case 'left':
        top = rect.top + rect.height / 2 - tooltipHeight / 2;
        left = rect.left - tooltipWidth - 20;
        break;
      case 'right':
        top = rect.top + rect.height / 2 - tooltipHeight / 2;
        left = rect.right + 20;
        break;
      case 'bottom-left':
        top = rect.bottom + 20;
        left = rect.left;
        break;
      case 'bottom-right':
        top = rect.bottom + 20;
        left = rect.right - tooltipWidth;
        break;
      default:
        top = rect.bottom + 20;
        left = rect.left;
    }

    // Keep tooltip within viewport
    left = Math.max(16, Math.min(left, viewportWidth - tooltipWidth - 16));
    top = Math.max(16, Math.min(top, viewportHeight - tooltipHeight - 16));

    this.tooltipStyle = {
      top: `${top}px`,
      left: `${left}px`
    };

    // Scroll element into view if needed
    targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  nextStep(): void {
    if (this.currentStepIndex < this.totalSteps - 1) {
      this.onboardingService.nextTourStep();
    }
  }

  previousStep(): void {
    if (this.currentStepIndex > 0) {
      this.onboardingService.prevTourStep();
    }
  }

  completeTour(): void {
    this.onboardingService.completeTour(this.stepId).subscribe(() => {
      this.tourCompleted.emit(this.stepId);
    });
  }

  closeTour(): void {
    this.onboardingService.stopTour();
    this.tourClosed.emit();
  }

  onOverlayClick(event: MouseEvent): void {
    // Only close if clicking on overlay, not on tooltip
    if ((event.target as HTMLElement).classList.contains('guided-tour-overlay')) {
      // Optionally close or just prevent
    }
  }

  private disableScroll(): void {
    this.renderer.addClass(document.body, 'tour-active');
    this.renderer.setStyle(document.body, 'overflow', 'hidden');
  }

  private enableScroll(): void {
    this.renderer.removeClass(document.body, 'tour-active');
    this.renderer.removeStyle(document.body, 'overflow');
  }
}
