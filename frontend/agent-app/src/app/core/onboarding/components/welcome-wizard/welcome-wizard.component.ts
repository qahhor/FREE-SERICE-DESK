import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatStepperModule } from '@angular/material/stepper';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingProgress, OnboardingStep, StepType } from '../../models/onboarding.models';

@Component({
  selector: 'app-welcome-wizard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatStepperModule,
    MatProgressBarModule,
    TranslateModule
  ],
  template: `
    <div class="welcome-wizard" *ngIf="progress">
      <!-- Header -->
      <div class="wizard-header">
        <div class="logo">
          <mat-icon>support_agent</mat-icon>
        </div>
        <h1>{{ 'onboarding.welcome.title' | translate }}</h1>
        <p>{{ 'onboarding.welcome.subtitle' | translate }}</p>
      </div>

      <!-- Content based on current page -->
      <div class="wizard-content" [ngSwitch]="currentPage">
        <!-- Page 1: Welcome -->
        <div *ngSwitchCase="0" class="page welcome-page">
          <div class="features-grid">
            <div class="feature-card">
              <mat-icon>confirmation_number</mat-icon>
              <h3>{{ 'onboarding.features.tickets.title' | translate }}</h3>
              <p>{{ 'onboarding.features.tickets.description' | translate }}</p>
            </div>
            <div class="feature-card">
              <mat-icon>forum</mat-icon>
              <h3>{{ 'onboarding.features.communication.title' | translate }}</h3>
              <p>{{ 'onboarding.features.communication.description' | translate }}</p>
            </div>
            <div class="feature-card">
              <mat-icon>menu_book</mat-icon>
              <h3>{{ 'onboarding.features.knowledge.title' | translate }}</h3>
              <p>{{ 'onboarding.features.knowledge.description' | translate }}</p>
            </div>
            <div class="feature-card">
              <mat-icon>analytics</mat-icon>
              <h3>{{ 'onboarding.features.analytics.title' | translate }}</h3>
              <p>{{ 'onboarding.features.analytics.description' | translate }}</p>
            </div>
          </div>
        </div>

        <!-- Page 2: Getting Started Steps -->
        <div *ngSwitchCase="1" class="page steps-page">
          <h2>{{ 'onboarding.steps.title' | translate }}</h2>
          <div class="steps-list">
            <div class="step-item" *ngFor="let step of mainSteps; let i = index"
                 [class.completed]="step.completed">
              <div class="step-number">
                <mat-icon *ngIf="step.completed">check</mat-icon>
                <span *ngIf="!step.completed">{{ i + 1 }}</span>
              </div>
              <div class="step-content">
                <h4>{{ step.title }}</h4>
                <p>{{ step.description }}</p>
                <span class="time">
                  <mat-icon>schedule</mat-icon>
                  {{ step.estimatedMinutes }} {{ 'onboarding.minutes' | translate }}
                </span>
              </div>
              <mat-icon class="step-icon">{{ step.icon }}</mat-icon>
            </div>
          </div>
        </div>

        <!-- Page 3: Ready to Start -->
        <div *ngSwitchCase="2" class="page ready-page">
          <div class="ready-content">
            <mat-icon class="ready-icon">rocket_launch</mat-icon>
            <h2>{{ 'onboarding.ready.title' | translate }}</h2>
            <p>{{ 'onboarding.ready.description' | translate }}</p>

            <div class="start-options">
              <button mat-flat-button color="primary" class="start-tour-btn"
                      (click)="startGuidedTour()">
                <mat-icon>play_arrow</mat-icon>
                {{ 'onboarding.ready.startTour' | translate }}
              </button>

              <button mat-stroked-button color="primary"
                      (click)="goToChecklist()">
                <mat-icon>checklist</mat-icon>
                {{ 'onboarding.ready.viewChecklist' | translate }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Progress dots -->
      <div class="progress-dots">
        <span *ngFor="let page of [0, 1, 2]"
              class="dot"
              [class.active]="currentPage === page"
              (click)="goToPage(page)">
        </span>
      </div>

      <!-- Footer -->
      <div class="wizard-footer">
        <button mat-button
                *ngIf="currentPage > 0"
                (click)="previousPage()">
          <mat-icon>arrow_back</mat-icon>
          {{ 'common.back' | translate }}
        </button>

        <div class="spacer"></div>

        <button mat-button color="warn" (click)="skip()">
          {{ 'onboarding.skip' | translate }}
        </button>

        <button mat-flat-button color="primary"
                *ngIf="currentPage < 2"
                (click)="nextPage()">
          {{ 'common.next' | translate }}
          <mat-icon>arrow_forward</mat-icon>
        </button>

        <button mat-flat-button color="primary"
                *ngIf="currentPage === 2"
                (click)="complete()">
          {{ 'onboarding.letsGo' | translate }}
          <mat-icon>check</mat-icon>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .welcome-wizard {
      width: 100%;
      max-width: 700px;
      padding: 32px;
    }

    .wizard-header {
      text-align: center;
      margin-bottom: 32px;

      .logo {
        width: 80px;
        height: 80px;
        border-radius: 20px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16px;

        mat-icon {
          font-size: 40px;
          width: 40px;
          height: 40px;
          color: white;
        }
      }

      h1 {
        margin: 0 0 8px;
        font-size: 28px;
        font-weight: 500;
      }

      p {
        margin: 0;
        color: rgba(0, 0, 0, 0.6);
        font-size: 16px;
      }
    }

    .wizard-content {
      min-height: 300px;
      margin-bottom: 24px;
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }

    .feature-card {
      padding: 20px;
      border-radius: 12px;
      background: #f5f5f5;
      text-align: center;

      mat-icon {
        font-size: 36px;
        width: 36px;
        height: 36px;
        color: #3f51b5;
        margin-bottom: 12px;
      }

      h3 {
        margin: 0 0 8px;
        font-size: 16px;
        font-weight: 500;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: rgba(0, 0, 0, 0.6);
      }
    }

    .steps-page {
      h2 {
        text-align: center;
        margin-bottom: 24px;
        font-size: 20px;
        font-weight: 500;
      }
    }

    .steps-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .step-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      border-radius: 12px;
      background: #f5f5f5;
      transition: all 0.2s;

      &.completed {
        background: #e8f5e9;

        .step-number {
          background: #4caf50;
        }
      }

      .step-number {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: #3f51b5;
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 500;
        flex-shrink: 0;
      }

      .step-content {
        flex: 1;

        h4 {
          margin: 0 0 4px;
          font-size: 15px;
          font-weight: 500;
        }

        p {
          margin: 0 0 4px;
          font-size: 13px;
          color: rgba(0, 0, 0, 0.6);
        }

        .time {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: rgba(0, 0, 0, 0.5);

          mat-icon {
            font-size: 14px;
            width: 14px;
            height: 14px;
          }
        }
      }

      .step-icon {
        color: rgba(0, 0, 0, 0.3);
      }
    }

    .ready-page {
      .ready-content {
        text-align: center;
        padding: 20px 0;

        .ready-icon {
          font-size: 64px;
          width: 64px;
          height: 64px;
          color: #4caf50;
          margin-bottom: 16px;
        }

        h2 {
          margin: 0 0 8px;
          font-size: 24px;
          font-weight: 500;
        }

        p {
          margin: 0 0 32px;
          color: rgba(0, 0, 0, 0.6);
        }

        .start-options {
          display: flex;
          flex-direction: column;
          gap: 12px;
          align-items: center;

          .start-tour-btn {
            padding: 12px 32px;
            font-size: 16px;
          }
        }
      }
    }

    .progress-dots {
      display: flex;
      justify-content: center;
      gap: 8px;
      margin-bottom: 24px;

      .dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: #e0e0e0;
        cursor: pointer;
        transition: all 0.2s;

        &.active {
          background: #3f51b5;
          width: 24px;
          border-radius: 5px;
        }
      }
    }

    .wizard-footer {
      display: flex;
      align-items: center;
      gap: 8px;
      padding-top: 16px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);

      .spacer {
        flex: 1;
      }
    }
  `]
})
export class WelcomeWizardComponent implements OnInit {
  @Output() completed = new EventEmitter<void>();
  @Output() startTour = new EventEmitter<string>();

  progress: OnboardingProgress | null = null;
  currentPage = 0;
  mainSteps: OnboardingStep[] = [];

  constructor(
    private onboardingService: OnboardingService,
    private dialogRef: MatDialogRef<WelcomeWizardComponent>
  ) {}

  ngOnInit(): void {
    this.onboardingService.progress$.subscribe(progress => {
      this.progress = progress;
      if (progress) {
        this.mainSteps = progress.steps.slice(0, 5);
      }
    });
  }

  nextPage(): void {
    if (this.currentPage < 2) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
  }

  skip(): void {
    this.onboardingService.skipOnboarding().subscribe(() => {
      this.dialogRef.close('skipped');
    });
  }

  complete(): void {
    this.onboardingService.dismissWelcome().subscribe(() => {
      this.dialogRef.close('completed');
    });
  }

  startGuidedTour(): void {
    const firstTourStep = this.progress?.steps.find(s => s.type === 'TOUR');
    if (firstTourStep) {
      this.onboardingService.dismissWelcome().subscribe(() => {
        this.dialogRef.close({ action: 'startTour', stepId: firstTourStep.stepId });
      });
    }
  }

  goToChecklist(): void {
    this.onboardingService.dismissWelcome().subscribe(() => {
      this.dialogRef.close('checklist');
    });
  }
}
