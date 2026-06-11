export enum QueueEntryStatus {
  WAITING = 'WAITING',
  CALLED = 'CALLED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED',
}

export interface QueueEntryResponse {
  entryId: string;
  salonId: string;
  clientId: string;
  clientName: string;
  status: QueueEntryStatus;
  position: number;
  estimatedWaitMinutes: number;
  requestedServiceId?: string;
  requestedServiceName?: string;
  requestedDurationMinutes: number;
  joinedAt?: string;
  calledAt?: string;
  notes?: string;
}

export interface QueueStatusResponse {
  salonId: string;
  entries: QueueEntryResponse[];
  estimatedWaitForNew: number;
  lastUpdatedAt?: string;
}

export interface JoinQueueRequest {
  salonId: string;
  clientId: string;
  requestedServiceId?: string;
  notes?: string;
}

export interface LeaveQueueRequest {
  entryId: string;
}
