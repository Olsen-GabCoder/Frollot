// --- Statistics sub-objects (SocialDto.kt, backend) ---
// Champs toujours présents (defaults 0 côté backend).

export interface ClientProfileStatistics {
  postsCount: number;
  totalLikes: number;
  followersCount: number;
  followingCount: number;
  collectionsCount: number;
  bookingsCount: number;
}

export interface SalonOwnerProfileStatistics {
  postsCount: number;
  totalLikes: number;
  followersCount: number;
  followingCount: number;
  salonsCount: number;
  collectionsCount: number;
}

export interface CoiffeurProfileStatistics {
  postsCount: number;
  totalLikes: number;
  followersCount: number;
  followingCount: number;
  averageRating: number;
  totalReviews: number;
}

export interface CoiffeurProfileResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  bio?: string;
  specialties: string[];
  yearsExperience?: number;
  salonId?: string;
  salonName?: string;
  isVerified: boolean;
  isFollowedByCurrentUser?: boolean;
  statistics: CoiffeurProfileStatistics;
}

export interface UpdateCoiffeurProfileRequest {
  bio?: string;
  specialties?: string[];
  yearsExperience?: number;
}

export interface SalonSocialProfileStatistics {
  postsCount: number;
  totalLikes: number;
  followersCount: number;
  averageRating: number;
  totalReviews: number;
}

export interface SalonSocialProfileResponse {
  id: string;
  salonId: string;
  name: string;
  description?: string;
  coverPhotoUrl?: string;
  city: string;
  isVerified: boolean;
  isFollowedByCurrentUser?: boolean;
  statistics: SalonSocialProfileStatistics;
}

export interface UpdateSalonSocialProfileRequest {
  description?: string;
}

export interface ClientProfileResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  memberSince?: string;
  statistics: ClientProfileStatistics;
}

export interface SalonOwnerProfileResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  isVerified: boolean;
  statistics: SalonOwnerProfileStatistics;
}

// Phase E.3 - Badges et Certifications

export enum BadgeCategory {
  CERTIFICATION = 'CERTIFICATION',
  COMPETITION = 'COMPETITION',
  FORMATION = 'FORMATION',
  PARTENARIAT = 'PARTENARIAT',
}

export interface BadgeResponse {
  id: string;
  name: string;
  description?: string;
  iconUrl?: string;
  category: BadgeCategory;
  createdAt?: string;
}

export interface UserBadgeResponse {
  id: string;
  userId: string;
  badgeId: string;
  badge?: BadgeResponse;
  awardedAt: string;
  isDisplayed: boolean;
}

export interface AwardBadgeRequest {
  badgeId: string;
}

// Search
export enum SearchType {
  ALL = 'ALL',
  POSTS = 'POSTS',
  SALONS = 'SALONS',
  USERS = 'USERS',
  HASHTAGS = 'HASHTAGS',
}

export interface SearchFilters {
  q?: string;
  type?: SearchType;
  postType?: string;
  serviceId?: string;
  salonId?: string;
  hashtagName?: string;
  authorId?: string;
  page?: number;
  size?: number;
}

export interface SearchResponse {
  salons: any[];
  posts: any[];
  users: any[];
  hashtags?: any[];
  totalResults?: number;
}
