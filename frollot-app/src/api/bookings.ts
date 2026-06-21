import api from './client';
import {
  BookingResponse,
  BookingSummary,
  CreateBookingRequest,
  AvailableSlotsRequest,
  AvailableSlotsResponse,
  UpdateBookingStatusRequest,
  UpdateBookingPaymentRequest,
  BookingStatistics,
  PageResponse,
} from '../types';

export const bookingsApi = {
  createBooking: (data: CreateBookingRequest) =>
    api.post<BookingResponse>('/api/bookings', data).then((r) => r.data),

  getBookingById: (bookingId: string) =>
    api.get<BookingResponse>(`/api/bookings/${bookingId}`).then((r) => r.data),

  getSalonBookings: (salonId: string) =>
    api.get<BookingResponse[]>(`/api/salons/${salonId}/bookings`).then((r) => r.data),

  getUserBookings: (userId: string) =>
    api.get<BookingResponse[]>(`/api/clients/${userId}/bookings`).then((r) => r.data),

  cancelBooking: (bookingId: string) =>
    api.delete<BookingResponse>(`/api/bookings/${bookingId}`).then((r) => r.data),

  getUpcomingBookings: (userId: string, size = 5) =>
    api.get<BookingResponse[]>(`/api/bookings/user/${userId}/upcoming`, { params: { size } }).then((r) => r.data),

  getAvailableSlots: (salonId: string, data: AvailableSlotsRequest) =>
    api.post<AvailableSlotsResponse>(`/api/salons/${salonId}/available-slots`, data).then((r) => r.data),

  updateBookingStatus: (bookingId: string, data: UpdateBookingStatusRequest) =>
    api.patch<BookingResponse>(`/api/bookings/${bookingId}/status`, data).then((r) => r.data),

  updateBookingPayment: (bookingId: string, data: UpdateBookingPaymentRequest) =>
    api.patch<BookingResponse>(`/api/bookings/${bookingId}/payment`, data).then((r) => r.data),

  getBookingStatistics: (salonId: string) =>
    api.get<BookingStatistics>(`/api/salons/${salonId}/bookings/statistics`).then((r) => r.data),

  getDailyBookings: (salonId: string, from: string, to: string) =>
    api.get<BookingSummary[]>(`/api/salons/${salonId}/bookings/daily`, { params: { from, to } }).then((r) => r.data),

  getStaffBookings: (staffId: string) =>
    api.get<BookingResponse[]>(`/api/staff/${staffId}/bookings`).then((r) => r.data),
};
