import api from './client';
import {
  ReportResponse,
  CreateReportRequest,
  HandleReportRequest,
  ModerateContentRequest,
  ModerationActionResponse,
  AppealModerationRequest,
  HandleAppealRequest,
  PageResponse,
  ReportStatus,
} from '../types';

export const moderationApi = {
  // Reports
  reportContent: (data: CreateReportRequest) =>
    api.post<ReportResponse>('/api/social/reports', data).then((r) => r.data),

  getMyReports: (page = 0, size = 20) =>
    api.get<PageResponse<ReportResponse>>('/api/social/reports/my-reports', { params: { page, size } }).then((r) => r.data),

  getReports: (page = 0, size = 20, status?: ReportStatus) =>
    api.get<PageResponse<ReportResponse>>('/api/social/reports', { params: { page, size, status } }).then((r) => r.data),

  getPendingReports: (page = 0, size = 20) =>
    api.get<PageResponse<ReportResponse>>('/api/social/reports/pending', { params: { page, size } }).then((r) => r.data),

  handleReport: (reportId: string, data: HandleReportRequest) =>
    api.put<ReportResponse>(`/api/social/moderation/reports/${reportId}/handle`, data).then((r) => r.data),

  // Moderation actions
  moderateContent: (data: ModerateContentRequest) =>
    api.post<ModerationActionResponse>('/api/social/moderation/moderate', data).then((r) => r.data),

  getModerationActions: (entityType: string, entityId: string) =>
    api.get<ModerationActionResponse[]>(`/api/social/moderation/actions/entity/${entityType}/${entityId}`).then((r) => r.data),

  // Appeals
  appealModeration: (data: AppealModerationRequest) =>
    api.post<ModerationActionResponse>('/api/social/moderation/appeal', data).then((r) => r.data),

  handleAppeal: (moderationActionId: string, data: HandleAppealRequest) =>
    api.put<ModerationActionResponse>(`/api/social/moderation/appeals/${moderationActionId}/handle`, data).then((r) => r.data),

  getPendingAppeals: (page = 0, size = 20) =>
    api.get<PageResponse<ModerationActionResponse>>('/api/social/moderation/appeals/pending', { params: { page, size } }).then((r) => r.data),
};
