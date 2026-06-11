import { useEffect, useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { bookingsApi } from '../../src/api/bookings';
import { BookingResponse, BookingStatus, PaymentStatus } from '../../src/types';

const STATUS_EMOJI: Record<BookingStatus, string> = {
  [BookingStatus.PENDING]: '\u23F3',
  [BookingStatus.CONFIRMED]: '\u2705',
  [BookingStatus.IN_PROGRESS]: '\u26A1',
  [BookingStatus.COMPLETED]: '\uD83C\uDF89',
  [BookingStatus.CANCELLED]: '\u274C',
  [BookingStatus.NO_SHOW]: '\uD83D\uDC7B',
};

export default function BookingDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [booking, setBooking] = useState<BookingResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadBooking = async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      const b = await bookingsApi.getBookingById(id);
      setBooking(b);
    } catch (e: any) {
      setError(e?.message || t('common.error'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadBooking();
  }, [id]);

  const handleCancel = () => {
    Alert.alert(
      t('booking.cancelBooking'),
      t('booking.cancelConfirm'),
      [
        { text: t('common.cancel'), style: 'cancel' },
        {
          text: t('booking.cancelBooking'),
          style: 'destructive',
          onPress: async () => {
            if (!id) return;
            setIsCancelling(true);
            try {
              await bookingsApi.cancelBooking(id);
              await loadBooking();
            } catch (e: any) {
              Alert.alert(t('common.error'), e?.response?.data?.message || t('common.error'));
            } finally {
              setIsCancelling(false);
            }
          },
        },
      ],
    );
  };

  if (isLoading) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (error || !booking) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[typo.bodyLarge, { color: colors.error }]}>{error || t('common.error')}</Text>
        <TouchableOpacity style={[styles.retryBtn, { backgroundColor: colors.primary }]} onPress={loadBooking}>
          <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>{t('common.retry')}</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const date = new Date(booking.bookingDatetime);

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>
          {t('booking.myBookings')}
        </Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Status card */}
        <View style={[styles.card, { backgroundColor: colors.surface, alignItems: 'center' }]}>
          <Text style={{ fontSize: 48 }}>{STATUS_EMOJI[booking.status]}</Text>
          <Text style={[typo.headlineSmall, { color: colors.onSurface, marginTop: 8 }]}>
            {booking.statusLabel}
          </Text>
        </View>

        {/* Salon card */}
        <TouchableOpacity
          style={[styles.card, styles.cardRow, { backgroundColor: colors.surface }]}
          onPress={() => router.push(`/salon/${booking.salonId}`)}
        >
          <View style={[styles.iconCircle, { backgroundColor: colors.primaryContainer }]}>
            <MaterialIcons name="store" size={24} color={colors.onPrimaryContainer} />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{booking.salonName}</Text>
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
              {t('common.seeAll')}
            </Text>
          </View>
          <MaterialIcons name="chevron-right" size={24} color={colors.onSurfaceVariant} />
        </TouchableOpacity>

        {/* Service card */}
        <View style={[styles.card, styles.cardRow, { backgroundColor: colors.surface }]}>
          <View style={[styles.iconCircle, { backgroundColor: colors.secondaryContainer }]}>
            <MaterialIcons name="content-cut" size={24} color={colors.onSecondaryContainer} />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{booking.serviceName}</Text>
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
              {booking.serviceCategory} - {booking.formattedDuration}
            </Text>
          </View>
        </View>

        {/* Date & staff card */}
        <View style={[styles.card, { backgroundColor: colors.surface }]}>
          <View style={styles.detailRow}>
            <MaterialIcons name="event" size={20} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyLarge, { color: colors.onSurface, marginLeft: 12 }]}>
              {date.toLocaleDateString()} - {date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </Text>
          </View>
          {booking.staffName && (
            <View style={[styles.detailRow, { marginTop: 12 }]}>
              <MaterialIcons name="person" size={20} color={colors.onSurfaceVariant} />
              <Text style={[typo.bodyLarge, { color: colors.onSurface, marginLeft: 12 }]}>
                {booking.staffName}
              </Text>
            </View>
          )}
          <View style={[styles.detailRow, { marginTop: 12 }]}>
            <MaterialIcons name="access-time" size={20} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyLarge, { color: colors.onSurface, marginLeft: 12 }]}>
              {booking.formattedDuration}
            </Text>
          </View>
        </View>

        {/* Payment card */}
        <View style={[styles.card, { backgroundColor: colors.surface }]}>
          <View style={styles.detailRow}>
            <MaterialIcons name="payment" size={20} color={colors.onSurfaceVariant} />
            <Text style={[typo.headlineSmall, { color: colors.onSurface, marginLeft: 12 }]}>
              {booking.formattedPrice || '-'}
            </Text>
          </View>
          <View style={[styles.paymentBadge, { backgroundColor: colors.surfaceContainerHigh, marginTop: 8 }]}>
            <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant }]}>
              {booking.paymentStatusLabel}
            </Text>
          </View>
          {booking.paymentMethod && (
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 4 }]}>
              {booking.paymentMethod}
            </Text>
          )}
        </View>

        {/* Notes */}
        {booking.notesClient && (
          <View style={[styles.card, { backgroundColor: colors.surface }]}>
            <Text style={[typo.titleSmall, { color: colors.onSurface, marginBottom: 4 }]}>
              {t('booking.notes')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant }]}>
              {booking.notesClient}
            </Text>
          </View>
        )}

        {/* Cancel button */}
        {booking.canBeCancelled && (
          <TouchableOpacity
            style={[styles.cancelBtn, { borderColor: colors.error }]}
            onPress={handleCancel}
            disabled={isCancelling}
          >
            {isCancelling ? (
              <ActivityIndicator color={colors.error} />
            ) : (
              <Text style={[typo.labelLarge, { color: colors.error }]}>
                {t('booking.cancelBooking')}
              </Text>
            )}
          </TouchableOpacity>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  retryBtn: { marginTop: 16, paddingHorizontal: 24, paddingVertical: 12, borderRadius: 28 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scrollContent: { paddingHorizontal: 16, paddingTop: 16 },
  card: { borderRadius: 16, padding: 20, marginBottom: 12 },
  cardRow: { flexDirection: 'row', alignItems: 'center' },
  iconCircle: { width: 44, height: 44, borderRadius: 22, justifyContent: 'center', alignItems: 'center' },
  detailRow: { flexDirection: 'row', alignItems: 'center' },
  paymentBadge: { alignSelf: 'flex-start', paddingHorizontal: 12, paddingVertical: 4, borderRadius: 999 },
  cancelBtn: {
    borderWidth: 1, borderRadius: 28, height: 52,
    justifyContent: 'center', alignItems: 'center', marginTop: 8,
  },
});
