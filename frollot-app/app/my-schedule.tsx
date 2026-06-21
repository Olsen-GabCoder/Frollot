import { useEffect, useState, useCallback, useMemo } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  RefreshControl,
  I18nManager,
  ActivityIndicator,
  TextInput,
  Modal,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons, MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { bookingsApi } from '../src/api/bookings';
import { BookingResponse, BookingStatus } from '../src/types';
import { Avatar } from '../src/components/common';
import { useMyStaffMemberships, StaffMembership } from '../src/hooks/useMyStaffMemberships';

type FilterKey = 'all' | 'upcoming' | 'pending' | 'confirmed' | 'completed';

const STATUS_COLOR_MAP: Record<string, (c: any) => { bg: string; fg: string }> = {
  [BookingStatus.PENDING]: (c) => ({ bg: c.warningContainer, fg: c.onWarningContainer }),
  [BookingStatus.CONFIRMED]: (c) => ({ bg: c.infoContainer, fg: c.onInfoContainer }),
  [BookingStatus.IN_PROGRESS]: (c) => ({ bg: c.primaryContainer, fg: c.onPrimaryContainer }),
  [BookingStatus.COMPLETED]: (c) => ({ bg: c.successContainer, fg: c.onSuccessContainer }),
  [BookingStatus.CANCELLED]: (c) => ({ bg: c.errorContainer, fg: c.onErrorContainer }),
  [BookingStatus.NO_SHOW]: (c) => ({ bg: c.errorContainer, fg: c.onErrorContainer }),
};

function fmtDay(iso: string, t: any): string {
  const d = new Date(iso);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  const diff = (target.getTime() - today.getTime()) / 86400000;
  if (diff === 0) return t('mySchedule.today');
  if (diff === 1) return t('mySchedule.tomorrow');
  if (diff === -1) return t('mySchedule.yesterday');
  return d.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' });
}

function fmtTime(iso: string): string {
  return new Date(iso).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

export default function MyScheduleScreen() {
  const { t } = useTranslation();
  const { colors, elevation: elev } = useTheme();
  const user = useAuthStore((s) => s.user);

  const { memberships, isLoading: isLoadingMemberships, error: membershipError, reload: reloadMemberships } = useMyStaffMemberships();
  const [activeMembership, setActiveMembership] = useState<StaffMembership | null>(null);

  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [isLoadingBookings, setIsLoadingBookings] = useState(false);
  const [bookingsError, setBookingsError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [filter, setFilter] = useState<FilterKey>('all');
  const [processingId, setProcessingId] = useState<string | null>(null);

  // Cancel modal
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const [cancelBookingId, setCancelBookingId] = useState<string | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const [isCancelling, setIsCancelling] = useState(false);

  // Toast
  const [toast, setToast] = useState<string | null>(null);
  const showToast = useCallback((msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }, []);

  // Set active membership when loaded
  useEffect(() => {
    if (memberships.length > 0 && !activeMembership) {
      setActiveMembership(memberships[0]);
    }
  }, [memberships, activeMembership]);

  // Load bookings
  const loadBookings = useCallback(async () => {
    if (!activeMembership) return;
    setIsLoadingBookings(true);
    setBookingsError(null);
    try {
      const data = await bookingsApi.getStaffBookings(activeMembership.staffId);
      setBookings(data);
    } catch (e: any) {
      setBookingsError(e?.response?.data?.message || e?.message || 'error');
    } finally {
      setIsLoadingBookings(false);
    }
  }, [activeMembership]);

  useEffect(() => { loadBookings(); }, [loadBookings]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await reloadMemberships();
    await loadBookings();
    setRefreshing(false);
  }, [reloadMemberships, loadBookings]);

  // Filtered bookings
  const filteredBookings = useMemo(() => {
    const now = new Date().toISOString();
    switch (filter) {
      case 'upcoming':
        return bookings.filter((b) => b.bookingDatetime >= now && b.status !== BookingStatus.CANCELLED && b.status !== BookingStatus.COMPLETED);
      case 'pending':
        return bookings.filter((b) => b.status === BookingStatus.PENDING);
      case 'confirmed':
        return bookings.filter((b) => b.status === BookingStatus.CONFIRMED);
      case 'completed':
        return bookings.filter((b) => b.status === BookingStatus.COMPLETED);
      default:
        return bookings;
    }
  }, [bookings, filter]);

  // Group by day
  const groupedBookings = useMemo(() => {
    const groups: { day: string; dayLabel: string; bookings: BookingResponse[]; isPast: boolean }[] = [];
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

    const sorted = [...filteredBookings].sort((a, b) => a.bookingDatetime.localeCompare(b.bookingDatetime));
    for (const b of sorted) {
      const day = b.bookingDatetime.slice(0, 10);
      let group = groups.find((g) => g.day === day);
      if (!group) {
        group = { day, dayLabel: fmtDay(b.bookingDatetime, t), bookings: [], isPast: day < todayStr };
        groups.push(group);
      }
      group.bookings.push(b);
    }
    return groups;
  }, [filteredBookings, t]);

  // Actions
  const handleConfirm = useCallback(async (bookingId: string) => {
    setProcessingId(bookingId);
    try {
      await bookingsApi.updateBookingStatus(bookingId, { status: BookingStatus.CONFIRMED });
      setBookings((prev) => prev.map((b) => b.id === bookingId ? { ...b, status: BookingStatus.CONFIRMED } : b));
      showToast(t('mySchedule.actions.confirmSuccess'));
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setProcessingId(null);
    }
  }, [t, showToast]);

  const handleComplete = useCallback(async (bookingId: string) => {
    setProcessingId(bookingId);
    try {
      await bookingsApi.updateBookingStatus(bookingId, { status: BookingStatus.COMPLETED });
      setBookings((prev) => prev.map((b) => b.id === bookingId ? { ...b, status: BookingStatus.COMPLETED } : b));
      showToast(t('mySchedule.actions.completeSuccess'));
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setProcessingId(null);
    }
  }, [t, showToast]);

  const openCancelModal = useCallback((bookingId: string) => {
    setCancelBookingId(bookingId);
    setCancelReason('');
    setCancelModalVisible(true);
  }, []);

  const handleCancel = useCallback(async () => {
    if (!cancelBookingId) return;
    setIsCancelling(true);
    try {
      await bookingsApi.updateBookingStatus(cancelBookingId, {
        status: BookingStatus.CANCELLED,
        ...(cancelReason.trim() ? { notesSalon: cancelReason.trim() } : {}),
      });
      setBookings((prev) => prev.map((b) => b.id === cancelBookingId ? { ...b, status: BookingStatus.CANCELLED } : b));
      setCancelModalVisible(false);
      showToast(t('mySchedule.actions.cancelSuccess'));
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setIsCancelling(false);
    }
  }, [cancelBookingId, cancelReason, t, showToast]);

  // Filter pills
  const filters: { key: FilterKey; labelKey: string }[] = [
    { key: 'all', labelKey: 'mySchedule.filters.all' },
    { key: 'upcoming', labelKey: 'mySchedule.filters.upcoming' },
    { key: 'pending', labelKey: 'mySchedule.filters.pending' },
    { key: 'confirmed', labelKey: 'mySchedule.filters.confirmed' },
    { key: 'completed', labelKey: 'mySchedule.filters.completed' },
  ];

  // Loading state
  if (isLoadingMemberships) {
    return (
      <View style={[s.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  // No membership
  if (!isLoadingMemberships && memberships.length === 0) {
    return (
      <View style={[s.container, { backgroundColor: colors.background }]}>
        <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
          <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
            <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('mySchedule.title')}</Text>
        </View>
        <View style={s.emptyCenter}>
          <MaterialCommunityIcons name="calendar-blank-outline" size={56} color={colors.onSurfaceVariant} />
          <Text style={[s.emptyTitle, { color: colors.onSurface }]}>{t('mySchedule.noMembership')}</Text>
          <Text style={[s.emptyHint, { color: colors.onSurfaceVariant }]}>{t('mySchedule.noMembershipHint')}</Text>
        </View>
      </View>
    );
  }

  const pendingCount = bookings.filter((b) => b.status === BookingStatus.PENDING).length;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('mySchedule.title')}</Text>
        {pendingCount > 0 && (
          <View style={[s.headerBadge, { backgroundColor: colors.error }]}>
            <Text style={[s.headerBadgeText, { color: colors.onError }]}>{pendingCount}</Text>
          </View>
        )}
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
        contentContainerStyle={s.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Salon banner */}
        {activeMembership && (
          <View style={[s.salonBanner, { backgroundColor: colors.surface }, elev[1]]}>
            <View style={[s.salonIconCircle, { backgroundColor: colors.primaryContainer }]}>
              <MaterialCommunityIcons name="store" size={20} color={colors.onPrimaryContainer} />
            </View>
            <View style={{ flex: 1, marginStart: 12 }}>
              <Text style={[s.salonName, { color: colors.onSurface }]} numberOfLines={1}>{activeMembership.salonName}</Text>
              <Text style={[s.salonRole, { color: colors.onSurfaceVariant }]}>{t('mySchedule.memberRole')}</Text>
            </View>
          </View>
        )}

        {/* Filter pills */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={s.filterRow}>
          {filters.map((f) => {
            const active = filter === f.key;
            return (
              <TouchableOpacity
                key={f.key}
                style={[s.filterPill, active ? { backgroundColor: colors.primary } : { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={() => setFilter(f.key)}
              >
                <Text style={[s.filterText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>{t(f.labelKey)}</Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>

        {/* Loading */}
        {isLoadingBookings && (
          <ActivityIndicator size="large" color={colors.primary} style={{ marginVertical: 32 }} />
        )}

        {/* Error */}
        {bookingsError && !isLoadingBookings && (
          <View style={[s.section, { backgroundColor: colors.errorContainer }]}>
            <Text style={[s.errorText, { color: colors.onErrorContainer }]}>{bookingsError}</Text>
            <TouchableOpacity onPress={loadBookings}>
              <Text style={[s.retryText, { color: colors.primary }]}>{t('common.actions.retry')}</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Empty */}
        {!isLoadingBookings && !bookingsError && filteredBookings.length === 0 && (
          <View style={s.emptyCenter}>
            <MaterialCommunityIcons name="calendar-check-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[s.emptyTitle, { color: colors.onSurfaceVariant }]}>{t('mySchedule.empty')}</Text>
          </View>
        )}

        {/* Grouped bookings */}
        {!isLoadingBookings && groupedBookings.map((group) => (
          <View key={group.day} style={{ marginBottom: 8 }}>
            {/* Day header */}
            <View style={s.dayHeader}>
              <Text style={[s.dayLabel, { color: group.isPast ? colors.onSurfaceVariant : colors.onSurface }]}>
                {group.dayLabel}
              </Text>
              <Text style={[s.dayCount, { color: colors.onSurfaceVariant }]}>
                {group.bookings.length} {t('mySchedule.appointmentCount', { count: group.bookings.length })}
              </Text>
            </View>

            {/* Cards */}
            {group.bookings.map((booking) => {
              const statusColors = (STATUS_COLOR_MAP[booking.status] || STATUS_COLOR_MAP[BookingStatus.PENDING])(colors);
              const isPending = booking.status === BookingStatus.PENDING;
              const isConfirmed = booking.status === BookingStatus.CONFIRMED;
              const canCancel = booking.status !== BookingStatus.CANCELLED && booking.status !== BookingStatus.COMPLETED && booking.status !== BookingStatus.NO_SHOW;
              const isProcessing = processingId === booking.id;

              return (
                <View
                  key={booking.id}
                  style={[
                    s.bookingCard,
                    { backgroundColor: colors.surface, borderColor: colors.outlineVariant },
                    elev[1],
                    group.isPast && { opacity: 0.7 },
                  ]}
                >
                  {/* Top row: avatar + info + status badge */}
                  <View style={s.cardTopRow}>
                    <Avatar
                      initials={(booking.clientName || '??').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()}
                      size={42}
                      tone="secondary"
                      imageUrl={booking.clientAvatarUrl}
                    />
                    <View style={{ flex: 1, marginStart: 12 }}>
                      <Text style={[s.clientName, { color: colors.onSurface }]} numberOfLines={1}>{booking.clientName}</Text>
                      <Text style={[s.serviceName, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{booking.serviceName}</Text>
                    </View>
                    <View style={[s.statusBadge, { backgroundColor: statusColors.bg }]}>
                      <Text style={[s.statusText, { color: statusColors.fg }]}>
                        {t(`booking.status.${booking.status === BookingStatus.IN_PROGRESS ? 'inProgress' : booking.status === BookingStatus.NO_SHOW ? 'noShow' : booking.status}`)}
                      </Text>
                    </View>
                  </View>

                  {/* Meta row */}
                  <View style={s.metaRow}>
                    <View style={s.metaItem}>
                      <MaterialCommunityIcons name="clock-outline" size={14} color={colors.onSurfaceVariant} />
                      <Text style={[s.metaText, { color: colors.onSurfaceVariant }]}>
                        {fmtTime(booking.bookingDatetime)} - {fmtTime(booking.endDatetime)}
                      </Text>
                    </View>
                    <View style={s.metaItem}>
                      <MaterialCommunityIcons name="timer-outline" size={14} color={colors.onSurfaceVariant} />
                      <Text style={[s.metaText, { color: colors.onSurfaceVariant }]}>{booking.formattedDuration}</Text>
                    </View>
                    {booking.formattedPrice && (
                      <View style={s.metaItem}>
                        <MaterialCommunityIcons name="cash" size={14} color={colors.onSurfaceVariant} />
                        <Text style={[s.metaText, { color: colors.onSurface, fontFamily: 'Manrope-SemiBold', fontWeight: '600' }]}>{booking.formattedPrice}</Text>
                      </View>
                    )}
                  </View>

                  {/* Client notes */}
                  {booking.notesClient && (
                    <View style={[s.notesRow, { backgroundColor: colors.surfaceContainerHigh }]}>
                      <MaterialCommunityIcons name="message-text-outline" size={14} color={colors.onSurfaceVariant} />
                      <Text style={[s.notesText, { color: colors.onSurfaceVariant }]} numberOfLines={2}>{booking.notesClient}</Text>
                    </View>
                  )}

                  {/* Actions (only for actionable statuses, not past) */}
                  {!group.isPast && (isPending || isConfirmed || canCancel) && (
                    <View style={s.actionsRow}>
                      {isPending && (
                        <TouchableOpacity
                          style={[s.actionBtnFill, { backgroundColor: colors.primary }]}
                          onPress={() => handleConfirm(booking.id)}
                          disabled={isProcessing}
                        >
                          {isProcessing ? (
                            <ActivityIndicator size="small" color={colors.onPrimary} />
                          ) : (
                            <Text style={[s.actionBtnText, { color: colors.onPrimary }]}>{t('mySchedule.actions.confirm')}</Text>
                          )}
                        </TouchableOpacity>
                      )}
                      {isConfirmed && (
                        <TouchableOpacity
                          style={[s.actionBtnFill, { backgroundColor: colors.success }]}
                          onPress={() => handleComplete(booking.id)}
                          disabled={isProcessing}
                        >
                          {isProcessing ? (
                            <ActivityIndicator size="small" color={colors.onSuccess} />
                          ) : (
                            <Text style={[s.actionBtnText, { color: colors.onSuccess }]}>{t('mySchedule.actions.complete')}</Text>
                          )}
                        </TouchableOpacity>
                      )}
                      {canCancel && (
                        <TouchableOpacity
                          style={[s.actionBtnOutline, { borderColor: colors.error }]}
                          onPress={() => openCancelModal(booking.id)}
                          disabled={isProcessing}
                        >
                          <Text style={[s.actionBtnText, { color: colors.error }]}>{t('mySchedule.actions.cancel')}</Text>
                        </TouchableOpacity>
                      )}
                    </View>
                  )}
                </View>
              );
            })}
          </View>
        ))}

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* Cancel modal */}
      <Modal visible={cancelModalVisible} transparent animationType="fade" onRequestClose={() => setCancelModalVisible(false)}>
        <View style={s.modalOverlay}>
          <View style={[s.modalContent, { backgroundColor: colors.surface }, elev[3]]}>
            <Text style={[s.modalTitle, { color: colors.onSurface }]}>{t('mySchedule.actions.cancelTitle')}</Text>
            <TextInput
              style={[s.modalInput, { color: colors.onSurface, borderColor: colors.outlineVariant, backgroundColor: colors.surfaceContainerHigh }]}
              placeholder={t('mySchedule.actions.cancelReason')}
              placeholderTextColor={colors.onSurfaceVariant}
              value={cancelReason}
              onChangeText={setCancelReason}
              multiline
              numberOfLines={3}
              textAlignVertical="top"
            />
            <View style={s.modalActions}>
              <TouchableOpacity style={[s.modalBtn, { borderColor: colors.outlineVariant, borderWidth: 1 }]} onPress={() => setCancelModalVisible(false)}>
                <Text style={[s.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.back')}</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[s.modalBtn, { backgroundColor: colors.error }]} onPress={handleCancel} disabled={isCancelling}>
                {isCancelling ? (
                  <ActivityIndicator size="small" color={colors.onError} />
                ) : (
                  <Text style={[s.modalBtnText, { color: colors.onError }]}>{t('mySchedule.actions.cancelConfirm')}</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* Toast */}
      {toast && (
        <View style={[s.toast, { backgroundColor: colors.inverseSurface }]}>
          <Text style={[s.toastText, { color: colors.inverseOnSurface }]}>{toast}</Text>
        </View>
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingTop: 52, paddingBottom: 14, gap: 12 },
  headerTitle: { fontFamily: 'Cormorant-Bold', fontSize: 24, fontWeight: '700', flex: 1 },
  headerBadge: { minWidth: 22, height: 22, borderRadius: 11, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 6 },
  headerBadgeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  scrollContent: { paddingBottom: 100 },

  salonBanner: { flexDirection: 'row', alignItems: 'center', marginHorizontal: 16, marginTop: 12, padding: 14, borderRadius: 16 },
  salonIconCircle: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  salonName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  salonRole: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },

  filterRow: { paddingHorizontal: 16, paddingVertical: 14, gap: 8 },
  filterPill: { paddingHorizontal: 16, paddingVertical: 8, borderRadius: 999 },
  filterText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },

  section: { marginHorizontal: 16, padding: 16, borderRadius: 16, marginBottom: 12 },

  dayHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingTop: 12, paddingBottom: 6 },
  dayLabel: { fontFamily: 'Cormorant-SemiBold', fontSize: 18, fontWeight: '600', textTransform: 'capitalize' },
  dayCount: { fontFamily: 'Manrope-Regular', fontSize: 12 },

  bookingCard: { marginHorizontal: 16, marginBottom: 10, padding: 14, borderRadius: 14, borderWidth: 1 },
  cardTopRow: { flexDirection: 'row', alignItems: 'center' },
  clientName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  serviceName: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2 },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  statusText: { fontFamily: 'Manrope-SemiBold', fontSize: 11, fontWeight: '600' },

  metaRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 14, marginTop: 10, paddingTop: 10, borderTopWidth: 0.5, borderTopColor: 'rgba(0,0,0,0.06)' },
  metaItem: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  metaText: { fontFamily: 'Manrope-Regular', fontSize: 13 },

  notesRow: { flexDirection: 'row', alignItems: 'flex-start', gap: 6, marginTop: 8, padding: 10, borderRadius: 10 },
  notesText: { fontFamily: 'Manrope-Regular', fontSize: 12, flex: 1, lineHeight: 18 },

  actionsRow: { flexDirection: 'row', gap: 8, marginTop: 12 },
  actionBtnFill: { flex: 1, paddingVertical: 10, borderRadius: 999, alignItems: 'center' },
  actionBtnOutline: { flex: 1, paddingVertical: 10, borderRadius: 999, alignItems: 'center', borderWidth: 1.5 },
  actionBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700' },

  emptyCenter: { alignItems: 'center', justifyContent: 'center', paddingVertical: 64, gap: 12 },
  emptyTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600', textAlign: 'center' },
  emptyHint: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', paddingHorizontal: 32 },

  errorText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
  retryText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', textAlign: 'center', marginTop: 8 },

  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center', padding: 24 },
  modalContent: { width: '100%', maxWidth: 400, borderRadius: 20, padding: 24 },
  modalTitle: { fontFamily: 'Cormorant-Bold', fontSize: 20, fontWeight: '700', marginBottom: 16 },
  modalInput: { fontFamily: 'Manrope-Regular', fontSize: 14, borderRadius: 12, borderWidth: 1, padding: 12, minHeight: 80, textAlignVertical: 'top' },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 16 },
  modalBtn: { flex: 1, paddingVertical: 12, borderRadius: 999, alignItems: 'center' },
  modalBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },

  toast: { position: 'absolute', bottom: 40, left: 24, right: 24, paddingVertical: 14, paddingHorizontal: 20, borderRadius: 12, alignItems: 'center' },
  toastText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
