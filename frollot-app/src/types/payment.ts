import { PaymentStatus } from './booking';

export interface PaymentIntentResponse {
  paymentIntentId: string;
  clientSecret: string;
  amount: number;
  currency: string;
  status: string;
}

export interface PaymentResponse {
  id: string;
  bookingId: string;
  clientId: string;
  salonId: string;
  stripePaymentIntentId?: string;
  stripeChargeId?: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  statusLabel: string;
  paymentMethod?: string;
  paymentMethodType?: string;
  description?: string;
  failureReason?: string;
  refundedAmount: number;
  remainingRefundableAmount: number;
}

export interface CheckoutSessionRequest {
  bookingId: string;
  successUrl: string;
  cancelUrl: string;
}

export interface CheckoutSessionResponse {
  sessionId: string;
  checkoutUrl: string;
  expiresAt: number;
}

export interface PaymentSessionStatus {
  sessionId: string;
  paymentStatus: string;
  bookingId: string;
  amountTotal: number;
  currency: string;
  customerEmail?: string;
}
