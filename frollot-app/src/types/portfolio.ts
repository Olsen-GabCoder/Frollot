import { PostResponse } from './social';

export interface PortfolioResponse {
  id: string;
  ownerId: string;
  ownerType: string;
  title: string;
  description?: string;
  coverImageUrl?: string;
  isPublic: boolean;
  postsCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreatePortfolioRequest {
  ownerId: string;
  ownerType: string;
  title: string;
  description?: string;
  coverImageUrl?: string;
  isPublic: boolean;
}

export interface UpdatePortfolioRequest {
  title?: string;
  description?: string;
  coverImageUrl?: string;
  isPublic?: boolean;
}

export interface PortfolioPostResponse {
  id: string;
  portfolioId: string;
  postId: string;
  orderIndex: number;
  addedAt?: string;
}

// Phase F.1 - Collections Thematiques

export enum CollectionCategory {
  INSPIRATION = 'INSPIRATION',
  PORTFOLIO = 'PORTFOLIO',
  TENDANCE = 'TENDANCE',
  PERSONNEL = 'PERSONNEL',
}

export interface CollectionResponse {
  id: string;
  userId: string;
  name: string;
  description?: string;
  coverImageUrl?: string;
  isPublic: boolean;
  category: CollectionCategory;
  postsCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCollectionRequest {
  name: string;
  description?: string;
  coverImageUrl?: string;
  isPublic?: boolean;
  category?: CollectionCategory;
}

export interface UpdateCollectionRequest {
  name?: string;
  description?: string;
  coverImageUrl?: string;
  isPublic?: boolean;
  category?: CollectionCategory;
}

// B26 : le backend embarque le post complet (SocialDto.CollectionPostResponse), pas un postId.
export interface CollectionPostResponse {
  id: string;
  collectionId: string;
  post: PostResponse;
  orderIndex: number;
  addedAt?: string;
}
