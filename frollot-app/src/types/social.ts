export enum PostType {
  GENERAL = 'GENERAL',
  AVANT_APRES = 'AVANT_APRES',
  PORTFOLIO = 'PORTFOLIO',
  TENDANCE = 'TENDANCE',
  CONSEIL = 'CONSEIL',
  REALISATION = 'REALISATION',
  INSPIRATION = 'INSPIRATION',
}

export enum PostVisibility {
  PUBLIC = 'PUBLIC',
  FOLLOWERS = 'FOLLOWERS',
  PRIVATE = 'PRIVATE',
}

export enum PostMediaType {
  BEFORE = 'before',
  AFTER = 'after',
  PROCESS = 'process',
  DETAIL = 'detail',
}

export enum SortBy {
  RECENT = 'RECENT',
  POPULAR = 'POPULAR',
}

export enum TrendPeriod {
  LAST_24H = 'LAST_24H',
  LAST_7D = 'LAST_7D',
  LAST_30D = 'LAST_30D',
}

export enum TaggedType {
  SALON = 'salon',
  USER = 'user',
}

export enum FollowingType {
  USER = 'USER',
  SALON = 'SALON',
  COIFFEUR = 'COIFFEUR',
}

export enum HairHashtagCategory {
  TECHNIQUE = 'TECHNIQUE',
  STYLE = 'STYLE',
  COULEUR = 'COULEUR',
  LONGUEUR = 'LONGUEUR',
  TEXTURE = 'TEXTURE',
}

// B27 : noms alignés sur le JSON réellement renvoyé par le backend (vérifié par appel live
// sur GET /api/social/posts/trending). Les anciens noms (isLiked, isFavorite, isShared,
// authorType, userReaction, salonId, salonName, isArchived) n'existaient pas dans le JSON.
export interface PostResponse {
  id: string;
  authorId: string;
  authorName: string;
  authorEmail?: string;
  authorAvatarUrl?: string;
  authorUserType?: string;
  content: string;
  imageUrl?: string;
  postType: PostType;
  visibility: PostVisibility;
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  isLikedByCurrentUser?: boolean;
  isFavoritedByCurrentUser?: boolean;
  isSharedByCurrentUser?: boolean;
  isPinned?: boolean;
  reactions?: Record<string, number>;
  media?: PostMedia[];
  tags?: TagResponse[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PostMedia {
  id: string;
  mediaUrl: string;
  mediaType: PostMediaType;
  caption?: string;
  orderIndex: number;
}

export interface CreatePostRequest {
  authorId: string;
  content: string;
  imageUrl?: string;
  postType: PostType;
  visibility: PostVisibility;
  tags?: CreateTagRequest[];
  serviceIds?: string[];
  media?: CreatePostMediaRequest[];
}

export interface CreatePostMediaRequest {
  mediaUrl: string;
  mediaType: PostMediaType;
  caption?: string;
  orderIndex: number;
}

export interface CreateTagRequest {
  taggedType: TaggedType;
  taggedId: string;
}

export interface TagResponse {
  id: string;
  postId: string;
  taggedType: TaggedType;
  taggedId: string;
}

export interface CommentResponse {
  id: string;
  postId: string;
  authorId: string;
  authorName: string;
  authorAvatarUrl?: string;
  content: string;
  createdAt?: string;
}

export interface CreateCommentRequest {
  postId: string;
  authorId: string;
  content: string;
}

export interface FollowResponse {
  id: string;
  followerId: string;
  followingType: FollowingType;
  followingId: string;
  createdAt?: string;
}

export interface PostShareResponse {
  id: string;
  postId: string;
  sharerId: string;
  sharerName: string;
  content?: string;
  createdAt?: string;
}

export interface HairHashtagResponse {
  id: string;
  name: string;
  category?: string;
  usageCount: number;
}
