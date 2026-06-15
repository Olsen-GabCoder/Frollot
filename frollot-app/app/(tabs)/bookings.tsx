import { useEffect, useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl, Modal, Pressable } from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { bookingsApi } from '../../src/api/bookings';
import { LoadingState, EmptyState, ErrorState } from '../../src/components/lists';
import { BookingResponse, BookingStatus } from '../../src/types';
import { formatDateTimeShort } from '../../src/utils/formatDate';

type BookingFilter = 'all' | 'upcoming' | 'past';

export default function BookingsScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { user } = useAuthStore();
  const { colors, typography: typo } = theme;

  const STATUS_COLORS: Record<BookingStatus, string> = {
    [BookingStatus.PENDING]: colors.warning,
    [BookingStatus.CONFIRMED]: colors.success,
    [BookingStatus.IN_PROGRESS]: colors.info,
    [BookingStatus.COMPLETED]: colors.success,
    [BookingStatus.CANCELLED]: colors.error,
    [BookingStatus.NO_SHOW]: colors.onSurfaceVariant,
  };

  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [filter, setFilter] = useState<BookingFilter>('all');
  const [cancelId, setCancelId] = useState<string | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);

  const loadBookings = useCallback(async () => {
    if (!user) return;
    setHasError(false);
    try {
      const result = await bookingsApi.getUserBookings(user.id);
      setBookings(result);
    } catch {
      setHasError(true);
    } finally {
      setIsLoading(false);
      setRefreshing(false);
    }
  }, [user]);

  useEffect(() => { loadBookings(); }, [loadBookings]);

  const onRefresh = () => { setRefreshing(true); loadBookings(); };

  // Filter logic
  const filtered = bookings.filter(b => {
    if (filter === 'upcoming') return !b.isPast && b.status !== BookingStatus.CANCELLED && b.status !== BookingStatus.COMPLETED;
    if (filter === 'past') return b.isPast || b.status === BookingStatus.COMPLETED || b.status === BookingStatus.CANCELLED;
    return true;
  });

  const counts = {
    all: bookings.length,
    upcoming: bookings.filter(b => !b.isPast && b.status !== BookingStatus.CANCELLED && b.status !== BookingStatus.COMPLETED).length,
    past: bookings.filter(b => b.isPast || b.status === BookingStatus.COMPLETED || b.status === BookingStatus.CANCELLED).length,
  };

  const handleCancel = async () => {
    if (!cancelId) return;
    setIsCancelling(true);
    try {
      await bookingsApi.cancelBooking(cancelId);
      setBookings(prev => prev.map(b => b.id === cancelId ? { ...b, status: BookingStatus.CANCELLED, statusLabel: 'Annulée', canBeCancelled: false } : b));
    } catch {} finally {
      setIsCancelling(false);
      setCancelId(null);
    }
  };

  const FILTER_KEYS: { key: BookingFilter; i18nKey: string }[] = [
    { key: 'all', i18nKey: 'booking.tabFilter.all' },
    { key: 'upcoming', i18nKey: 'booking.tabFilter.upcoming' },
    { key: 'past', i18nKey: 'booking.tabFilter.past' },
  ];

  const renderBooking = ({ item }: { item: BookingResponse }) => {
    const date = new Date(item.bookingDatetime);
    const statusColor = STATUS_COLORS[item.status] || colors.onSurfaceVariant;
    const isCompleted = item.status === BookingStatus.COMPLETED;

    return (
      <TouchableOpacity
        style={[styles.card, { backgroundColor: colors.surface }]}
        onPress={() => router.push(`/booking/${item.id}`)}
      >
        <View style={styles.cardHeader}>
          <Text style={[typo.titleSmall, { color: colors.onSurface, flex: 1 }]}>{item.salonName}</Text>
          <View style={[styles.statusBadge, { backgroundColor: statusColor + '20' }]}>
            <Text style={[typo.labelSmall, { color: statusColor }]}>{item.statusLabel}</Text>
          </View>
        </View>
        <Text style={[typo.bodyMedium, { color: colors.onSurface }]}>{item.serviceName}</Text>
        <View style={styles.metaRow}>
          <MaterialIcons name="event" size={16} color={colors.onSurfaceVariant} />
          <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>
            {formatDateTimeShort(date)}
          </Text>
          {item.formattedDuration && (
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginStart: 8 }]}>
              {item.formattedDuration}
            </Text>
          )}
        </View>
        {item.formattedPrice && (
          <Text style={[typo.labelLarge, { color: colors.primary, marginTop: 4 }]}>
            {item.formattedPrice}
          </Text>
        )}

        {/* Actions */}
        <View style={styles.actionsRow}>
          {item.canBeCancelled && (
            <TouchableOpacity
              style={[styles.actionBtn, { borderColor: colors.error }]}
              onPress={() => setCancelId(item.id)}
            >
              <Text style={[styles.actionText, { color: colors.error }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
          )}
          {isCompleted && (
            <TouchableOpacity
              style={[styles.actionBtn, { borderColor: colors.tertiary }]}
              onPress={() => router.push(`/create-review?salonId=${item.salonId}&salonName=${encodeURIComponent(item.salonName)}&bookingId=${item.id}&serviceName=${encodeURIComponent(item.serviceName)}`)}
            >
              <MaterialIcons name="rate-review" size={16} color={colors.tertiary} />
              <Text style={[styles.actionText, { color: colors.tertiary }]}>{t('booking.leaveReview')}</Text>
            </TouchableOpacity>
          )}
        </View>
      </TouchableOpacity>
    );
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <Text style={[typo.headlineSmall, { color: colors.onBackground, paddingHorizontal: 16, paddingTop: 56, paddingBottom: 12 }]}>
        {t('booking.myBookings')}
      </Text>

      {/* Filter tabs */}
      <View style={styles.filterRow}>
        {FILTER_KEYS.map((f) => (
          <TouchableOpacity
            key={f.key}
            style={[styles.filterChip, { backgroundColor: filter === f.key ? colors.primaryContainer : colors.surfaceContainerHigh }]}
            onPress={() => setFilter(f.key)}
          >
            <Text style={[styles.filterText, { color: filter === f.key ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
              {t(f.i18nKey)} ({counts[f.key]})
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Content */}
      {isLoading ? (
        <LoadingState />
      ) : hasError ? (
        <ErrorState message={t('booking.loadError')} onRetry={() => { setIsLoading(true); loadBookings(); }} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon="calendar-blank-outline"
          title={t('booking.emptyTitle')}
          message={filter === 'upcoming' ? t('booking.emptyUpcoming') : filter === 'past' ? t('booking.emptyPast') : t('booking.emptyAll')}
        />
      ) : (
        <FlatList
          data={filtered}
          renderItem={renderBooking}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.list}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
        />
      )}

      {/* Cancel confirmation modal */}
      <Modal visible={!!cancelId} transparent animationType="fade" onRequestClose={() => setCancelId(null)}>
        <Pressable style={styles.modalOverlay} onPress={() => setCancelId(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="event-busy" size={40} color={colors.error} style={{ alignSelf: 'center', marginBottom: 12 }} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('booking.cancelModalTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 20 }]}>
              {t('booking.cancelModalMessage')}
            </Text>
            <View style={styles.modalActions}>
              <TouchableOpacity style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={() => setCancelId(null)}>
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('booking.cancelKeep')}</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.modalBtn, { backgroundColor: colors.error }]} onPress={handleCancel} disabled={isCancelling}>
                <Text style={[styles.modalBtnText, { color: colors.onError }]}>
                  {isCancelling ? t('booking.cancelling') : t('booking.cancelYes')}
                </Text>
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  filterRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingBottom: 12 },
  filterChip: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 999 },
  filterText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  card: { borderRadius: 16, padding: 16, marginBottom: 10 },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  metaRow: { flexDirection: 'row', alignItems: 'center', marginTop: 6 },
  actionsRow: { flexDirection: 'row', gap: 8, marginTop: 12 },
  actionBtn: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999, borderWidth: 1 },
  actionText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  // Modal
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 340, borderRadius: 24, padding: 24 },
  modalActions: { flexDirection: 'row', gap: 12 },
  modalBtn: { flex: 1, paddingVertical: 14, borderRadius: 999, alignItems: 'center' },
  modalBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
