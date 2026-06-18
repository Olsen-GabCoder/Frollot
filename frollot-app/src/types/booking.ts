export enum BookingStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED',
  CANCELED = 'CANCELED',
  PARTIALLY_REFUNDED = 'PARTIALLY_REFUNDED',
  UNPAID = 'UNPAID',
  PAID = 'PAID',
  REFUNDED = 'REFUNDED',
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
  totalBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  noShowBookings: number;
  revenue: number;
  averageRating?: number;
}

export interface BookingSummary {
  date: string;
  count: number;
  revenue: number;
}
