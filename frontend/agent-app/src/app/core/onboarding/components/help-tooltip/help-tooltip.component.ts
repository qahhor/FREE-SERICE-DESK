import { Component, OnInit, OnDestroy, Input, HostListener, ElementRef, ViewChild, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { OverlayModule, Overlay, OverlayRef, OverlayPositionBuilder, ConnectedPosition } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { OnboardingService } from '../../services/onboarding.service';

export interface HelpContent {
  title: string;
  description: string;
  learnMoreUrl?: string;
  videoUrl?: string;
  tips?: string[];
}

@Component({
  selector: 'app-help-tooltip',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    OverlayModule,
    TranslateModule
  ],
  template: `
    <!-- Help trigger icon -->
    <button mat-icon-button
            class="help-trigger"
            [class.active]="isOpen"
            (click)="toggle()"
            *ngIf="hintsEnabled">
      <mat-icon>help_outline</mat-icon>
    </button>

    <!-- Tooltip template -->
    <ng-template #tooltipTemplate>
      <div class="help-tooltip" [class]="'position-' + position">
        <div class="tooltip-arrow"></div>

        <div class="tooltip-header">
          <mat-icon class="help-icon">lightbulb</mat-icon>
          <h4>{{ content.title }}</h4>
          <button mat-icon-button class="close-btn" (click)="close()">
            <mat-icon>close</mat-icon>
          </button>
        </div>

        <div class="tooltip-body">
          <p class="description">{{ content.description }}</p>

          <div class="tips-section" *ngIf="content.tips?.length">
            <h5>{{ 'onboarding.help.tips' | translate }}</h5>
            <ul class="tips-list">
              <li *ngFor="let tip of content.tips">
                <mat-icon>tips_and_updates</mat-icon>
                <span>{{ tip }}</span>
              </li>
            </ul>
          </div>

          <div class="video-preview" *ngIf="content.videoUrl" (click)="playVideo()">
            <mat-icon>play_circle_filled</mat-icon>
            <span>{{ 'onboarding.help.watchVideo' | translate }}</span>
          </div>
        </div>

        <div class="tooltip-footer">
          <button mat-button *ngIf="content.learnMoreUrl" (click)="openLearnMore()">
            <mat-icon>open_in_new</mat-icon>
            {{ 'onboarding.help.learnMore' | translate }}
          </button>
          <button mat-button color="primary" (click)="gotIt()">
            {{ 'onboarding.help.gotIt' | translate }}
          </button>
        </div>
      </div>
    </ng-template>
  `,
  styles: [`
    :host {
      display: inline-block;
    }

    .help-trigger {
      width: 28px;
      height: 28px;
      line-height: 28px;
      opacity: 0.6;
      transition: opacity 0.2s ease;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      &:hover, &.active {
        opacity: 1;
      }

      &.active {
        color: #1976d2;
      }
    }

    .help-tooltip {
      background: white;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      width: 320px;
      max-width: 90vw;
      animation: tooltipSlideIn 0.2s ease;

      .tooltip-arrow {
        position: absolute;
        width: 0;
        height: 0;
        border: 8px solid transparent;
      }

      &.position-bottom .tooltip-arrow {
        top: -16px;
        left: 50%;
        transform: translateX(-50%);
        border-bottom-color: white;
      }

      &.position-top .tooltip-arrow {
        bottom: -16px;
        left: 50%;
        transform: translateX(-50%);
        border-top-color: white;
      }

      &.position-left .tooltip-arrow {
        right: -16px;
        top: 50%;
        transform: translateY(-50%);
        border-left-color: white;
      }

      &.position-right .tooltip-arrow {
        left: -16px;
        top: 50%;
        transform: translateY(-50%);
        border-right-color: white;
      }

      .tooltip-header {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 16px;
        border-bottom: 1px solid rgba(0, 0, 0, 0.08);
        background: linear-gradient(135deg, #fff9c4, #fff59d);
        border-radius: 12px 12px 0 0;

        .help-icon {
          color: #f9a825;
        }

        h4 {
          flex: 1;
          margin: 0;
          font-size: 14px;
          font-weight: 600;
        }

        .close-btn {
          width: 28px;
          height: 28px;
          line-height: 28px;
          margin: -4px -8px -4px 0;
        }
      }

      .tooltip-body {
        padding: 16px;

        .description {
          margin: 0 0 16px;
          color: rgba(0, 0, 0, 0.7);
          line-height: 1.6;
          font-size: 14px;
        }

        .tips-section {
          margin-bottom: 16px;

          h5 {
            margin: 0 0 8px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            color: rgba(0, 0, 0, 0.5);
          }

          .tips-list {
            list-style: none;
            margin: 0;
            padding: 0;

            li {
              display: flex;
              align-items: flex-start;
              gap: 8px;
              padding: 6px 0;

              mat-icon {
                font-size: 16px;
                width: 16px;
                height: 16px;
                color: #f9a825;
                flex-shrink: 0;
                margin-top: 2px;
              }

              span {
                font-size: 13px;
                line-height: 1.4;
              }
            }
          }
        }

        .video-preview {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px;
          background: rgba(25, 118, 210, 0.08);
          border-radius: 8px;
          cursor: pointer;
          transition: background 0.2s ease;

          &:hover {
            background: rgba(25, 118, 210, 0.12);
          }

          mat-icon {
            color: #1976d2;
            font-size: 32px;
            width: 32px;
            height: 32px;
          }

          span {
            font-size: 14px;
            font-weight: 500;
            color: #1976d2;
          }
        }
      }

      .tooltip-footer {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        padding: 8px 16px 12px;
        border-top: 1px solid rgba(0, 0, 0, 0.08);
      }
    }

    @keyframes tooltipSlideIn {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `]
})
export class HelpTooltipComponent implements OnInit, OnDestroy {
  @Input() content!: HelpContent;
  @Input() position: 'top' | 'bottom' | 'left' | 'right' = 'bottom';
  @Input() hintId?: string;

  @ViewChild('tooltipTemplate') tooltipTemplate!: TemplateRef<any>;

  private destroy$ = new Subject<void>();
  private overlayRef: OverlayRef | null = null;

  isOpen = false;
  hintsEnabled = true;

  constructor(
    private elementRef: ElementRef,
    private overlay: Overlay,
    private overlayPositionBuilder: OverlayPositionBuilder,
    private onboardingService: OnboardingService
  ) {}

  ngOnInit(): void {
    this.onboardingService.progress$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(progress => {
      this.hintsEnabled = progress?.hintsEnabled ?? true;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.close();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isOpen && !this.elementRef.nativeElement.contains(event.target)) {
      this.close();
    }
  }

  @HostListener('document:keydown.escape')
  onEscapePress(): void {
    if (this.isOpen) {
      this.close();
    }
  }

  toggle(): void {
    if (this.isOpen) {
      this.close();
    } else {
      this.open();
    }
  }

  open(): void {
    if (this.isOpen) return;

    const positionStrategy = this.overlayPositionBuilder
      .flexibleConnectedTo(this.elementRef)
      .withPositions(this.getPositions());

    this.overlayRef = this.overlay.create({
      positionStrategy,
      hasBackdrop: false,
      scrollStrategy: this.overlay.scrollStrategies.reposition()
    });

    const portal = new TemplatePortal(this.tooltipTemplate, null as any);
    this.overlayRef.attach(portal);
    this.isOpen = true;
  }

  close(): void {
    if (this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    this.isOpen = false;
  }

  gotIt(): void {
    this.close();
    if (this.hintId) {
      // Could track that hint was viewed
    }
  }

  openLearnMore(): void {
    if (this.content.learnMoreUrl) {
      window.open(this.content.learnMoreUrl, '_blank');
    }
  }

  playVideo(): void {
    if (this.content.videoUrl) {
      // Could open video modal
      window.open(this.content.videoUrl, '_blank');
    }
  }

  private getPositions(): ConnectedPosition[] {
    const positions: Record<string, ConnectedPosition[]> = {
      bottom: [
        { originX: 'center', originY: 'bottom', overlayX: 'center', overlayY: 'top', offsetY: 12 },
        { originX: 'center', originY: 'top', overlayX: 'center', overlayY: 'bottom', offsetY: -12 }
      ],
      top: [
        { originX: 'center', originY: 'top', overlayX: 'center', overlayY: 'bottom', offsetY: -12 },
        { originX: 'center', originY: 'bottom', overlayX: 'center', overlayY: 'top', offsetY: 12 }
      ],
      left: [
        { originX: 'start', originY: 'center', overlayX: 'end', overlayY: 'center', offsetX: -12 },
        { originX: 'end', originY: 'center', overlayX: 'start', overlayY: 'center', offsetX: 12 }
      ],
      right: [
        { originX: 'end', originY: 'center', overlayX: 'start', overlayY: 'center', offsetX: 12 },
        { originX: 'start', originY: 'center', overlayX: 'end', overlayY: 'center', offsetX: -12 }
      ]
    };

    return positions[this.position];
  }
}
