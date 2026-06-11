import api from './client';
import {
  CoiffeurProfileResponse,
  UpdateCoiffeurProfileRequest,
  SalonSocialProfileResponse,
  UpdateSalonSocialProfileRequest,
  ClientProfileResponse,
  SalonOwnerProfileResponse,
  BadgeResponse,
  UserBadgeResponse,
  AwardBadgeRequest,
} from '../types';

export const profilesApi = {
  getCoiffeurProfile: (coiffeurId: string) =>
    api.get<CoiffeurProfileResponse>(`/api/social/coiffeurs/${coiffeurId}/profile`).then((r) => r.data),

  updateCoiffeurProfile: (coiffeurId: string, data: UpdateCoiffeurProfileRequest) =>
    api.put<CoiffeurProfileResponse>(`/api/social/coiffeurs/${coiffeurId}/profile`, data).then((r) => r.data),

  getSalonSocialProfile: (salonId: string) =>
    api.get<SalonSocialProfileResponse>(`/api/social/salons/${salonId}/profile`).then((r) => r.data),

  updateSalonSocialProfile: (salonId: string, data: UpdateSalonSocialProfileRequest) =>
    api.put<SalonSocialProfileResponse>(`/api/social/salons/${salonId}/profile`, data).then((r) => r.data),

  getClientProfile: (clientId: string) =>
    api.get<ClientProfileResponse>(`/api/social/clients/${clientId}/profile`).then((r) => r.data),

  getSalonOwnerProfile: (ownerId: string) =>
    api.get<SalonOwnerProfileResponse>(`/api/social/owners/${ownerId}/profile`).then((r) => r.data),

  getAvailableBadges: (category?: string) =>
    api.get<BadgeResponse[]>('/api/badges', { params: { category } }).then((r) => r.data),

  getUserBadges: (userId: string, includeHidden = false) =>
    api.get<UserBadgeResponse[]>(`/api/badges/user/${userId}`, { params: { includeHidden } }).then((r) => r.data),

  awardBadge: (userId: string, data: AwardBadgeRequest) =>
    api.post<UserBadgeResponse>(`/api/badges/user/${userId}`, data).then((r) => r.data),

  toggleBadgeDisplay: (userId: string, badgeId: string) =>
    api.put<UserBadgeResponse>(`/api/badges/user/${userId}/${badgeId}/toggle`).then((r) => r.data),

  removeBadge: (userId: string, badgeId: string) =>
    api.delete(`/api/badges/user/${userId}/${badgeId}`).then((r) => r.data),
};
