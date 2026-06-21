import { create } from 'zustand';
import { User, AuthResponse, UserType } from '../types';
import { authApi } from '../api/auth';
import { usersApi } from '../api/users';
import { tokenManager } from '../api/client';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isInitialized: boolean;
  // S9d-2 — jeton de défi 2fa_pending (5 min). EN MÉMOIRE UNIQUEMENT : jamais écrit dans
  // SecureStore/AsyncStorage (secret éphémère, un refresh web le perd volontairement).
  pendingTwoFactorToken: string | null;

  // Actions
  initialize: () => Promise<void>;
  login: (email: string, password: string) => Promise<AuthResponse>;
  loginTwoFactor: (twoFactorToken: string, code: string) => Promise<AuthResponse>;
  clearTwoFactorChallenge: () => void;
  register: (email: string, password: string, firstName: string, lastName: string, userType: UserType) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  setUser: (user: User) => void;
}

function authResponseToUser(res: AuthResponse): User {
  return {
    id: res.userId,
    email: res.email,
    userType: res.userType as UserType,
    firstName: res.firstName,
    lastName: res.lastName,
    isVerified: res.isVerified,
    isActive: res.isActive,
    avatarUrl: res.avatarUrl,
  };
}

export const useAuthStore = create<AuthState>((set, get) => {
  /**
   * S9d-2 — UNIQUE point d'entrée en état authentifié (login normal ET succès 2FA).
   *
   * GARDE DÉFENSIVE anti-session-zombie : refuse de stocker quoi que ce soit si les
   * tokens sont absents/vides (cas historique : la réponse de défi 2FA contenait
   * accessToken="" que l'ancien login stockait aveuglément -> isAuthenticated=true
   * avec tous les /me/* en 401). Toute réponse malformée future lève une erreur
   * visible au lieu de créer une session cassée.
   */
  const finalizeSession = async (response: AuthResponse): Promise<void> => {
    if (!response.accessToken || !response.refreshToken) {
      throw new Error(
        "Réponse d'authentification invalide (tokens absents) : session refusée."
      );
    }
    await tokenManager.setTokens(response.accessToken, response.refreshToken);
    // Charger le profil complet via GET /me (AuthResponse ne contient que les
    // champs basiques — city, coverImageUrl, instagramHandle etc. en sont absents).
    let user: User;
    try {
      user = await usersApi.getCurrentUser();
    } catch {
      // Fallback sur les champs basiques de l'AuthResponse si /me échoue
      user = authResponseToUser(response);
    }
    set({ user, isAuthenticated: true, pendingTwoFactorToken: null });
  };

  return {
  user: null,
  isAuthenticated: false,
  isLoading: false,
  isInitialized: false,
  pendingTwoFactorToken: null,

  initialize: async () => {
    try {
      set({ isLoading: true });
      const hasToken = await tokenManager.isAuthenticated();
      if (hasToken) {
        const user = await usersApi.getCurrentUser();
        set({ user, isAuthenticated: true });
      }
    } catch {
      await tokenManager.clearTokens();
      set({ user: null, isAuthenticated: false });
    } finally {
      set({ isLoading: false, isInitialized: true });
    }
  },

  login: async (email, password) => {
    set({ isLoading: true });
    try {
      const response = await authApi.login({ email, password });

      // S9d-2 — défi 2FA TESTÉ AVANT TOUT STOCKAGE : la réponse de défi ne contient
      // AUCUN vrai token (accessToken=""). On garde le jeton en mémoire et on rend
      // le sentinel au caller (login.tsx navigue vers l'écran de défi).
      if (response.requiresTwoFactor) {
        if (!response.twoFactorToken) {
          throw new Error('Défi 2FA reçu sans jeton de vérification. Réessayez.');
        }
        set({ pendingTwoFactorToken: response.twoFactorToken, isLoading: false });
        return response;
      }

      await finalizeSession(response);
      set({ isLoading: false });
      return response;
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  // S9d-2 — étape 2 : transforme le défi en vraie session, par la MÊME finalisation
  // que le login normal (état final strictement identique).
  loginTwoFactor: async (twoFactorToken, code) => {
    set({ isLoading: true });
    try {
      const response = await authApi.loginTwoFactor({ twoFactorToken, code });
      await finalizeSession(response);
      set({ isLoading: false });
      return response;
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  // Abandon du défi (annuler / jeton mort) : on jette le jeton, rien n'a été stocké.
  clearTwoFactorChallenge: () => set({ pendingTwoFactorToken: null }),

  register: async (email, password, firstName, lastName, userType) => {
    set({ isLoading: true });
    try {
      const response = await authApi.preRegister({ email, password, firstName, lastName, userType });
      set({ isLoading: false });
      return response;
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  logout: async () => {
    await tokenManager.clearTokens();
    set({ user: null, isAuthenticated: false, pendingTwoFactorToken: null });
  },

  refreshUser: async () => {
    try {
      const user = await usersApi.getCurrentUser();
      set({ user, isAuthenticated: true });
    } catch {
      await get().logout();
    }
  },

  setUser: (user) => set({ user }),
  };
});
