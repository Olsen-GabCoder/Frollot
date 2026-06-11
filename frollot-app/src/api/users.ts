import api from './client';
import { User } from '../types';

export const usersApi = {
  getCurrentUser: () =>
    api.get<User>('/api/users/me').then((r) => r.data),

  getAllUsers: () =>
    api.get<User[]>('/api/users').then((r) => r.data),

  searchUsers: (query: string) =>
    api.get<User[]>('/api/users/search', { params: { query } }).then((r) => r.data),

  getCurrentUserLanguage: () =>
    api.get<string>('/api/users/me/language').then((r) => r.data),

  updateCurrentUserLanguage: (languageCode: string) =>
    api.put<string>('/api/users/me/language', { languageCode }).then((r) => r.data),

  updateUserAvatar: (userId: string, avatarUrl: string) =>
    api.patch<User>(`/api/users/${userId}/avatar`, { avatarUrl }).then((r) => r.data),

  updateUserCoverImage: (userId: string, coverImageUrl: string) =>
    api.put<Record<string, string>>(`/api/users/${userId}/cover-image`, { coverImageUrl }).then((r) => r.data),
};
