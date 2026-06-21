export enum BookingStatus {
  PENDING = 'pending',
  CONFIRMED = 'confirmed',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
  NO_SHOW = 'no_show',
}

export enum PaymentStatus {
  UNPAID = 'unpaid',
  PAID = 'paid',
  REFUNDED = 'refunded',
}

export interface BookingResponse {
  id: string;
  salonId: string;
  salonName: string;
  clientId: string;
  clientName: string;
  clientEmail: string;
  clientPhone?: string;
  staffId?: string;
  staffName?: string;
  staffAvatarUrl?: string;
  clientAvatarUrl?: string;
  serviceId: string;
  serviceName: string;
  serviceCategory: string;
  bookingDatetime: string;
  endDatetime: string;
  durationMinutes: number;
  formattedDuration: string;
  status: BookingStatus;
  statusLabel: string;
  priceFinal?: number;
  formattedPrice?: string;
  paymentStatus: PaymentStatus;
  paymentStatusLabel: string;
  paymentMethod?: string;
  notesClient?: string;
  notesSalon?: string;
  reminderSentAt?: string;
  confirmedAt?: string;
  completedAt?: string;
  cancelledAt?: string;
  createdAt?: string;
  canBeCancelled: boolean;
  canBeConfirmed: boolean;
  isPast: boolean;
}

export interface CreateBookingRequest {
  salonId: string;
  clientId: string;
  staffId?: string;
  serviceId: string;
  bookingDatetime: string;
  notesClient?: string;
}

export interface TimeSlot {
  datetime: string;
  staffId?: string;
  staffName?: string;
  available: boolean;
}

export interface AvailableSlotsResponse {
  date: string;
  salonId: string;
  serviceId: string;
  slots: TimeSlot[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface AvailableSlotsRequest {
  salonId: string;
  serviceId: string;
  staffId?: string;
  date: string;
  duration?: number;
}

export interface UpdateBookingStatusRequest {
  status: BookingStatus;
  notesSalon?: string;
}

export interface UpdateBookingPaymentRequest {
  paymentStatus: PaymentStatus;
  paymentMethod?: string;
  paidAmount?: number;
}

export interface BookingStatistics {
  salonId: string;
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  totalRevenue: number;
  averagePrice: number;
}

export interface BookingSummary {
  date: string;
  count: number;
  revenue: number;
}
