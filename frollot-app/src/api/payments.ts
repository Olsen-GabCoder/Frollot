import api from './client';
import {
  PaymentIntentResponse,
  PaymentResponse,
  CheckoutSessionRequest,
  CheckoutSessionResponse,
  PaymentSessionStatus,
} from '../types';

export const paymentsApi = {
  createPaymentIntent: (bookingId: string) =>
    api.post<PaymentIntentResponse>(`/api/payments/intent/${bookingId}`).then((r) => r.data),

  confirmPayment: (paymentIntentId: string) =>
    api.post<PaymentResponse>(`/api/payments/confirm/${paymentIntentId}`).then((r) => r.data),

  getPayment: (paymentId: string) =>
    api.get<PaymentResponse>(`/api/payments/${paymentId}`).then((r) => r.data),

  createCheckoutSession: (data: CheckoutSessionRequest) =>
    api.post<CheckoutSessionResponse>('/api/payments/checkout', data).then((r) => r.data),

  getCheckoutSessionStatus: (sessionId: string) =>
    api.get<PaymentSessionStatus>(`/api/payments/checkout/${sessionId}/status`).then((r) => r.data),

  getPaymentsByBooking: (bookingId: string) =>
    api.get<PaymentResponse[]>(`/api/payments/booking/${bookingId}`).then((r) => r.data),

  getPaymentsByClient: (clientId: string) =>
    api.get<PaymentResponse[]>(`/api/payments/client/${clientId}`).then((r) => r.data),

  getPaymentsBySalon: (salonId: string) =>
    api.get<PaymentResponse[]>(`/api/payments/salon/${salonId}`).then((r) => r.data),
};
