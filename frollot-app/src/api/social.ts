import api from './client';
import {
  PostResponse,
  CreatePostRequest,
  CommentResponse,
  CreateCommentRequest,
  TagResponse,
  CreateTagRequest,
  PostShareResponse,
  HairHashtagResponse,
  FollowResponse,
  PageResponse,
  SearchResponse,
  SortBy,
  TrendPeriod,
} from '../types';

export const socialApi = {
  // Feed
  getFeed: (page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>('/api/social/feed', { params: { page, size } }).then((r) => r.data),

  getPostsBySalon: (salonId: string, params?: { postType?: string; serviceId?: string; sortBy?: SortBy; page?: number; size?: number }) =>
    api.get<PageResponse<PostResponse>>(`/api/social/salons/${salonId}/posts`, { params }).then((r) => r.data),

  getTrendingPosts: (period?: TrendPeriod, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>('/api/social/posts/trending', { params: { period, page, size } }).then((r) => r.data),

  getPostById: (postId: string) =>
    api.get<PostResponse>(`/api/social/posts/${postId}`).then((r) => r.data),

  createPost: (data: CreatePostRequest) =>
    api.post<PostResponse>('/api/social/posts', data).then((r) => r.data),

  deletePost: (postId: string) =>
    api.delete(`/api/social/posts/${postId}`).then((r) => r.data),

  getPostsNearby: (latitude: number, longitude: number, radiusKm?: number, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>('/api/social/posts/nearby', { params: { latitude, longitude, radiusKm, page, size } }).then((r) => r.data),

  // Interactions
  toggleLike: (postId: string) =>
    api.post<PostResponse>(`/api/social/posts/${postId}/like`).then((r) => r.data),

  isPostLiked: (postId: string) =>
    api.get<boolean>(`/api/social/posts/${postId}/liked`).then((r) => r.data),

  toggleFavorite: (postId: string) =>
    api.post<PostResponse>(`/api/social/posts/${postId}/favorite`).then((r) => r.data),

  // Backend réel : GET /users/{userId}/favorites (SocialController.kt, owner-only : 403 si userId ≠ utilisateur authentifié)
  // — l'ancien GET /posts/favorites/{userId} n'existe pas (404 permanent, B28)
  getFavoritesByUser: (userId: string, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>(`/api/social/users/${userId}/favorites`, { params: { page, size } }).then((r) => r.data),

  // DORMANT depuis B31 (plus aucun appelant UI — le bouton partage fait du partage externe
  // natif). Réservées au futur REPARTAGE-PROFIL. Body corrigé : le backend attend
  // { sharedContent } (SharePostRequest, SocialDto.kt) — l'ancien { content } perdait
  // silencieusement le commentaire. NB : sharePost REJETTE si déjà partagé (pas un toggle).
  sharePost: (postId: string, sharedContent?: string) =>
    api.post<PostResponse>(`/api/social/posts/${postId}/share`, { sharedContent }).then((r) => r.data),

  unsharePost: (postId: string) =>
    api.delete<PostResponse>(`/api/social/posts/${postId}/share`).then((r) => r.data),

  isPostShared: (postId: string) =>
    api.get<boolean>(`/api/social/posts/${postId}/shared`).then((r) => r.data),

  getSharesByPost: (postId: string, page = 0, size = 20) =>
    api.get<PageResponse<PostShareResponse>>(`/api/social/posts/${postId}/shares`, { params: { page, size } }).then((r) => r.data),

  // Reactions

  // Comments
  createComment: (postId: string, data: CreateCommentRequest) =>
    api.post<CommentResponse>(`/api/social/posts/${postId}/comments`, data).then((r) => r.data),

  getCommentsByPost: (postId: string, page = 0, size = 20) =>
    api.get<PageResponse<CommentResponse>>(`/api/social/posts/${postId}/comments`, { params: { page, size } }).then((r) => r.data),

  deleteComment: (commentId: string) =>
    api.delete(`/api/social/comments/${commentId}`).then((r) => r.data),

  // Tags
  addTag: (postId: string, data: CreateTagRequest) =>
    api.post<TagResponse>(`/api/social/posts/${postId}/tags`, data).then((r) => r.data),

  removeTag: (postId: string, tagId: string) =>
    api.delete(`/api/social/posts/${postId}/tags/${tagId}`).then((r) => r.data),

  getTagsByPost: (postId: string) =>
    api.get<TagResponse[]>(`/api/social/posts/${postId}/tags`).then((r) => r.data),

  // Hashtags
  getTrendingHashtags: (limit = 20) =>
    api.get<HairHashtagResponse[]>('/api/social/hashtags/trending', { params: { limit } }).then((r) => r.data),

  searchHashtags: (query: string) =>
    api.get<HairHashtagResponse[]>('/api/social/hashtags/search', { params: { q: query } }).then((r) => r.data),

  suggestHashtags: (prefix: string, limit = 10) =>
    api.get<HairHashtagResponse[]>('/api/social/hashtags/suggest', { params: { prefix, limit } }).then((r) => r.data),

  getPostsByHashtag: (hashtagName: string, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>(`/api/social/hashtags/${hashtagName}/posts`, { params: { page, size } }).then((r) => r.data),

  getHashtagsByCategory: (category: string) =>
    api.get<HairHashtagResponse[]>(`/api/social/hashtags/category/${category}`).then((r) => r.data),

  // Follow — routes réelles du backend (FollowController.kt, base /api/social)
  followSalon: (salonId: string) =>
    api.post<FollowResponse>(`/api/social/salons/${salonId}/follow`).then((r) => r.data),

  unfollowSalon: (salonId: string) =>
    api.delete(`/api/social/salons/${salonId}/follow`).then((r) => r.data),

  followCoiffeur: (coiffeurId: string) =>
    api.post<FollowResponse>(`/api/social/coiffeurs/${coiffeurId}/follow`).then((r) => r.data),

  unfollowCoiffeur: (coiffeurId: string) =>
    api.delete(`/api/social/coiffeurs/${coiffeurId}/follow`).then((r) => r.data),

  followUser: (userId: string) =>
    api.post<FollowResponse>(`/api/social/users/${userId}/follow`).then((r) => r.data),

  unfollowUser: (userId: string) =>
    api.delete(`/api/social/users/${userId}/follow`).then((r) => r.data),

  getFollowing: (userId: string) =>
    api.get<FollowResponse[]>(`/api/social/users/${userId}/following`).then((r) => r.data),

  getSalonFollowers: (salonId: string) =>
    api.get<any[]>(`/api/social/salons/${salonId}/followers`).then((r) => r.data),

  getCoiffeurFollowers: (coiffeurId: string) =>
    api.get<any[]>(`/api/social/coiffeurs/${coiffeurId}/followers`).then((r) => r.data),

  getFollowingFeed: (page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>('/api/social/feed/following', { params: { page, size } }).then((r) => r.data),

  // Archive
  archivePost: (postId: string) =>
    api.post<PostResponse>(`/api/social/posts/${postId}/archive`).then((r) => r.data),

  // Backend réel : DELETE /posts/{id}/archive (SocialController.kt) — l'ancien POST /unarchive n'existe pas
  unarchivePost: (postId: string) =>
    api.delete<PostResponse>(`/api/social/posts/${postId}/archive`).then((r) => r.data),

  // Backend réel : GET /users/{userId}/archives (owner-only, 403 sinon)
  getArchivedPosts: (userId: string, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>(`/api/social/users/${userId}/archives`, { params: { page, size } }).then((r) => r.data),

  // Pin (B29)
  // Backend réel : POST /posts/{id}/pin (ownership 403 ; limite 3 posts épinglés —
  // IllegalStateException mappée 401 par le handler du contrôleur, message backend à afficher)
  pinPost: (postId: string) =>
    api.post<PostResponse>(`/api/social/posts/${postId}/pin`).then((r) => r.data),

  // Backend réel : DELETE /posts/{id}/pin — l'ancien POST /unpin n'existe pas (B29)
  unpinPost: (postId: string) =>
    api.delete<PostResponse>(`/api/social/posts/${postId}/pin`).then((r) => r.data),

  // Backend réel : GET /users/{authorId}/pinned-posts — l'ancien /posts/pinned/{id} n'existe pas (B29)
  getPinnedPosts: (authorId: string) =>
    api.get<PostResponse[]>(`/api/social/users/${authorId}/pinned-posts`).then((r) => r.data),

  // Search
  searchPostsByContent: (query: string, page = 0, size = 20) =>
    api.get<PageResponse<PostResponse>>('/api/social/posts/search', { params: { query, page, size } }).then((r) => r.data),

  searchPostsWithFilters: (params: Record<string, unknown>) =>
    api.get<PageResponse<PostResponse>>('/api/social/posts/search/advanced', { params }).then((r) => r.data),

  unifiedSearch: (params: Record<string, unknown>) =>
    api.get<SearchResponse>('/api/social/search', { params }).then((r) => r.data),
};
