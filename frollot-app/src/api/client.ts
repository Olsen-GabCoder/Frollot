import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthResponse } from '../types';

const BASE_URL = __DEV__
  ? Platform.select({
      android: 'http://10.0.2.2:9090',
      ios: 'http://localhost:9090',
      default: 'http://localhost:9090',
    })
  : 'https://api.frollot.com';

const ACCESS_TOKEN_KEY = 'frollot_access_token';
const REFRESH_TOKEN_KEY = 'frollot_refresh_token';

// Secure storage abstraction (SecureStore for native, AsyncStorage for web)
const storage = {
  async get(key: string): Promise<string | null> {
    if (Platform.OS === 'web') {
      return AsyncStorage.getItem(key);
    }
    return SecureStore.getItemAsync(key);
  },
  async set(key: string, value: string): Promise<void> {
    if (Platform.OS === 'web') {
      await AsyncStorage.setItem(key, value);
    } else {
      await SecureStore.setItemAsync(key, value);
    }
  },
  async delete(key: string): Promise<void> {
    if (Platform.OS === 'web') {
      await AsyncStorage.removeItem(key);
    } else {
      await SecureStore.deleteItemAsync(key);
    }
  },
};

// Mutex for token refresh (prevents concurrent refreshes)
let isRefreshing = false;
let refreshSubscribers: Array<(token: string) => void> = [];

function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
}

function addRefreshSubscriber(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

// Create axios instance
const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// Request interceptor: inject Bearer token
api.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const token = await storage.get(ACCESS_TOKEN_KEY);
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: auto-refresh on 401
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    // Don't retry refresh or login requests
    const url = originalRequest.url || '';
    if (url.includes('/users/refresh') || url.includes('/users/login')) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      // Another refresh is in progress — queue this request
      return new Promise((resolve) => {
        addRefreshSubscriber((newToken: string) => {
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
          }
          originalRequest._retry = true;
          resolve(api(originalRequest));
        });
      });
    }

    isRefreshing = true;
    originalRequest._retry = true;

    try {
      const refreshToken = await storage.get(REFRESH_TOKEN_KEY);
      if (!refreshToken) {
        throw new Error('No refresh token');
      }

      const { data } = await axios.post<AuthResponse>(`${BASE_URL}/api/users/refresh`, {
        refreshToken,
      });

      await tokenManager.setTokens(data.accessToken, data.refreshToken);

      if (originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
      }

      onTokenRefreshed(data.accessToken);
      return api(originalRequest);
    } catch (refreshError) {
      await tokenManager.clearTokens();
      refreshSubscribers = [];
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

// Token management API
export const tokenManager = {
  async setTokens(accessToken: string, refreshToken: string): Promise<void> {
    await Promise.all([
      storage.set(ACCESS_TOKEN_KEY, accessToken),
      storage.set(REFRESH_TOKEN_KEY, refreshToken),
    ]);
  },

  async getAccessToken(): Promise<string | null> {
    return storage.get(ACCESS_TOKEN_KEY);
  },

  async getRefreshToken(): Promise<string | null> {
    return storage.get(REFRESH_TOKEN_KEY);
  },

  async clearTokens(): Promise<void> {
    await Promise.all([
      storage.delete(ACCESS_TOKEN_KEY),
      storage.delete(REFRESH_TOKEN_KEY),
    ]);
  },

  async isAuthenticated(): Promise<boolean> {
    const token = await storage.get(ACCESS_TOKEN_KEY);
    return token !== null;
  },
};

export { api, BASE_URL };
export default api;
