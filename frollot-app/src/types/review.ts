export interface Review {
  id: string;
  salonId: string;
  salonName: string;
  staffId?: string;
  staffName?: string;
  clientId: string;
  clientName: string;
  clientEmail: string;
  bookingId?: string;
  rating: number;
  title?: string;
  content?: string;
  responseSalon?: string;
  responseAt?: string;
  isVerified: boolean;
  isVisible: boolean;
  createdAt?: string;
}

export interface CreateReviewRequest {
  salonId: string;
  bookingId: string;
  rating: number;
  title?: string;
  content?: string;
}

export interface SalonReviewStats {
  salonId: string;
  averageRating: number;
  totalReviews: number;
  ratingDistribution: Record<string, number>;
}
