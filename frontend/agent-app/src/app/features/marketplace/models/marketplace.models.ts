/**
 * Marketplace module models
 */

export interface MarketplaceModule {
  id: string;
  moduleId: string;
  name: string;
  description: string;
  shortDescription: string;
  category: ModuleCategory;
  author: string;
  authorUrl?: string;
  documentationUrl?: string;
  supportUrl?: string;
  repositoryUrl?: string;
  icon?: string;
  screenshots: string[];
  tags: string[];
  pricingModel: PricingModel;
  price?: number;
  priceCurrency: string;
  trialDays: number;
  latestVersion: string;
  minimumPlatformVersion?: string;
  dependencies: string[];
  installCount: number;
  averageRating: number;
  reviewCount: number;
  status: ModuleStatus;
  verified: boolean;
  featured: boolean;
  official: boolean;
  publishedAt: Date;
  createdAt: Date;
  updatedAt: Date;

  // Installation info
  installed?: boolean;
  installedVersion?: string;
  enabled?: boolean;
  updateAvailable?: boolean;
}

export enum ModuleCategory {
  INTEGRATION = 'INTEGRATION',
  ANALYTICS = 'ANALYTICS',
  AUTOMATION = 'AUTOMATION',
  COMMUNICATION = 'COMMUNICATION',
  PRODUCTIVITY = 'PRODUCTIVITY',
  CUSTOMER_EXPERIENCE = 'CUSTOMER_EXPERIENCE',
  SECURITY = 'SECURITY',
  AI_ML = 'AI_ML',
  FINANCE = 'FINANCE',
  UTILITIES = 'UTILITIES',
  THEMES = 'THEMES',
  LANGUAGES = 'LANGUAGES'
}

export const CategoryLabels: Record<ModuleCategory, string> = {
  [ModuleCategory.INTEGRATION]: 'Integrations',
  [ModuleCategory.ANALYTICS]: 'Analytics & Reports',
  [ModuleCategory.AUTOMATION]: 'Automation',
  [ModuleCategory.COMMUNICATION]: 'Communication',
  [ModuleCategory.PRODUCTIVITY]: 'Productivity',
  [ModuleCategory.CUSTOMER_EXPERIENCE]: 'Customer Experience',
  [ModuleCategory.SECURITY]: 'Security',
  [ModuleCategory.AI_ML]: 'AI & Machine Learning',
  [ModuleCategory.FINANCE]: 'Finance',
  [ModuleCategory.UTILITIES]: 'Utilities',
  [ModuleCategory.THEMES]: 'Themes & UI',
  [ModuleCategory.LANGUAGES]: 'Languages'
};

export enum PricingModel {
  FREE = 'FREE',
  ONE_TIME = 'ONE_TIME',
  SUBSCRIPTION_MONTHLY = 'SUBSCRIPTION_MONTHLY',
  SUBSCRIPTION_YEARLY = 'SUBSCRIPTION_YEARLY',
  USAGE_BASED = 'USAGE_BASED',
  CONTACT_US = 'CONTACT_US'
}

export enum ModuleStatus {
  DRAFT = 'DRAFT',
  PENDING_REVIEW = 'PENDING_REVIEW',
  PUBLISHED = 'PUBLISHED',
  SUSPENDED = 'SUSPENDED',
  DEPRECATED = 'DEPRECATED'
}

export interface ModuleInstallation {
  id: string;
  tenantId: string;
  moduleId: string;
  moduleName?: string;
  moduleIcon?: string;
  installedVersion: string;
  latestVersion?: string;
  updateAvailable: boolean;
  status: InstallationStatus;
  configuration: Record<string, any>;
  enabled: boolean;
  autoUpdate: boolean;
  licenseKey?: string;
  licenseExpiresAt?: Date;
  trialExpiresAt?: Date;
  inTrial: boolean;
  healthStatus: HealthStatus;
  errorMessage?: string;
  usageStats: Record<string, any>;
  createdAt: Date;
  updatedAt: Date;
}

export enum InstallationStatus {
  INSTALLING = 'INSTALLING',
  ACTIVE = 'ACTIVE',
  DISABLED = 'DISABLED',
  UPDATING = 'UPDATING',
  FAILED = 'FAILED',
  UNINSTALLING = 'UNINSTALLING',
  UNINSTALLED = 'UNINSTALLED'
}

export enum HealthStatus {
  HEALTHY = 'HEALTHY',
  DEGRADED = 'DEGRADED',
  UNHEALTHY = 'UNHEALTHY',
  UNKNOWN = 'UNKNOWN'
}

export interface ModuleReview {
  id: string;
  moduleId: string;
  userId: string;
  rating: number;
  title?: string;
  comment?: string;
  installedVersion?: string;
  helpfulCount: number;
  verifiedPurchase: boolean;
  authorResponse?: string;
  authorResponseAt?: Date;
  createdAt: Date;
}

export interface CategoryInfo {
  category: ModuleCategory;
  displayName: string;
  description: string;
  moduleCount: number;
}

export interface ModuleSearchRequest {
  query?: string;
  categories?: ModuleCategory[];
  tags?: string[];
  pricingModel?: PricingModel;
  freeOnly?: boolean;
  verifiedOnly?: boolean;
  officialOnly?: boolean;
  minRating?: number;
  sortBy?: 'RELEVANCE' | 'POPULARITY' | 'RATING' | 'NEWEST' | 'NAME' | 'PRICE';
  sortOrder?: 'ASC' | 'DESC';
  page?: number;
  size?: number;
}

export interface InstallModuleRequest {
  moduleId: string;
  version?: string;
  configuration?: Record<string, any>;
  licenseKey?: string;
  enableTrial?: boolean;
  autoUpdate?: boolean;
}

export interface ModuleWidget {
  id: string;
  title: string;
  description?: string;
  type: 'CHART' | 'TABLE' | 'METRIC' | 'LIST' | 'CUSTOM';
  size: 'SMALL' | 'MEDIUM' | 'LARGE' | 'FULL';
  dataEndpoint?: string;
  refreshInterval?: number;
  componentName?: string;
  defaultConfig?: Record<string, any>;
  requiredPermission?: string;
}

export interface ModuleMenuItem {
  id: string;
  label: string;
  icon?: string;
  route?: string;
  parentId?: string;
  location: 'MAIN' | 'SETTINGS' | 'USER' | 'ADMIN' | 'TOOLBAR';
  order: number;
  requiredPermission?: string;
  children?: ModuleMenuItem[];
  badge?: string;
  badgeColor?: 'PRIMARY' | 'ACCENT' | 'WARN' | 'SUCCESS';
}
