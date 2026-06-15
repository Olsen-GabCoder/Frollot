import { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  FlatList,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { formatDateTimeShort } from '../src/utils/formatDate';
import { useTheme } from '../src/theme';
import { bookingsApi } from '../src/api/bookings';
import { BookingResponse, BookingStatus } from '../src/types';

type BookingFilter = 'ALL' | BookingStatus;

const FILTER_KEYS: { key: BookingFilter; i18nKey: string }[] = [
  { key: 'ALL', i18nKey: 'booking.filter.all' },
  { key: BookingStatus.PENDING, i18nKey: 'booking.filter.pending' },
  { key: BookingStatus.CONFIRMED, i18nKey: 'booking.filter.confirmed' },
  { key: BookingStatus.IN_PROGRESS, i18nKey: 'booking.filter.inProgress' },
  { key: BookingStatus.COMPLETED, i18nKey: 'booking.filter.completed' },
  { key: BookingStatus.CANCELLED, i18nKey: 'booking.filter.cancelled' },
];

export default function OwnerBookingsManagementScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
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
  const [filter, setFilter] = useState<BookingFilter>('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionId, setActionId] = useState<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const loadBookings = async () => {
    if (!salonId) return;
    try {
      const data = await bookingsApi.getSalonBookings(salonId);
      setBookings(data);
      setError(null);
    } catch (e: any) {
      setError(t('booking.loadError'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadBookings();
    intervalRef.current = setInterval(loadBookings, 30000);
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [salonId]);

  const filtered = filter === 'ALL' ? bookings : bookings.filter((b) => b.status === filter);

  const handleCancel = async (bookingId: string) => {
    Alert.alert(t('booking.cancelBooking'), t('booking.cancelConfirm'), [
      { text: t('common.actions.cancel'), style: 'cancel' },
      {
        text: t('booking.cancelBooking'),
        style: 'destructive',
        onPress: async () => {
          setActionId(bookingId);
          try {
            await bookingsApi.cancelBooking(bookingId);
            await loadBookings();
          } catch {} finally {
            setActionId(null);
          }
        },
      },
    ]);
  };

  if (isLoading) {
    return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, flex: 1, marginStart: 16 }]}>
          {t('salon.reservations')}
        </Text>
        <TouchableOpacity onPress={loadBookings}>
          <MaterialIcons name="refresh" size={24} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      {/* Stats */}
      <View style={styles.statsRow}>
        <View style={[styles.statCard, { backgroundColor: colors.primaryContainer }]}>
          <Text style={[typo.headlineSmall, { color: colors.onPrimaryContainer }]}>{bookings.length}</Text>
          <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>{t('booking.total')}</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: colors.warningContainer }]}>
          <Text style={[typo.headlineSmall, { color: colors.onWarningContainer }]}>
            {bookings.filter((b) => b.status === BookingStatus.PENDING).length}
          </Text>
          <Text style={[typo.labelSmall, { color: colors.onWarningContainer }]}>{t('booking.filter.pending')}</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: colors.successContainer }]}>
          <Text style={[typo.headlineSmall, { color: colors.onSuccessContainer }]}>
            {bookings.filter((b) => b.status === BookingStatus.CONFIRMED).length}
          </Text>
          <Text style={[typo.labelSmall, { color: colors.onSuccessContainer }]}>{t('booking.filter.confirmed')}</Text>
        </View>
      </View>

      {/* Filters */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.filtersScroll} contentContainerStyle={styles.filtersContent}>
        {FILTER_KEYS.map((f) => {
          const count = f.key === 'ALL' ? bookings.length : bookings.filter((b) => b.status === f.key).length;
          return (
            <TouchableOpacity
              key={f.key}
              style={[styles.filterChip, {
                backgroundColor: filter === f.key ? colors.primaryContainer : colors.surfaceContainerHigh,
              }]}
              onPress={() => setFilter(f.key)}
            >
              <Text style={[typo.labelMedium, { color: filter === f.key ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
                {t(f.i18nKey)} ({count})
              </Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>

      {error && (
        <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
          <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
        </View>
      )}

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => {
          const date = new Date(item.bookingDatetime);
          const statusColor = STATUS_COLORS[item.status] || colors.onSurfaceVariant;
          const isActioning = actionId === item.id;

          return (
            <TouchableOpacity
              style={[styles.bookingCard, { backgroundColor: colors.surface }]}
              onPress={() => router.push(`/booking/${item.id}`)}
            >
              <View style={styles.cardHeader}>
                <Text style={[typo.titleSmall, { color: colors.onSurface, flex: 1 }]}>{item.clientName}</Text>
                <View style={[styles.statusBadge, { backgroundColor: statusColor + '20' }]}>
                  <Text style={[typo.labelSmall, { color: statusColor }]}>{item.statusLabel}</Text>
                </View>
              </View>
              <Text style={[typo.bodyMedium, { color: colors.onSurface }]}>{item.serviceName}</Text>
              <View style={styles.metaRow}>
                <MaterialIcons name="event" size={14} color={colors.onSurfaceVariant} />
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>
                  {formatDateTimeShort(date)}
                </Text>
              </View>
              {item.staffName && (
                <View style={styles.metaRow}>
                  <MaterialIcons name="person" size={14} color={colors.onSurfaceVariant} />
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>{item.staffName}</Text>
                </View>
              )}
              {item.formattedPrice && (
                <Text style={[typo.labelLarge, { color: colors.primary, marginTop: 4 }]}>{item.formattedPrice}</Text>
              )}

              {/* Actions */}
              {item.canBeCancelled && (
                <View style={styles.actionsRow}>
                  <TouchableOpacity
                    style={[styles.cancelBtn, { borderColor: colors.error }]}
                    onPress={() => handleCancel(item.id)}
                    disabled={isActioning}
                  >
                    {isActioning ? <ActivityIndicator size="small" color={colors.error} /> : (
                      <Text style={[typo.labelSmall, { color: colors.error }]}>{t('booking.cancelBooking')}</Text>
                    )}
                  </TouchableOpacity>
                </View>
              )}
            </TouchableOpacity>
          );
        }}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <MaterialIcons name="event-busy" size={48} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
              {t('booking.noBookings')}
            </Text>
          </View>
        }
        contentContainerStyle={styles.list}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  statsRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 8 },
  statCard: { flex: 1, padding: 12, borderRadius: 16, alignItems: 'center' },
  filtersScroll: { maxHeight: 44, marginBottom: 8 },
  filtersContent: { paddingHorizontal: 16, gap: 8 },
  filterChip: { paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999 },
  errorCard: { marginHorizontal: 16, padding: 12, borderRadius: 12, marginBottom: 8 },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  bookingCard: { borderRadius: 16, padding: 16, marginBottom: 8 },
  cardHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 4 },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  metaRow: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  actionsRow: { flexDirection: 'row', gap: 8, marginTop: 12 },
  cancelBtn: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999, borderWidth: 1 },
  emptyState: { alignItems: 'center', padding: 48 },
});
