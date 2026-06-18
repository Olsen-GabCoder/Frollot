import type { PostResponse } from './social';
import type { PortfolioResponse, CollectionResponse } from './portfolio';

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
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
  coverImageUrl?: string;
  city?: string;
  isVerified: boolean;
  bio?: string;
  specialties: string[];
  yearsExperience?: number;
  certifications?: string;
  instagramHandle?: string;
  portfolioHighlighted?: PortfolioResponse;
  statistics: CoiffeurProfileStatistics;
  portfolios: PortfolioResponse[];
  recentPosts: PostResponse[];
  badges: UserBadgeResponse[];
  isFollowedByCurrentUser?: boolean;
}

export interface UpdateCoiffeurProfileRequest {
  bio?: string;
  specialties?: string[];
  yearsExperience?: number;
  certifications?: string;
  instagramHandle?: string;
  portfolioHighlightedId?: string;
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
  name: string;
  address: string;
  city: string;
  postalCode: string;
  slug: string;
  coverPhotoUrl?: string;
  socialDescription?: string;
  socialCoverImage?: string;
  isVerified?: boolean;
  isFollowedByCurrentUser?: boolean;
  isOwner?: boolean;
  statistics: SalonSocialProfileStatistics;
}

export interface UpdateSalonSocialProfileRequest {
  socialDescription?: string;
  socialCoverImage?: string;
}

export interface ClientProfileResponse {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
  coverImageUrl?: string;
  city?: string;
  isVerified: boolean;
  bio?: string;
  statistics: ClientProfileStatistics;
  recentPosts: PostResponse[];
  collections: CollectionResponse[];
  badges: UserBadgeResponse[];
  isFollowedByCurrentUser?: boolean;
}

export interface SalonSummaryResponse {
  id: string;
  name: string;
  city: string;
  coverPhotoUrl?: string;
  isVerified: boolean;
  followersCount: number;
}

export interface SalonOwnerProfileResponse {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
  coverImageUrl?: string;
  city?: string;
  isVerified: boolean;
  bio?: string;
  statistics: SalonOwnerProfileStatistics;
  salons: SalonSummaryResponse[];
  recentPosts: PostResponse[];
  collections: CollectionResponse[];
  badges: UserBadgeResponse[];
  isFollowedByCurrentUser?: boolean;
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
