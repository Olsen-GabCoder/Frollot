import api from './client';

export const verificationApi = {
  requestVerification: (entityType: string, entityId: string, data: { documentType?: string; documentUrl?: string; businessNumber?: string }) =>
    api.post(`/api/verification/request/${entityType}/${entityId}`, data).then((r) => r.data),

  verifyUser: (userId: string, data: { verificationType: string }) =>
    api.put(`/api/verification/users/${userId}/verify`, data).then((r) => r.data),

  verifySalon: (salonId: string, data: { verificationType: string }) =>
    api.put(`/api/verification/salons/${salonId}/verify`, data).then((r) => r.data),

  revokeVerification: (entityType: string, entityId: string) =>
    api.delete(`/api/verification/${entityType}/${entityId}/revoke`).then((r) => r.data),

  requestEmailVerification: () =>
    api.post('/api/users/request-verification').then((r) => r.data),
};
