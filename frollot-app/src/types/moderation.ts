// Phase H.1 - Signalement de Contenu
// Phase H.3 - Moderation de Contenu

export enum ReportedEntityType {
  POST = 'POST',
  COMMENT = 'COMMENT',
  USER = 'USER',
  SALON = 'SALON',
}

export enum ReportReason {
  INAPPROPRIE = 'INAPPROPRIE',
  SPAM = 'SPAM',
  FAUX = 'FAUX',
  COPYRIGHT = 'COPYRIGHT',
  AUTRE = 'AUTRE',
}

export enum ReportStatus {
  PENDING = 'PENDING',
  REVIEWED = 'REVIEWED',
  RESOLVED = 'RESOLVED',
  DISMISSED = 'DISMISSED',
}

export enum ModerationActionType {
  HIDE = 'HIDE',
  DELETE = 'DELETE',
  WARN = 'WARN',
}

export enum AppealStatus {
  NONE = 'NONE',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

// Report DTOs
export interface CreateReportRequest {
  reportedEntityType: ReportedEntityType;
  reportedEntityId: string;
  reason: ReportReason;
  additionalInfo?: string;
}

export interface ReportResponse {
  id: string;
  reportedEntityType: ReportedEntityType;
  reportedEntityId: string;
  reporterId: string;
  reporterName?: string;
  reason: ReportReason;
  status: ReportStatus;
  additionalInfo?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface HandleReportRequest {
  status: ReportStatus;
  moderatorNote?: string;
}

// Moderation DTOs
export interface ModerateContentRequest {
  contentEntityType: ReportedEntityType;
  contentEntityId: string;
  action: ModerationActionType;
  reason?: string;
}

export interface ModerationActionResponse {
  id: string;
  contentEntityType: ReportedEntityType;
  contentEntityId: string;
  action: ModerationActionType;
  moderatorId: string;
  reason?: string;
  appealStatus: AppealStatus;
  appealReason?: string;
  appealProcessedAt?: string;
  createdAt?: string;
}

// Appeal DTOs
export interface AppealModerationRequest {
  moderationActionId: string;
  reason: string;
}

export interface HandleAppealRequest {
  status: AppealStatus;
  moderatorNote?: string;
}
