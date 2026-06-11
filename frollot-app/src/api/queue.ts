import api from './client';
import { QueueStatusResponse, QueueEntryResponse, JoinQueueRequest, LeaveQueueRequest } from '../types';

export const queueApi = {
  getQueueStatus: (salonId: string) =>
    api.get<QueueStatusResponse>(`/api/salons/${salonId}/queue`).then((r) => r.data),

  joinQueue: (salonId: string, data: JoinQueueRequest) =>
    api.post<QueueEntryResponse>(`/api/salons/${salonId}/queue/join`, data).then((r) => r.data),

  leaveQueue: (salonId: string, data: LeaveQueueRequest) =>
    api.post<QueueEntryResponse>(`/api/salons/${salonId}/queue/leave`, data).then((r) => r.data),

  callNextClient: (salonId: string) =>
    api.post<QueueEntryResponse>(`/api/salons/${salonId}/queue/call-next`).then((r) => r.data),

  removeQueueEntry: (salonId: string, entryId: string) =>
    api.post<QueueEntryResponse>(`/api/salons/${salonId}/queue/leave`, { entryId }).then((r) => r.data),
};
