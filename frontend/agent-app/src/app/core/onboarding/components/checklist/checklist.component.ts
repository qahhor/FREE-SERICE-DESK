import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { trigger, transition, style, animate, stagger, query } from '@angular/animations';
import { Subject, takeUntil } from 'rxjs';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingProgress, OnboardingStep } from '../../models/onboarding.models';

@Component({
  selector: 'app-onboarding-checklist',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatProgressBarModule,
    MatTooltipModule,
    TranslateModule
  ],
  animations: [
    trigger('listAnimation', [
      transition('* => *', [
        query(':enter', [
          style({ opacity: 0, transform: 'translateX(-20px)' }),
          stagger(50, [
            animate('300ms ease-out', style({ opacity: 1, transform: 'translateX(0)' }))
          ])
        ], { optional: true })
      ])
    ]),
    trigger('checkAnimation', [
      transition(':enter', [
        style({ transform: 'scale(0)' }),
        animate('300ms cubic-bezier(0.175, 0.885, 0.32, 1.275)', style({ transform: 'scale(1)' }))
      ])
    ])
  ],
  template: `
    <div class="checklist-container" [class.sidebar-mode]="sidebarMode">
      <!-- Header -->
      <div class="checklist-header">
        <div class="header-content">
          <mat-icon>checklist</mat-icon>
          <div class="header-text">
            <h3>{{ 'onboarding.checklist.title' | translate }}</h3>
            <p class="subtitle">
              {{ completedCount }}/{{ totalCount }} {{ 'onboarding.checklist.completed' | translate }}
            </p>
          </div>
        </div>

        <div class="progress-indicator">
          <mat-progress-bar
            mode="determinate"
            [value]="progress?.completionPercentage || 0">
          </mat-progress-bar>
        </div>
      </div>

      <!-- Checklist sections -->
      <div class="checklist-content" [@listAnimation]="groupedSteps.length">
        <mat-accordion multi>
          <mat-expansion-panel *ngFor="let group of groupedSteps"
                               [expanded]="group.expanded"
                               class="step-group">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <span class="group-title">
                  <mat-icon>{{ group.icon }}</mat-icon>
                  {{ group.title }}
                </span>
              </mat-panel-title>
              <mat-panel-description>
                <span class="group-progress">
                  {{ group.completedCount }}/{{ group.steps.length }}
                </span>
              </mat-panel-description>
            </mat-expansion-panel-header>

            <div class="steps-list">
              <div class="step-item"
                   *ngFor="let step of group.steps; trackBy: trackByStepId"
                   [class.completed]="step.completed"
                   [class.active]="step.id === activeStepId"
                   (click)="onStepClick(step)">

                <div class="step-checkbox">
                  <mat-icon *ngIf="step.completed" @checkAnimation class="check-icon">
                    check_circle
                  </mat-icon>
                  <div *ngIf="!step.completed" class="unchecked-circle">
                    <span class="step-order">{{ step.order }}</span>
                  </div>
                </div>

                <div class="step-content">
                  <span class="step-title">{{ step.title }}</span>
                  <span class="step-description">{{ step.description }}</span>

                  <div class="step-meta" *ngIf="!step.completed">
                    <span class="time-estimate" *ngIf="step.estimatedTime">
                      <mat-icon>schedule</mat-icon>
                      {{ step.estimatedTime }}
                    </span>
                    <span class="difficulty" *ngIf="step.difficulty">
                      <mat-icon>signal_cellular_alt</mat-icon>
                      {{ step.difficulty }}
                    </span>
                  </div>
                </div>

                <div class="step-actions">
                  <button mat-icon-button
                          *ngIf="!step.completed && step.hasTour"
                          [matTooltip]="'onboarding.checklist.startTour' | translate"
                          (click)="startTour(step); $event.stopPropagation()">
                    <mat-icon>play_circle_outline</mat-icon>
                  </button>

                  <button mat-icon-button
                          *ngIf="!step.completed && step.actionUrl"
                          [matTooltip]="'onboarding.checklist.goToAction' | translate"
                          (click)="navigateToAction(step); $event.stopPropagation()">
                    <mat-icon>arrow_forward</mat-icon>
                  </button>

                  <mat-icon *ngIf="step.completed" class="completed-badge">
                    verified
                  </mat-icon>
                </div>
              </div>
            </div>
          </mat-expansion-panel>
        </mat-accordion>
      </div>

      <!-- Footer actions -->
      <div class="checklist-footer" *ngIf="!allCompleted">
        <button mat-stroked-button
                color="primary"
                (click)="continueNextStep()"
                *ngIf="nextIncompleteStep">
          <mat-icon>play_arrow</mat-icon>
          {{ 'onboarding.checklist.continue' | translate }}
        </button>

        <button mat-button
                (click)="skipAll()"
                class="skip-btn">
          {{ 'onboarding.checklist.skipAll' | translate }}
        </button>
      </div>

      <!-- Completion celebration -->
      <div class="completion-celebration" *ngIf="allCompleted">
        <mat-icon class="celebration-icon">celebration</mat-icon>
        <h4>{{ 'onboarding.checklist.allDone' | translate }}</h4>
        <p>{{ 'onboarding.checklist.congratulations' | translate }}</p>
        <button mat-flat-button color="primary" (click)="dismissChecklist()">
          {{ 'onboarding.checklist.getStarted' | translate }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .checklist-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      overflow: hidden;

      &.sidebar-mode {
        border-radius: 0;
        box-shadow: none;
        height: 100%;
        display: flex;
        flex-direction: column;
      }
    }

    .checklist-header {
      padding: 20px;
      background: linear-gradient(135deg, #1976d2, #1565c0);
      color: white;

      .header-content {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 16px;

        mat-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
          opacity: 0.9;
        }

        .header-text {
          h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 500;
          }

          .subtitle {
            margin: 4px 0 0;
            font-size: 13px;
            opacity: 0.85;
          }
        }
      }

      .progress-indicator {
        mat-progress-bar {
          height: 6px;
          border-radius: 3px;

          ::ng-deep .mdc-linear-progress__buffer-bar {
            background-color: rgba(255, 255, 255, 0.2);
          }

          ::ng-deep .mdc-linear-progress__bar-inner {
            background-color: white;
          }
        }
      }
    }

    .checklist-content {
      flex: 1;
      overflow-y: auto;
      padding: 12px;

      mat-accordion {
        display: block;
      }

      .step-group {
        margin-bottom: 8px;
        box-shadow: none;
        border: 1px solid rgba(0, 0, 0, 0.08);
        border-radius: 8px !important;

        &::before {
          display: none;
        }

        .group-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 500;

          mat-icon {
            font-size: 20px;
            width: 20px;
            height: 20px;
            color: #1976d2;
          }
        }

        .group-progress {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.5);
          background: rgba(0, 0, 0, 0.04);
          padding: 2px 8px;
          border-radius: 10px;
        }
      }

      .steps-list {
        .step-item {
          display: flex;
          align-items: flex-start;
          gap: 12px;
          padding: 12px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s ease;

          &:hover {
            background: rgba(0, 0, 0, 0.04);
          }

          &.completed {
            opacity: 0.7;

            .step-title {
              text-decoration: line-through;
              color: rgba(0, 0, 0, 0.5);
            }
          }

          &.active {
            background: rgba(25, 118, 210, 0.08);
            border-left: 3px solid #1976d2;
            margin-left: -3px;
          }

          .step-checkbox {
            flex-shrink: 0;
            width: 28px;
            height: 28px;
            display: flex;
            align-items: center;
            justify-content: center;

            .check-icon {
              color: #4caf50;
              font-size: 28px;
            }

            .unchecked-circle {
              width: 24px;
              height: 24px;
              border: 2px solid rgba(0, 0, 0, 0.2);
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;

              .step-order {
                font-size: 11px;
                font-weight: 600;
                color: rgba(0, 0, 0, 0.4);
              }
            }
          }

          .step-content {
            flex: 1;
            min-width: 0;

            .step-title {
              display: block;
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 2px;
            }

            .step-description {
              display: block;
              font-size: 12px;
              color: rgba(0, 0, 0, 0.6);
              line-height: 1.4;
            }

            .step-meta {
              display: flex;
              gap: 12px;
              margin-top: 8px;

              span {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 11px;
                color: rgba(0, 0, 0, 0.5);

                mat-icon {
                  font-size: 14px;
                  width: 14px;
                  height: 14px;
                }
              }
            }
          }

          .step-actions {
            display: flex;
            gap: 4px;

            button {
              width: 32px;
              height: 32px;
              line-height: 32px;

              mat-icon {
                font-size: 20px;
                color: #1976d2;
              }
            }

            .completed-badge {
              color: #4caf50;
              font-size: 20px;
            }
          }
        }
      }
    }

    .checklist-footer {
      padding: 16px 20px;
      border-top: 1px solid rgba(0, 0, 0, 0.08);
      display: flex;
      justify-content: space-between;
      align-items: center;

      .skip-btn {
        color: rgba(0, 0, 0, 0.5);
      }
    }

    .completion-celebration {
      padding: 40px 20px;
      text-align: center;

      .celebration-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #ffc107;
        animation: bounce 0.5s ease;
      }

      h4 {
        margin: 16px 0 8px;
        font-size: 20px;
      }

      p {
        margin: 0 0 20px;
        color: rgba(0, 0, 0, 0.6);
      }
    }

    @keyframes bounce {
      0%, 100% { transform: translateY(0); }
      50% { transform: translateY(-10px); }
    }
  `]
})
export class ChecklistComponent implements OnInit, OnDestroy {
  @Input() sidebarMode = false;
  @Output() stepClicked = new EventEmitter<OnboardingStep>();
  @Output() tourRequested = new EventEmitter<string>();
  @Output() actionRequested = new EventEmitter<string>();
  @Output() dismissed = new EventEmitter<void>();

  private destroy$ = new Subject<void>();

  progress: OnboardingProgress | null = null;
  groupedSteps: StepGroup[] = [];
  activeStepId: string | null = null;

  constructor(private onboardingService: OnboardingService) {}

  ngOnInit(): void {
    this.onboardingService.progress$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(progress => {
      this.progress = progress;
      if (progress) {
        this.groupSteps(progress.steps);
        this.updateActiveStep(progress.steps);
      }
    });

    this.onboardingService.loadProgress().subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get completedCount(): number {
    return this.progress?.steps.filter(s => s.completed).length ?? 0;
  }

  get totalCount(): number {
    return this.progress?.steps.length ?? 0;
  }

  get allCompleted(): boolean {
    return this.progress?.onboardingCompleted ?? false;
  }

  get nextIncompleteStep(): OnboardingStep | undefined {
    return this.progress?.steps.find(s => !s.completed);
  }

  onStepClick(step: OnboardingStep): void {
    this.activeStepId = step.id;
    this.stepClicked.emit(step);
  }

  startTour(step: OnboardingStep): void {
    this.tourRequested.emit(step.id);
  }

  navigateToAction(step: OnboardingStep): void {
    if (step.actionUrl) {
      this.actionRequested.emit(step.actionUrl);
    }
  }

  continueNextStep(): void {
    const next = this.nextIncompleteStep;
    if (next) {
      if (next.hasTour) {
        this.startTour(next);
      } else if (next.actionUrl) {
        this.navigateToAction(next);
      }
    }
  }

  skipAll(): void {
    this.onboardingService.skipOnboarding().subscribe();
  }

  dismissChecklist(): void {
    this.dismissed.emit();
  }

  trackByStepId(index: number, step: OnboardingStep): string {
    return step.id;
  }

  private groupSteps(steps: OnboardingStep[]): void {
    const groups: Map<string, StepGroup> = new Map();

    steps.forEach(step => {
      const category = step.category || 'general';
      if (!groups.has(category)) {
        groups.set(category, {
          id: category,
          title: this.getCategoryTitle(category),
          icon: this.getCategoryIcon(category),
          steps: [],
          completedCount: 0,
          expanded: false
        });
      }
      const group = groups.get(category)!;
      group.steps.push(step);
      if (step.completed) {
        group.completedCount++;
      }
    });

    this.groupedSteps = Array.from(groups.values());

    // Expand the group with incomplete steps
    this.groupedSteps.forEach(group => {
      group.expanded = group.completedCount < group.steps.length;
    });
  }

  private updateActiveStep(steps: OnboardingStep[]): void {
    const next = steps.find(s => !s.completed);
    this.activeStepId = next?.id ?? null;
  }

  private getCategoryTitle(category: string): string {
    const titles: Record<string, string> = {
      'getting-started': 'Getting Started',
      'tickets': 'Ticket Management',
      'communication': 'Communication',
      'settings': 'Settings & Profile',
      'advanced': 'Advanced Features',
      'general': 'General'
    };
    return titles[category] || category;
  }

  private getCategoryIcon(category: string): string {
    const icons: Record<string, string> = {
      'getting-started': 'flag',
      'tickets': 'confirmation_number',
      'communication': 'chat',
      'settings': 'settings',
      'advanced': 'star',
      'general': 'list'
    };
    return icons[category] || 'folder';
  }
}

interface StepGroup {
  id: string;
  title: string;
  icon: string;
  steps: OnboardingStep[];
  completedCount: number;
  expanded: boolean;
}
