/**
 * Onboarding models
 */

export interface OnboardingProgress {
  userId: string;
  userRole: UserRole;
  status: OnboardingStatus;
  currentStep: number;
  totalSteps: number;
  completionPercentage: number;
  steps: OnboardingStep[];
  completedStepIds: Record<string, boolean>;
  welcomeDismissed: boolean;
  tourCompleted: boolean;
  checklistCompleted: boolean;
  hintsEnabled: boolean;
  startedAt?: string;
  completedAt?: string;
  achievements: Achievement[];
  nextStep?: OnboardingStep;
}

export enum OnboardingStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  SKIPPED = 'SKIPPED'
}

export enum UserRole {
  AGENT = 'AGENT',
  ADMIN = 'ADMIN',
  SUPERVISOR = 'SUPERVISOR',
  CUSTOMER = 'CUSTOMER'
}

export interface OnboardingStep {
  id: string;
  stepId: string;
  title: string;
  description: string;
  type: StepType;
  targetRole: UserRole;
  displayOrder: number;
  required: boolean;
  icon: string;
  actionRoute?: string;
  actionLabel?: string;
  videoUrl?: string;
  helpUrl?: string;
  estimatedMinutes: number;
  tourSteps?: TourStep[];
  completed: boolean;
  completedAt?: string;
}

export enum StepType {
  CHECKLIST = 'CHECKLIST',
  TOUR = 'TOUR',
  ACTION = 'ACTION',
  VIDEO = 'VIDEO',
  LINK = 'LINK',
  CUSTOM = 'CUSTOM'
}

export interface TourStep {
  elementSelector: string;
  title: string;
  content: string;
  position: 'top' | 'bottom' | 'left' | 'right' | 'bottom-left' | 'bottom-right' | 'top-left' | 'top-right';
  highlightClass?: string;
  allowInteraction?: boolean;
  nextButtonLabel?: string;
  prevButtonLabel?: string;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  earnedAt: string;
}

export interface OnboardingStats {
  totalUsers: number;
  completedCount: number;
  inProgressCount: number;
  skippedCount: number;
  averageProgress: number;
  completionRate: number;
}

export interface HelpTooltip {
  id: string;
  targetSelector: string;
  title: string;
  content: string;
  position: 'top' | 'bottom' | 'left' | 'right';
  showOnce: boolean;
  triggerEvent?: 'hover' | 'click' | 'focus';
}
