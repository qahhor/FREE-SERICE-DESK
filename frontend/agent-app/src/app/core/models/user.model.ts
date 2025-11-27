export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  avatarUrl?: string;
  role: UserRole;
  status: UserStatus;
  locale: string;
  timezone: string;
  lastLoginAt?: string;
  emailVerified: boolean;
  teamId?: string;
  teamName?: string;
  createdAt: string;
}

export enum UserRole {
  ADMIN = 'ADMIN',
  MANAGER = 'MANAGER',
  AGENT = 'AGENT',
  CUSTOMER = 'CUSTOMER'
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION'
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  locale?: string;
  timezone?: string;
}
