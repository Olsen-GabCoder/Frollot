import api, { tokenManager } from './client';
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
  TwoFactorStatusResponse,
  TwoFactorSetupResponse,
  TwoFactorConfirmResponse,
  TwoFactorDisableRequest,
  TwoFactorDisableResponse,
  TwoFactorRegenerateRequest,
  TwoFactorLoginRequest,
} from '../types';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/api/users/login', data).then((r) => r.data),

  // S9d-2 : étape 2 du login 2FA (endpoint PUBLIC, pas de Bearer — l'auth repose sur le
  // jeton 2fa_pending). Succès = AuthResponse complet, identique à un login normal.
  // Erreurs 401 : message backend dans response.data.message (« reconnecter » = jeton mort).
  loginTwoFactor: (data: TwoFactorLoginRequest) =>
    api.post<AuthResponse>('/api/users/login/2fa', data).then((r) => r.data),

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

  // B25b : les URLs /api/security/* étaient inventées (aucun SecurityController backend).
  // Endpoints réels : UserController sous /api/users/me/... (vérifiés chemin:ligne 2026-06-12).
  changePassword: (data: ChangePasswordRequest) =>
    api.put<ChangePasswordResponse>('/api/users/me/password', data).then((r) => r.data),

  changeEmail: (data: ChangeEmailRequest) =>
    api.put<ChangeEmailResponse>('/api/users/me/email', data).then((r) => r.data),

  // Confirme le changement d'email avec le code OTP reçu sur la NOUVELLE adresse.
  // Backend : POST /api/users/me/email/confirm (UserController, V040). Renvoie le même
  // ChangeEmailResponse (newEmail = email actif final). Le renvoi de code se fait en
  // rappelant changeEmail (le backend écrase l'ancien token) — PAS resendVerificationEmail (cassé, D1).
  confirmEmailChange: (token: string) =>
    api.post<ChangeEmailResponse>('/api/users/me/email/confirm', { token }).then((r) => r.data),

  // Incrément 3 téléphone (2026-06-12) : numéro DÉCLARATIF (aucune vérification OTP/SMS —
  // couche future). PUT /api/users/me/phone, corps { newPhone, phonePublic, password } ;
  // newPhone vide = suppression (backend met NULL + phonePublic=false, prouvé curl incrément 1).
  changePhone: (data: ChangePhoneRequest) =>
    api.put<ChangePhoneResponse>('/api/users/me/phone', data).then((r) => r.data),

  // DELETE /api/users/me (UserController:1034). Le backend exige confirmDeletion=true dans le corps.
  deleteAccount: (data: DeleteAccountRequest) =>
    api.delete<DeleteAccountResponse>('/api/users/me', { data }).then((r) => r.data),

  // X-Refresh-Token (UserController:783) : permet au backend de marquer la session courante
  // (isCurrent). Sans lui, aucune session n'est identifiée comme « cet appareil ».
  getActiveSessions: async () => {
    const refreshToken = await tokenManager.getRefreshToken();
    return api
      .get<SessionsListResponse>('/api/users/me/sessions', {
        headers: refreshToken ? { 'X-Refresh-Token': refreshToken } : undefined,
      })
      .then((r) => r.data);
  },

  revokeSession: (sessionId: number) =>
    api.delete<RevokeSessionResponse>(`/api/users/me/sessions/${sessionId}`).then((r) => r.data),

  // DELETE /api/users/me/sessions (UserController:842) — PAS de suffixe /others.
  // X-Refresh-Token OBLIGATOIRE ici (UserController:845) : sans lui le backend révoque TOUTES les
  // sessions, y compris la courante -> auto-déconnexion de l'utilisateur (UserController:854-857).
  revokeAllOtherSessions: async () => {
    const refreshToken = await tokenManager.getRefreshToken();
    if (!refreshToken) {
      throw new Error('Refresh token introuvable : révocation globale refusée (la session courante serait déconnectée).');
    }
    return api
      .delete<RevokeSessionResponse>('/api/users/me/sessions', {
        headers: { 'X-Refresh-Token': refreshToken },
      })
      .then((r) => r.data);
  },

  // ===== 2FA TOTP (S9d-1) — endpoints TwoFactorController, vérifiés curl S9a-S9c =====

  getTwoFactorStatus: () =>
    api.get<TwoFactorStatusResponse>('/api/users/me/2fa/status').then((r) => r.data),

  // (Re)génère un secret TOTP — la 2FA reste inactive jusqu'à confirm. Un setup non
  // confirmé est écrasé par le suivant (sémantique backend S9a) : annuler le wizard est sans danger.
  setupTwoFactor: () =>
    api.post<TwoFactorSetupResponse>('/api/users/me/2fa/setup').then((r) => r.data),

  // Active la 2FA avec un premier code TOTP valide ; retourne les 10 codes de
  // récupération EN CLAIR (une seule fois). Code faux -> 400, message backend tel quel.
  confirmTwoFactor: (code: string) =>
    api.post<TwoFactorConfirmResponse>('/api/users/me/2fa/confirm', { code }).then((r) => r.data),

  // Corps sur DELETE : axios passe par { data } (même pattern que deleteAccount, B25b).
  // Exige mot de passe + code (TOTP courant OU code de récupération non utilisé).
  disableTwoFactor: (data: TwoFactorDisableRequest) =>
    api.delete<TwoFactorDisableResponse>('/api/users/me/2fa', { data }).then((r) => r.data),

  // Invalide TOUS les anciens codes et en génère 10 nouveaux (le secret TOTP n'est pas touché).
  regenerateRecoveryCodes: (data: TwoFactorRegenerateRequest) =>
    api
      .post<TwoFactorConfirmResponse>('/api/users/me/2fa/recovery-codes/regenerate', data)
      .then((r) => r.data),
};
