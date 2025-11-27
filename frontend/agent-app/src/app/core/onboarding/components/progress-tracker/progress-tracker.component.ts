import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingProgress, OnboardingStep } from '../../models/onboarding.models';

@Component({
  selector: 'app-progress-tracker',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatBadgeModule,
    TranslateModule
  ],
  template: `
    <div class="progress-tracker"
         [class.expanded]="isExpanded"
         [class.minimized]="isMinimized"
         *ngIf="progress && !progress.onboardingCompleted">

      <!-- Minimized state - floating button -->
      <button mat-fab
              *ngIf="isMinimized"
              class="progress-fab"
              [matTooltip]="'onboarding.progress.expand' | translate"
              (click)="toggleMinimize()">
        <mat-progress-spinner
          mode="determinate"
          [value]="progress.completionPercentage"
          diameter="48"
          strokeWidth="4">
        </mat-progress-spinner>
        <span class="progress-percent">{{ progress.completionPercentage }}%</span>
      </button>

      <!-- Collapsed state - compact bar -->
      <div class="progress-compact" *ngIf="!isMinimized && !isExpanded" (click)="toggleExpand()">
        <div class="compact-header">
          <mat-icon>school</mat-icon>
          <span class="compact-title">{{ 'onboarding.progress.title' | translate }}</span>
          <span class="compact-percentage">{{ progress.completionPercentage }}%</span>
          <button mat-icon-button class="minimize-btn" (click)="toggleMinimize(); $event.stopPropagation()">
            <mat-icon>remove</mat-icon>
          </button>
        </div>
        <div class="compact-progress-bar">
          <div class="progress-fill" [style.width.%]="progress.completionPercentage"></div>
        </div>
      </div>

      <!-- Expanded state - full panel -->
      <div class="progress-panel" *ngIf="isExpanded">
        <div class="panel-header">
          <div class="header-title">
            <mat-icon>school</mat-icon>
            <h3>{{ 'onboarding.progress.title' | translate }}</h3>
          </div>
          <div class="header-actions">
            <button mat-icon-button (click)="toggleExpand()" [matTooltip]="'common.collapse' | translate">
              <mat-icon>expand_less</mat-icon>
            </button>
            <button mat-icon-button (click)="toggleMinimize()" [matTooltip]="'common.minimize' | translate">
              <mat-icon>remove</mat-icon>
            </button>
          </div>
        </div>

        <div class="panel-content">
          <!-- Progress circle -->
          <div class="progress-circle-container">
            <mat-progress-spinner
              mode="determinate"
              [value]="progress.completionPercentage"
              diameter="100"
              strokeWidth="8">
            </mat-progress-spinner>
            <div class="progress-circle-content">
              <span class="progress-value">{{ progress.completionPercentage }}%</span>
              <span class="progress-label">{{ 'onboarding.progress.complete' | translate }}</span>
            </div>
          </div>

          <!-- Steps list -->
          <div class="steps-list">
            <div class="step-item"
                 *ngFor="let step of progress.steps; trackBy: trackByStepId"
                 [class.completed]="step.completed"
                 [class.current]="step.id === currentStepId"
                 (click)="onStepClick(step)">
              <div class="step-status">
                <mat-icon *ngIf="step.completed" class="completed-icon">check_circle</mat-icon>
                <div *ngIf="!step.completed" class="step-number">{{ step.order }}</div>
              </div>
              <div class="step-info">
                <span class="step-title">{{ step.title }}</span>
                <span class="step-description" *ngIf="step.id === currentStepId">{{ step.description }}</span>
              </div>
              <mat-icon class="step-action" *ngIf="!step.completed && step.hasTour">
                play_circle_outline
              </mat-icon>
            </div>
          </div>

          <!-- Quick actions -->
          <div class="quick-actions">
            <button mat-stroked-button (click)="startNextTour()" *ngIf="hasNextTour">
              <mat-icon>play_arrow</mat-icon>
              {{ 'onboarding.progress.startTour' | translate }}
            </button>
            <button mat-button color="warn" (click)="skipOnboarding()">
              {{ 'onboarding.progress.skip' | translate }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .progress-tracker {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
      transition: all 0.3s ease;
    }

    /* Minimized FAB */
    .progress-fab {
      position: relative;
      background: white;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);

      mat-progress-spinner {
        position: absolute;
        top: 0;
        left: 0;
      }

      .progress-percent {
        font-size: 12px;
        font-weight: 600;
        color: #1976d2;
      }
    }

    /* Compact bar */
    .progress-compact {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      padding: 12px 16px;
      cursor: pointer;
      min-width: 280px;
      transition: transform 0.2s ease;

      &:hover {
        transform: translateY(-2px);
      }

      .compact-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        mat-icon {
          color: #1976d2;
          font-size: 20px;
          width: 20px;
          height: 20px;
        }

        .compact-title {
          flex: 1;
          font-weight: 500;
          font-size: 14px;
        }

        .compact-percentage {
          font-weight: 600;
          color: #1976d2;
        }

        .minimize-btn {
          width: 24px;
          height: 24px;
          line-height: 24px;

          mat-icon {
            font-size: 18px;
          }
        }
      }

      .compact-progress-bar {
        height: 4px;
        background: rgba(25, 118, 210, 0.1);
        border-radius: 2px;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #1976d2, #42a5f5);
          border-radius: 2px;
          transition: width 0.5s ease;
        }
      }
    }

    /* Expanded panel */
    .progress-panel {
      background: white;
      border-radius: 16px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      width: 320px;
      max-height: 70vh;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      .panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px;
        border-bottom: 1px solid rgba(0, 0, 0, 0.08);
        background: linear-gradient(135deg, #1976d2, #1565c0);
        color: white;

        .header-title {
          display: flex;
          align-items: center;
          gap: 8px;

          h3 {
            margin: 0;
            font-size: 16px;
            font-weight: 500;
          }
        }

        .header-actions {
          display: flex;

          button {
            color: white;
          }
        }
      }

      .panel-content {
        padding: 20px;
        overflow-y: auto;
      }

      .progress-circle-container {
        position: relative;
        display: flex;
        justify-content: center;
        margin-bottom: 24px;

        mat-progress-spinner {
          ::ng-deep circle {
            stroke: #1976d2;
          }
        }

        .progress-circle-content {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          text-align: center;

          .progress-value {
            display: block;
            font-size: 24px;
            font-weight: 600;
            color: #1976d2;
          }

          .progress-label {
            display: block;
            font-size: 11px;
            color: rgba(0, 0, 0, 0.5);
          }
        }
      }

      .steps-list {
        margin-bottom: 20px;

        .step-item {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          padding: 12px;
          border-radius: 8px;
          cursor: pointer;
          transition: background 0.2s ease;

          &:hover {
            background: rgba(0, 0, 0, 0.04);
          }

          &.completed {
            .step-info .step-title {
              color: rgba(0, 0, 0, 0.5);
              text-decoration: line-through;
            }
          }

          &.current {
            background: rgba(25, 118, 210, 0.08);
            border-left: 3px solid #1976d2;
          }

          .step-status {
            flex-shrink: 0;

            .completed-icon {
              color: #4caf50;
            }

            .step-number {
              width: 24px;
              height: 24px;
              border-radius: 50%;
              background: rgba(0, 0, 0, 0.08);
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 12px;
              font-weight: 600;
            }
          }

          .step-info {
            flex: 1;
            min-width: 0;

            .step-title {
              display: block;
              font-size: 14px;
              font-weight: 500;
            }

            .step-description {
              display: block;
              font-size: 12px;
              color: rgba(0, 0, 0, 0.5);
              margin-top: 4px;
            }
          }

          .step-action {
            color: #1976d2;
            cursor: pointer;
          }
        }
      }

      .quick-actions {
        display: flex;
        flex-direction: column;
        gap: 8px;

        button {
          width: 100%;
        }
      }
    }
  `]
})
export class ProgressTrackerComponent implements OnInit, OnDestroy {
  @Input() autoExpand = false;
  @Output() stepSelected = new EventEmitter<OnboardingStep>();
  @Output() tourRequested = new EventEmitter<string>();

  private destroy$ = new Subject<void>();

  progress: OnboardingProgress | null = null;
  isExpanded = false;
  isMinimized = false;
  currentStepId: string | null = null;

  constructor(private onboardingService: OnboardingService) {}

  ngOnInit(): void {
    this.isExpanded = this.autoExpand;

    this.onboardingService.progress$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(progress => {
      this.progress = progress;
      this.updateCurrentStep();
    });

    this.onboardingService.loadProgress().subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded;
    if (this.isExpanded) {
      this.isMinimized = false;
    }
  }

  toggleMinimize(): void {
    this.isMinimized = !this.isMinimized;
    if (this.isMinimized) {
      this.isExpanded = false;
    }
  }

  onStepClick(step: OnboardingStep): void {
    this.currentStepId = step.id;
    this.stepSelected.emit(step);

    if (!step.completed && step.hasTour) {
      this.tourRequested.emit(step.id);
    }
  }

  startNextTour(): void {
    const nextStep = this.progress?.steps.find(s => !s.completed && s.hasTour);
    if (nextStep) {
      this.tourRequested.emit(nextStep.id);
    }
  }

  skipOnboarding(): void {
    this.onboardingService.skipOnboarding().subscribe(() => {
      this.isExpanded = false;
      this.isMinimized = true;
    });
  }

  get hasNextTour(): boolean {
    return this.progress?.steps.some(s => !s.completed && s.hasTour) ?? false;
  }

  trackByStepId(index: number, step: OnboardingStep): string {
    return step.id;
  }

  private updateCurrentStep(): void {
    if (this.progress) {
      const nextIncomplete = this.progress.steps.find(s => !s.completed);
      this.currentStepId = nextIncomplete?.id ?? null;
    }
  }
}
