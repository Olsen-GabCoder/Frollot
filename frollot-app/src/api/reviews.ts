import api from './client';
import { Review, CreateReviewRequest, CreateSalonReviewRequest, SalonReviewStats, PageResponse } from '../types';

export const reviewsApi = {
  createReview: (data: CreateReviewRequest) =>
    api.post<Review>('/api/reviews', data).then((r) => r.data),

  getSalonReviews: (salonId: string, page = 0, size = 20) =>
    api.get<PageResponse<Review>>(`/api/salons/${salonId}/reviews`, { params: { page, size } }).then((r) => r.data),

  getAllSalonReviews: (salonId: string) =>
    api.get<Review[]>(`/api/salons/${salonId}/reviews/all`).then((r) => r.data),

  getReviewById: (reviewId: string) =>
    api.get<Review>(`/api/reviews/${reviewId}`).then((r) => r.data),

  getClientReviews: (clientId: string) =>
    api.get<Review[]>(`/api/clients/${clientId}/reviews`).then((r) => r.data),

  hasReviewForBooking: (bookingId: string) =>
    api.get<boolean>(`/api/bookings/${bookingId}/review/exists`).then((r) => r.data),

  getSalonReviewStats: (salonId: string) =>
    api.get<SalonReviewStats>(`/api/salons/${salonId}/reviews/stats`).then((r) => r.data),

  replyToReview: (salonId: string, reviewId: string, reply: string) =>
    api.put<Review>(`/api/salons/${salonId}/reviews/${reviewId}/reply`, { reply }).then((r) => r.data),

  createSalonReview: (salonId: string, data: CreateSalonReviewRequest) =>
    api.post<Review>(`/api/salons/${salonId}/reviews`, data).then((r) => r.data),
};
