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

export const SERVICE_CATEGORY_META: Record<ServiceCategory, { emoji: string; label: string }> = {
  [ServiceCategory.COUPE]: { emoji: '\u2702\uFE0F', label: 'Coupe & Taille' },
  [ServiceCategory.COLORATION]: { emoji: '\uD83C\uDFA8', label: 'Coloration' },
  [ServiceCategory.SOIN]: { emoji: '\uD83D\uDC86', label: 'Soins' },
  [ServiceCategory.COIFFAGE]: { emoji: '\uD83D\uDC87', label: 'Coiffage' },
  [ServiceCategory.BARBE]: { emoji: '\uD83E\uDDD4', label: 'Barbier' },
  [ServiceCategory.TECHNIQUE]: { emoji: '\uD83C\uDF1F', label: 'Techniques Speciales' },
  [ServiceCategory.AUTRE]: { emoji: '\uD83D\uDCCB', label: 'Autres Prestations' },
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
