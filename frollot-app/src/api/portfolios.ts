import api from './client';
import {
  PortfolioResponse,
  CreatePortfolioRequest,
  UpdatePortfolioRequest,
  PortfolioPostResponse,
  CollectionResponse,
  CollectionPostResponse,
  CreateCollectionRequest,
  UpdateCollectionRequest,
  PageResponse,
  PostResponse,
} from '../types';

export const portfoliosApi = {
  createPortfolio: (data: CreatePortfolioRequest) =>
    api.post<PortfolioResponse>('/api/portfolios', data).then((r) => r.data),

  updatePortfolio: (portfolioId: string, data: UpdatePortfolioRequest) =>
    api.put<PortfolioResponse>(`/api/portfolios/${portfolioId}`, data).then((r) => r.data),

  deletePortfolio: (portfolioId: string) =>
    api.delete(`/api/portfolios/${portfolioId}`).then((r) => r.data),

  getPortfolioById: (portfolioId: string) =>
    api.get<PortfolioResponse>(`/api/portfolios/${portfolioId}`).then((r) => r.data),

  getPortfoliosByOwner: (ownerId: string, ownerType: string, includePrivate = false) =>
    api.get<PortfolioResponse[]>(`/api/portfolios/owner/${ownerId}`, { params: { ownerType, includePrivate } }).then((r) => r.data),

  addPostToPortfolio: (portfolioId: string, postId: string) =>
    api.post<PortfolioPostResponse>(`/api/portfolios/${portfolioId}/posts/${postId}`).then((r) => r.data),

  removePostFromPortfolio: (portfolioId: string, postId: string) =>
    api.delete(`/api/portfolios/${portfolioId}/posts/${postId}`).then((r) => r.data),

  getPortfolioPosts: (portfolioId: string, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>(`/api/portfolios/${portfolioId}/posts`, { params: { page, size } }).then((r) => r.data),

  reorderPortfolioPosts: (portfolioId: string, postIds: string[]) =>
    api.put(`/api/portfolios/${portfolioId}/posts/reorder`, { postIds }).then((r) => r.data),
};

// B26 : les collections sont servies par SocialController (base /api/social) — aucun
// /api/collections n'existe côté backend. Routes vérifiées contre SocialController.kt.
export const collectionsApi = {
  createCollection: (data: CreateCollectionRequest) =>
    api.post<CollectionResponse>('/api/social/collections', data).then((r) => r.data),

  updateCollection: (collectionId: string, data: UpdateCollectionRequest) =>
    api.put<CollectionResponse>(`/api/social/collections/${collectionId}`, data).then((r) => r.data),

  deleteCollection: (collectionId: string) =>
    api.delete(`/api/social/collections/${collectionId}`).then((r) => r.data),

  getCollectionsByUser: (userId: string, includePrivate = false) =>
    api.get<CollectionResponse[]>(`/api/social/users/${userId}/collections`, { params: { includePrivate } }).then((r) => r.data),

  getCollectionById: (collectionId: string) =>
    api.get<CollectionResponse>(`/api/social/collections/${collectionId}`).then((r) => r.data),

  addPostToCollection: (collectionId: string, postId: string) =>
    api.post<CollectionPostResponse>(`/api/social/collections/${collectionId}/posts/${postId}`).then((r) => r.data),

  removePostFromCollection: (collectionId: string, postId: string) =>
    api.delete(`/api/social/collections/${collectionId}/posts/${postId}`).then((r) => r.data),

  getCollectionPosts: (collectionId: string, page = 0, size = 20) =>
    api.get<PageResponse<CollectionPostResponse>>(`/api/social/collections/${collectionId}/posts`, { params: { page, size } }).then((r) => r.data),

  getPublicCollections: (page = 0, size = 20) =>
    api.get<PageResponse<CollectionResponse>>('/api/social/collections/public', { params: { page, size } }).then((r) => r.data),
};
