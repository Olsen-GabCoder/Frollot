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

export enum ServiceCategory {
  COUPE = 'COUPE',
  COLORATION = 'COLORATION',
  SOIN = 'SOIN',
  COIFFAGE = 'COIFFAGE',
  BARBE = 'BARBE',
  TECHNIQUE = 'TECHNIQUE',
  AUTRE = 'AUTRE',
}

export const SERVICE_CATEGORY_META: Record<ServiceCategory, { emoji: string; labelKey: string }> = {
  [ServiceCategory.COUPE]: { emoji: '✂️', labelKey: 'service.categories.coupe' },
  [ServiceCategory.COLORATION]: { emoji: '🎨', labelKey: 'service.categories.coloration' },
  [ServiceCategory.SOIN]: { emoji: '💆', labelKey: 'service.categories.soin' },
  [ServiceCategory.COIFFAGE]: { emoji: '💇', labelKey: 'service.categories.coiffage' },
  [ServiceCategory.BARBE]: { emoji: '🧔', labelKey: 'service.categories.barbe' },
  [ServiceCategory.TECHNIQUE]: { emoji: '🌟', labelKey: 'service.categories.technique' },
  [ServiceCategory.AUTRE]: { emoji: '📋', labelKey: 'service.categories.autre' },
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

export interface StaffStatistics {
  totalStaff: number;
  activeStaff: number;
  inactiveStaff: number;
  specialtyDistribution: Record<string, number>;
}
