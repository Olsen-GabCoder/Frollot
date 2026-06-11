import api from './client';
import {
  Salon,
  CreateSalonRequest,
  PageResponse,
  SalonService,
  CreateServiceRequest,
  UpdateServiceRequest,
  StaffMember,
  CreateStaffRequest,
  StaffStatistics,
} from '../types';

export const salonsApi = {
  // Salon CRUD
  getSalons: (params?: { query?: string; city?: string; category?: string }) =>
    api.get<PageResponse<Salon>>('/api/salons', { params }).then((r) => r.data.content),

  getAllSalons: () =>
    api.get<Salon[]>('/api/salons/all').then((r) => r.data),

  getSalonById: (salonId: string) =>
    api.get<Salon>(`/api/salons/${salonId}`).then((r) => r.data),

  getSalonsByOwner: (ownerId: string) =>
    api.get<Salon[]>(`/api/salons/owner/${ownerId}`).then((r) => r.data),

  createSalon: (data: CreateSalonRequest) =>
    api.post<Salon>('/api/salons', data).then((r) => r.data),

  updateSalonCoverPhoto: (salonId: string, coverPhotoUrl: string) =>
    api.put<Salon>(`/api/salons/${salonId}/cover-photo`, { coverPhotoUrl }).then((r) => r.data),

  getTrendingSalons: (limit?: number) =>
    api.get<Salon[]>('/api/salons/trending', { params: { limit } }).then((r) => r.data),

  getSalonsNearby: (latitude: number, longitude: number, radiusKm?: number) =>
    api.get<Salon[]>('/api/salons/nearby', { params: { latitude, longitude, radiusKm } }).then((r) => r.data),

  // Services
  getSalonServices: (salonId: string) =>
    api.get<SalonService[]>(`/api/salons/${salonId}/services`).then((r) => r.data),

  getServiceById: (salonId: string, serviceId: string) =>
    api.get<SalonService>(`/api/salons/${salonId}/services/${serviceId}`).then((r) => r.data),

  createSalonService: (data: CreateServiceRequest) =>
    api.post<SalonService>(`/api/salons/${data.salonId}/services`, data).then((r) => r.data),

  updateSalonService: (salonId: string, serviceId: string, data: UpdateServiceRequest) =>
    api.put<SalonService>(`/api/salons/${salonId}/services/${serviceId}`, data).then((r) => r.data),

  deleteSalonService: (salonId: string, serviceId: string) =>
    api.delete(`/api/salons/${salonId}/services/${serviceId}`).then((r) => r.data),

  searchSalonServices: (salonId: string, searchTerm: string) =>
    api.get<SalonService[]>(`/api/salons/${salonId}/services/search`, { params: { searchTerm } }).then((r) => r.data),

  getServicesByCategory: (salonId: string, category: string) =>
    api.get<SalonService[]>(`/api/salons/${salonId}/services/category/${category}`).then((r) => r.data),

  getSalonServiceStatistics: (salonId: string) =>
    api.get<Record<string, unknown>>(`/api/salons/${salonId}/services/statistics`).then((r) => r.data),

  // Staff
  getSalonStaff: (salonId: string) =>
    api.get<StaffMember[]>(`/api/salons/${salonId}/staff`).then((r) => r.data),

  getActiveSalonStaff: (salonId: string) =>
    api.get<StaffMember[]>(`/api/salons/${salonId}/staff/active`).then((r) => r.data),

  getStaffById: (salonId: string, staffId: string) =>
    api.get<StaffMember>(`/api/salons/${salonId}/staff/${staffId}`).then((r) => r.data),

  getStaffBySpecialty: (salonId: string, specialty: string) =>
    api.get<StaffMember[]>(`/api/salons/${salonId}/staff/specialties/${specialty}`).then((r) => r.data),

  addStaffMember: (data: CreateStaffRequest) =>
    api.post<StaffMember>(`/api/salons/${data.salonId}/staff`, data).then((r) => r.data),

  getStaffStatistics: (salonId: string) =>
    api.get<StaffStatistics>(`/api/salons/${salonId}/staff/statistics`).then((r) => r.data),
};
