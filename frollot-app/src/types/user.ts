export enum UserType {
  CLIENT = 'client',
  HAIRSTYLIST = 'hairstylist',
  SALON_OWNER = 'salon_owner',
  ADMIN = 'admin',
}

export enum VerificationType {
  EMAIL = 'EMAIL',
  PHONE = 'PHONE',
  BUSINESS = 'BUSINESS',
  PROFESSIONAL = 'PROFESSIONAL',
}

export interface User {
  id: string;
  email: string;
  userType: UserType;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  isVerified: boolean;
  isActive: boolean;
  preferredLanguage?: string;
  createdAt?: string;
  updatedAt?: string;
  avatarUrl?: string;
  coverImageUrl?: string;
  verificationType?: VerificationType;
  isFollowedByCurrentUser?: boolean;
  followersCount?: number;
  bio?: string;
  yearsExperience?: number;
  certifications?: string;
  instagramHandle?: string;
  specialties?: string[];
  emailVerified?: boolean;
  portfolioHighlighted?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  userType: string;
  firstName?: string;
  lastName?: string;
  isVerified: boolean;
  isActive: boolean;
  avatarUrl?: string;
  message?: string;
  emailSendStatus?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  userType: UserType;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
  emailSendStatus?: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordResponse {
  message: string;
}

export interface ChangeEmailRequest {
  newEmail: string;
  password: string;
}

export interface ChangeEmailResponse {
  success: boolean;
  message: string;
  newEmail?: string;
}

export interface ChangePhoneRequest {
  newPhone?: string;
  password: string;
}

export interface ChangePhoneResponse {
  message: string;
}

export interface DeleteAccountRequest {
  password: string;
  confirmDeletion: boolean;
}

export interface DeleteAccountResponse {
  message: string;
}

export interface SessionInfo {
  id: number;
  createdAt: string;
  expiresAt: string;
  isCurrent: boolean;
  deviceName?: string;
  deviceType?: string;
  ipAddress?: string;
  location?: string;
  lastUsedAt?: string;
  isActive: boolean;
  browser?: string;
  operatingSystem?: string;
}

export interface SessionsListResponse {
  sessions: SessionInfo[];
  totalCount: number;
  currentSessionId?: number;
}

export interface RevokeSessionResponse {
  message: string;
}
