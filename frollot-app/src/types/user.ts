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
  // V045 — visibilité choisie du numéro. Présent sur /me (vue propriétaire) ;
  // sur les surfaces publiques, phoneNumber est ABSENT du JSON si phonePublic=false.
  phonePublic?: boolean;
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
  // S9d-2 — défi 2FA (S9b backend) : champs à la RACINE (AuthResponse plat, JsonInclude NON_NULL),
  // présents UNIQUEMENT quand le login tombe sur un compte 2FA-actif. Dans ce cas accessToken/
  // refreshToken/userId valent "" : NE JAMAIS les stocker (cause de la session zombie).
  requiresTwoFactor?: boolean;
  twoFactorToken?: string;
}

// POST /api/users/login/2fa (public, S9b) — transforme le défi en vrais tokens.
// twoFactorToken = jeton 2fa_pending (5 min, 5 essais max) ; code = TOTP 6 chiffres
// OU code de récupération XXXX-XXXX (verifyLoginCode gère les deux, chaîne brute).
export interface TwoFactorLoginRequest {
  twoFactorToken: string;
  code: string;
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

// B25b : champs alignés sur SecurityDto.kt (backend) — `success` manquait.
export interface ChangePasswordResponse {
  success: boolean;
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

// Aligné sur SecurityDto.kt ChangePhoneRequest (incrément 1 téléphone, prouvé curl).
// newPhone : E.164 attendu ; '' = SUPPRESSION du numéro (backend normalise en NULL).
// phonePublic : visibilité déclarée (false = visible de soi seul + canal transactionnel).
export interface ChangePhoneRequest {
  newPhone: string;
  phonePublic: boolean;
  password: string;
}

export interface ChangePhoneResponse {
  success: boolean;
  message: string;
  newPhone?: string;
  phonePublic: boolean;
}

export interface DeleteAccountRequest {
  password: string;
  confirmDeletion: boolean;
}

export interface DeleteAccountResponse {
  success: boolean;
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
  success: boolean;
  message: string;
  revokedCount: number;
}

// ===== 2FA TOTP (S9d-1) — alignés sur TwoFactorDto.kt (backend, vérifié curl S9a-S9c) =====

// GET /api/users/me/2fa/status -> TwoFactorStatusResponse (jamais le secret)
export interface TwoFactorStatusResponse {
  enabled: boolean;
}

// POST /api/users/me/2fa/setup -> TwoFactorSetupResponse
// SEULE réponse de toute l'API où le secret circule en clair (tant que enabled=false).
export interface TwoFactorSetupResponse {
  secret: string;
  otpauthUri: string;
}

// POST /api/users/me/2fa/confirm -> TwoFactorConfirmResponse
// recoveryCodes : les 10 codes EN CLAIR, montrés UNE seule fois (stockés hachés côté backend).
// Même forme pour POST /api/users/me/2fa/recovery-codes/regenerate.
export interface TwoFactorConfirmResponse {
  success: boolean;
  message: string;
  recoveryCodes: string[];
}

// DELETE /api/users/me/2fa — exige les DEUX : mot de passe ET code (TOTP courant
// OU code de récupération non utilisé). Backend : TwoFactorDisableRequest.
export interface TwoFactorDisableRequest {
  password: string;
  code: string;
}

// DELETE /api/users/me/2fa -> TwoFactorDisableResponse
export interface TwoFactorDisableResponse {
  success: boolean;
  message: string;
}

// POST /api/users/me/2fa/recovery-codes/regenerate — même niveau de preuve que la
// désactivation (password + code TOTP ou récupération). Backend : TwoFactorRegenerateRequest.
export interface TwoFactorRegenerateRequest {
  password: string;
  code: string;
}
