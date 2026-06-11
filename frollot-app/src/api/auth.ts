import api from './client';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ForgotPasswordResponse,
  ResetPasswordRequest,
  ResetPasswordResponse,
  ChangePasswordRequest,
  ChangePasswordResponse,
  ChangeEmailRequest,
  ChangeEmailResponse,
  ChangePhoneRequest,
  ChangePhoneResponse,
  DeleteAccountRequest,
  DeleteAccountResponse,
  SessionsListResponse,
  RevokeSessionResponse,
} from '../types';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/api/users/login', data).then((r) => r.data),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/api/users/register', data).then((r) => r.data),

  preRegister: (data: RegisterRequest) =>
    api.post<AuthResponse>('/api/users/register', data).then((r) => r.data),

  completeRegistration: (token: string) =>
    api.get<AuthResponse>(`/api/users/complete-registration?token=${token}`).then((r) => r.data),

  verifyEmail: (token: string) =>
    api.get<Record<string, string>>(`/api/users/verify-email?token=${token}`).then((r) => r.data),

  resendVerificationEmail: () =>
    api.post<Record<string, string>>('/api/users/resend-verification').then((r) => r.data),

  forgotPassword: (data: ForgotPasswordRequest) =>
    api.post<ForgotPasswordResponse>('/api/users/forgot-password', data).then((r) => r.data),

  resetPassword: (data: ResetPasswordRequest) =>
    api.post<ResetPasswordResponse>('/api/users/reset-password', data).then((r) => r.data),

  changePassword: (data: ChangePasswordRequest) =>
    api.put<ChangePasswordResponse>('/api/security/change-password', data).then((r) => r.data),

  changeEmail: (data: ChangeEmailRequest) =>
    api.put<ChangeEmailResponse>('/api/users/me/email', data).then((r) => r.data),

  // Confirme le changement d'email avec le code OTP reçu sur la NOUVELLE adresse.
  // Backend : POST /api/users/me/email/confirm (UserController, V040). Renvoie le même
  // ChangeEmailResponse (newEmail = email actif final). Le renvoi de code se fait en
  // rappelant changeEmail (le backend écrase l'ancien token) — PAS resendVerificationEmail (cassé, D1).
  confirmEmailChange: (token: string) =>
    api.post<ChangeEmailResponse>('/api/users/me/email/confirm', { token }).then((r) => r.data),

  changePhone: (data: ChangePhoneRequest) =>
    api.put<ChangePhoneResponse>('/api/security/change-phone', data).then((r) => r.data),

  deleteAccount: (data: DeleteAccountRequest) =>
    api.post<DeleteAccountResponse>('/api/security/delete-account', data).then((r) => r.data),

  getActiveSessions: () =>
    api.get<SessionsListResponse>('/api/security/sessions').then((r) => r.data),

  revokeSession: (sessionId: number) =>
    api.delete<RevokeSessionResponse>(`/api/security/sessions/${sessionId}`).then((r) => r.data),

  revokeAllOtherSessions: () =>
    api.delete<RevokeSessionResponse>('/api/security/sessions/others').then((r) => r.data),
};
