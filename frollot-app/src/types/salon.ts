import { VerificationType } from './user';

export interface Salon {
  id: string;
  name: string;
  address: string;
  city: string;
  postalCode: string;
  description?: string;
  slug: string;
  ownerId: string;
  createdAt?: string;
  coverPhotoUrl?: string;
  latitude?: number;
  longitude?: number;
  isVerified: boolean;
  verificationType?: VerificationType;
  phoneNumber?: string;
  email?: string;
  websiteUrl?: string;
  isFollowedByCurrentUser?: boolean;
  followersCount?: number;
  // Salon.kt: ratingAverage (BigDecimal) & totalReviews (Int) — colonnes DB,
  // pas encore exposées dans SalonResponse DTO mais anticipées par l'écran Home.
  averageRating?: number;
  reviewCount?: number;
}

export interface CreateSalonRequest {
  id: string;
  name: string;
  address: string;
  city: string;
  postalCode: string;
  description?: string;
  ownerId: string;
  createdAt: string;
  coverPhotoUrl?: string;
  latitude?: number;
  longitude?: number;
}

export interface UpdateSalonRequest {
  name: string;
  address: string;
  city: string;
  postalCode: string;
  description?: string;
  phoneNumber?: string;
  email?: string;
  websiteUrl?: string;
}

export enum ServiceCategory {
  COUPE = 'COUPE',
  COLORATION = 'COLORATION',
  SOIN = 'SOIN',
  COIFFAGE = 'COIFFAGE',
  BARBE = 'BARBE',
  TECHNIQUE = 'TECHNIQUE',
  AUTRE = 'AUTRE',
}

import type { ComponentProps } from 'react';
import type { MaterialCommunityIcons } from '@expo/vector-icons';

type MCIcon = ComponentProps<typeof MaterialCommunityIcons>['name'];

export const SERVICE_CATEGORY_META: Record<ServiceCategory, { icon: MCIcon; labelKey: string }> = {
  [ServiceCategory.COUPE]: { icon: 'content-cut', labelKey: 'service.categories.coupe' },
  [ServiceCategory.COLORATION]: { icon: 'palette', labelKey: 'service.categories.coloration' },
  [ServiceCategory.SOIN]: { icon: 'spa-outline', labelKey: 'service.categories.soin' },
  [ServiceCategory.COIFFAGE]: { icon: 'hair-dryer', labelKey: 'service.categories.coiffage' },
  [ServiceCategory.BARBE]: { icon: 'face-man-shimmer', labelKey: 'service.categories.barbe' },
  [ServiceCategory.TECHNIQUE]: { icon: 'creation', labelKey: 'service.categories.technique' },
  [ServiceCategory.AUTRE]: { icon: 'clipboard-text-outline', labelKey: 'service.categories.autre' },
};

export interface SalonService {
  id: string;
  salonId: string;
  name: string;
  description?: string;
  durationMinutes: number;
  formattedDuration: string;
  price: number;
  category: ServiceCategory;
  categoryLabel: string;
  categoryEmoji: string;
  imageUrls: string[];
}

export interface CreateServiceRequest {
  salonId: string;
  name: string;
  description?: string;
  durationMinutes: number;
  price: string;
  category: ServiceCategory;
  imageUrls?: string[];
}

export interface UpdateServiceRequest {
  name?: string;
  description?: string;
  durationMinutes?: number;
  price?: string;
  category?: ServiceCategory;
  imageUrls?: string[];
}

export interface StaffMember {
  id: string;
  salonId: string;
  salonName: string;
  userId: string;
  userFirstName: string;
  userLastName: string;
  userEmail: string;
  userAvatarUrl?: string;
  role: string;
  specialties: ServiceCategory[];
  specialtyLabels: string[];
  isActive: boolean;
  createdAt?: string;
}

export interface CreateStaffRequest {
  salonId: string;
  userId: string;
  specialties: ServiceCategory[];
  isActive: boolean;
}

export interface UpdateStaffRequest {
  specialties?: ServiceCategory[];
  isActive?: boolean;
  role?: string;
}

// Staff invitations
export enum Invitability {
  INVITABLE = 'INVITABLE',
  ALREADY_MEMBER_ELSEWHERE = 'ALREADY_MEMBER_ELSEWHERE',
  ALREADY_INVITED = 'ALREADY_INVITED',
  ALREADY_IN_THIS_SALON = 'ALREADY_IN_THIS_SALON',
}

export interface InvitableStylist {
  id: string;
  firstName: string | null;
  lastName: string | null;
  avatarUrl: string | null;
  email: string | null;
  invitability: Invitability;
}

export interface InvitationResponse {
  id: string;
  salonId: string;
  salonName: string;
  salonCoverUrl: string | null;
  invitedUserId: string | null;
  invitedUserName: string | null;
  invitedUserAvatar: string | null;
  invitedEmail: string | null;
  role: string;
  specialties: ServiceCategory[];
  status: string;
  expiresAt: string;
  createdAt: string | null;
}

export interface StaffStatistics {
  totalStaff: number;
  activeStaff: number;
  inactiveStaff: number;
  specialtyDistribution: Record<string, number>;
}

// Permissions — GET /api/salons/{salonId}/my-permissions
export interface MyPermissionsResponse {
  role: string;
  permissions: string[];
}
