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

  // Actions
  initialize: () => Promise<void>;
  login: (email: string, password: string) => Promise<AuthResponse>;
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

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  isInitialized: false,

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
      await tokenManager.setTokens(response.accessToken, response.refreshToken);
      const user = authResponseToUser(response);
      set({ user, isAuthenticated: true, isLoading: false });
      return response;
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

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
    set({ user: null, isAuthenticated: false });
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
}));
