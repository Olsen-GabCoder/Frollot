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
  followersCount: number;
  postsCount: number;
  portfoliosCount: number;
  averageRating?: number;
  totalReviews?: number;
  isFollowedByCurrentUser?: boolean;
}

export interface UpdateCoiffeurProfileRequest {
  bio?: string;
  specialties?: string[];
  yearsExperience?: number;
}

export interface SalonSocialProfileResponse {
  id: string;
  salonId: string;
  name: string;
  description?: string;
  coverPhotoUrl?: string;
  city: string;
  followersCount: number;
  postsCount: number;
  portfoliosCount: number;
  averageRating?: number;
  totalReviews?: number;
  isVerified: boolean;
  isFollowedByCurrentUser?: boolean;
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
  totalBookings: number;
  totalReviews: number;
  favoriteSalonsCount: number;
  collectionsCount: number;
}

export interface SalonOwnerProfileResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  avatarUrl?: string;
  salonsCount: number;
  totalFollowers: number;
  totalRevenue?: number;
  isVerified: boolean;
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
