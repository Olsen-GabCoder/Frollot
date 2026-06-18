import { useEffect, useState, useCallback, useMemo } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  RefreshControl,
  I18nManager,
  Alert,
  ActivityIndicator,
  TextInput,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { salonsApi } from '../src/api/salons';
import { bookingsApi } from '../src/api/bookings';
import { reviewsApi } from '../src/api/reviews';
import Svg, { Path, Defs, LinearGradient, Stop, Circle, Line, Text as SvgText } from 'react-native-svg';
import { Salon, BookingStatistics, SalonReviewStats, BookingResponse, BookingStatus, BookingSummary } from '../src/types';
import { EditBottomSheet } from '../src/components/profile';
import { resolveMediaUrl } from '../src/utils/media';
import { AccessDenied } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';

// Category pills (Activite is the only active one in this lot)
const CATEGORIES = ['activity', 'bookings', 'mySalon', 'community'] as const;

// T7 — Period selector
const PERIODS = [
  { key: 'today', days: 0 },
  { key: '7d', days: 6 },
  { key: '28d', days: 27 },
] as const;
type PeriodKey = typeof PERIODS[number]['key'];

function formatDateParam(d: Date): string {
  return d.toISOString().slice(0, 10);
}

function getPeriodRange(key: PeriodKey): { from: string; to: string } {
  const to = new Date();
  const from = new Date();
  const period = PERIODS.find((p) => p.key === key)!;
  from.setDate(to.getDate() - period.days);
  return { from: formatDateParam(from), to: formatDateParam(to) };
}

// T8 — SVG chart component
const CHART_W = 300;
const CHART_H = 120;
const CHART_PAD = { top: 10, bottom: 20, left: 0, right: 0 };

function DailyChart({ data, colors, t }: { data: BookingSummary[]; colors: any; t: any }) {
  if (data.length === 0) return null;

  const maxCount = Math.max(...data.map((d) => d.count), 1); // min 1 to avoid /0
  const allZero = data.every((d) => d.count === 0);
  const drawW = CHART_W - CHART_PAD.left - CHART_PAD.right;
  const drawH = CHART_H - CHART_PAD.top - CHART_PAD.bottom;

  const points = data.map((d, i) => {
    const x = CHART_PAD.left + (data.length === 1 ? drawW / 2 : (i / (data.length - 1)) * drawW);
    const y = CHART_PAD.top + drawH - (d.count / maxCount) * drawH;
    return { x, y };
  });

  // Curve path
  const linePath = points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
  // Area path (close to bottom)
  const areaPath = linePath + ` L${points[points.length - 1].x.toFixed(1)},${CHART_H - CHART_PAD.bottom} L${points[0].x.toFixed(1)},${CHART_H - CHART_PAD.bottom} Z`;

  // Date labels (start / mid / end)
  const labelIndices = data.length <= 2
    ? data.map((_, i) => i)
    : [0, Math.floor(data.length / 2), data.length - 1];
  const formatLabel = (dateStr: string) => {
    const d = new Date(dateStr + 'T00:00:00');
    return `${d.getDate()}/${d.getMonth() + 1}`;
  };

  return (
    <View style={{ alignItems: 'center', marginTop: 8 }}>
      <Svg width={CHART_W} height={CHART_H} viewBox={`0 0 ${CHART_W} ${CHART_H}`}>
        <Defs>
          <LinearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
            <Stop offset="0%" stopColor={colors.primary} stopOpacity="0.3" />
            <Stop offset="100%" stopColor={colors.primary} stopOpacity="0.02" />
          </LinearGradient>
        </Defs>
        {/* Grid line at y=0 */}
        <Line x1={CHART_PAD.left} y1={CHART_H - CHART_PAD.bottom} x2={CHART_W - CHART_PAD.right} y2={CHART_H - CHART_PAD.bottom} stroke={colors.outlineVariant} strokeWidth={0.5} />
        {/* Area fill */}
        <Path d={areaPath} fill="url(#areaGrad)" />
        {/* Curve line */}
        <Path d={linePath} fill="none" stroke={colors.primary} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
        {/* Dots on each point (only if few points) */}
        {data.length <= 7 && points.map((p, i) => (
          <Circle key={i} cx={p.x} cy={p.y} r={3} fill={colors.primary} />
        ))}
        {/* Date labels */}
        {labelIndices.map((idx) => (
          <SvgText key={idx} x={points[idx].x} y={CHART_H - 2} fontSize={9} fill={colors.onSurfaceVariant} textAnchor="middle" fontFamily="Manrope-Regular">
            {formatLabel(data[idx].date)}
          </SvgText>
        ))}
      </Svg>
      {allZero && (
        <Text style={{ fontFamily: 'Manrope-Regular', fontSize: 12, color: colors.onSurfaceVariant, marginTop: 4 }}>
          {t('profile.ownerDashboard.chart.empty')}
        </Text>
      )}
    </View>
  );
}

interface MetricCard {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  label: string;
  value: string;
}

interface ManageTile {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  labelKey: string;
  route?: string;
  routeParams?: Record<string, string>;
}

export default function OwnerDashboardScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  const [salons, setSalons] = useState<Salon[]>([]);
  const [activeSalonId, setActiveSalonId] = useState<string | null>(null);
  const [isLoadingSalons, setIsLoadingSalons] = useState(true);
  const { role, isLoading: permLoading, can } = usePermissions(activeSalonId);
  const [refreshing, setRefreshing] = useState(false);

  // Metrics
  const [bookingStats, setBookingStats] = useState<BookingStatistics | null>(null);
  const [reviewStats, setReviewStats] = useState<SalonReviewStats | null>(null);
  const [followersCount, setFollowersCount] = useState<number | null>(null);
  const [isLoadingMetrics, setIsLoadingMetrics] = useState(false);

  // All bookings (factored: T3 pending + T5 distribution)
  const [allBookings, setAllBookings] = useState<BookingResponse[]>([]);
  const [isLoadingBookings, setIsLoadingBookings] = useState(false);
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [declineVisible, setDeclineVisible] = useState(false);
  const [declineBookingId, setDeclineBookingId] = useState<string | null>(null);
  const [declineReason, setDeclineReason] = useState('');
  const [isDeclining, setIsDeclining] = useState(false);

  // T7+T8 — Period selector + chart data
  const [activePeriod, setActivePeriod] = useState<PeriodKey>('28d');
  const [dailySeries, setDailySeries] = useState<BookingSummary[]>([]);
  const [isLoadingChart, setIsLoadingChart] = useState(false);
  const [chartError, setChartError] = useState<string | null>(null);

  const activeSalon = salons.find((s) => s.id === activeSalonId) || null;

  // T6 — Profile completeness (6 fields)
  const profileCompleteness = useMemo(() => {
    if (!activeSalon) return 0;
    const fields = [
      !!activeSalon.name,
      !!activeSalon.description?.trim(),
      !!activeSalon.address,
      !!activeSalon.city,
      !!activeSalon.coverPhotoUrl,
      !!activeSalon.postalCode?.trim(),
    ];
    return Math.round((fields.filter(Boolean).length / fields.length) * 100);
  }, [activeSalon]);

  // Load salons
  const loadSalons = useCallback(async () => {
    if (!user) return;
    setIsLoadingSalons(true);
    try {
      const list = await salonsApi.getSalonsByOwner(user.id);
      setSalons(list);
      if (list.length > 0 && !activeSalonId) {
        setActiveSalonId(list[0].id);
      }
    } catch {} finally {
      setIsLoadingSalons(false);
    }
  }, [user, activeSalonId]);

  // Load all bookings for active salon (T3 pending + T5 distribution)
  const loadBookings = useCallback(async () => {
    if (!activeSalonId) return;
    setIsLoadingBookings(true);
    try {
      const list = await bookingsApi.getSalonBookings(activeSalonId);
      setAllBookings(list);
    } catch (e) {
      console.error('loadBookings', e);
      setAllBookings([]);
    } finally {
      setIsLoadingBookings(false);
    }
  }, [activeSalonId]);

  // T3 — Derived: pending bookings
  const pendingBookings = useMemo(
    () => allBookings.filter((b) => b.status === BookingStatus.PENDING),
    [allBookings],
  );

  // T5 — Derived: top services distribution (CONFIRMED + IN_PROGRESS + COMPLETED)
  const ENGAGED_STATUSES = new Set([BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED]);
  const serviceDistribution = useMemo(() => {
    const engaged = allBookings.filter((b) => ENGAGED_STATUSES.has(b.status));
    const counts = new Map<string, number>();
    for (const b of engaged) {
      counts.set(b.serviceName, (counts.get(b.serviceName) || 0) + 1);
    }
    return [...counts.entries()]
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  }, [allBookings]);

  // Load metrics for active salon
  const loadMetrics = useCallback(async () => {
    if (!activeSalonId) return;
    setIsLoadingMetrics(true);
    const [bRes, rRes, sRes] = await Promise.allSettled([
      bookingsApi.getBookingStatistics(activeSalonId),
      reviewsApi.getSalonReviewStats(activeSalonId),
      salonsApi.getSalonById(activeSalonId),
    ]);
    setBookingStats(bRes.status === 'fulfilled' ? bRes.value : null);
    setReviewStats(rRes.status === 'fulfilled' ? rRes.value : null);
    setFollowersCount(sRes.status === 'fulfilled' ? (sRes.value.followersCount ?? 0) : null);
    setIsLoadingMetrics(false);
  }, [activeSalonId]);

  // T8 — Load chart data
  const loadChart = useCallback(async () => {
    if (!activeSalonId) return;
    setIsLoadingChart(true);
    setChartError(null);
    try {
      const { from, to } = getPeriodRange(activePeriod);
      const data = await bookingsApi.getDailyBookings(activeSalonId, from, to);
      setDailySeries(data);
    } catch (e: any) {
      console.error('loadChart', e);
      setChartError(e?.message || String(e));
      setDailySeries([]);
    } finally {
      setIsLoadingChart(false);
    }
  }, [activeSalonId, activePeriod]);

  useEffect(() => { loadSalons(); }, [loadSalons]);
  useEffect(() => {
    if (activeSalonId) {
      loadMetrics();
      loadBookings();
      loadChart();
    }
  }, [activeSalonId, loadMetrics, loadBookings, loadChart]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadSalons();
    await Promise.all([loadMetrics(), loadBookings(), loadChart()]);
    setRefreshing(false);
  }, [loadSalons, loadMetrics, loadBookings, loadChart]);

  // Metrics cards (revenue visible only with payment.view_salon)
  const metrics: MetricCard[] = [
    {
      icon: 'calendar-check-outline',
      label: t('profile.ownerDashboard.metrics.bookings'),
      value: bookingStats?.totalBookings?.toString() ?? '—',
    },
    ...(can('payment.view_salon') ? [{
      icon: 'cash-multiple' as const,
      label: t('profile.ownerDashboard.metrics.revenue'),
      value: bookingStats?.revenue != null ? `${Math.round(bookingStats.revenue)} FCFA` : '—',
    }] : []),
    {
      icon: 'star-outline',
      label: t('profile.ownerDashboard.metrics.rating'),
      value: reviewStats ? `${reviewStats.averageRating.toFixed(1)} (${reviewStats.totalReviews})` : '—',
    },
    {
      icon: 'account-group-outline',
      label: t('profile.ownerDashboard.metrics.followers'),
      value: followersCount?.toString() ?? '—',
    },
  ];

  // Manage tiles
  const tiles: ManageTile[] = [
    { icon: 'calendar-clock', labelKey: 'bookings', route: '/owner-bookings', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'content-cut', labelKey: 'services', route: '/owner-services', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'account-multiple-outline', labelKey: 'staff', route: '/owner-staff', routeParams: { salonId: activeSalonId || '' } },
    ...(can('salon.update_info') ? [{ icon: 'store-edit-outline' as const, labelKey: 'editSalon', route: '/edit-salon', routeParams: { salonId: activeSalonId || '' } }] : []),
    { icon: 'clock-outline', labelKey: 'hours' },
    { icon: 'star-half-full', labelKey: 'reviews' },
    { icon: 'credit-card-outline', labelKey: 'payments' },
    { icon: 'human-queue', labelKey: 'queue', route: '/queue-management', routeParams: { salonId: activeSalonId || '' } },
  ];

  // T3 — Confirm a pending booking
  const handleConfirm = useCallback(async (bookingId: string) => {
    setProcessingId(bookingId);
    try {
      await bookingsApi.updateBookingStatus(bookingId, { status: BookingStatus.CONFIRMED });
      setAllBookings((prev) => prev.map((b) => b.id === bookingId ? { ...b, status: BookingStatus.CONFIRMED } : b));
      loadMetrics();
      Alert.alert(t('profile.ownerDashboard.pending.confirmSuccess'));
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.message || String(e));
    } finally {
      setProcessingId(null);
    }
  }, [loadMetrics, t]);

  // T3 — Open decline modal
  const openDeclineModal = useCallback((bookingId: string) => {
    setDeclineBookingId(bookingId);
    setDeclineReason('');
    setDeclineVisible(true);
  }, []);

  // T3 — Execute decline
  const handleDecline = useCallback(async () => {
    if (!declineBookingId) return;
    setIsDeclining(true);
    try {
      await bookingsApi.updateBookingStatus(declineBookingId, {
        status: BookingStatus.CANCELLED,
        ...(declineReason.trim() ? { notesSalon: declineReason.trim() } : {}),
      });
      setAllBookings((prev) => prev.map((b) => b.id === declineBookingId ? { ...b, status: BookingStatus.CANCELLED } : b));
      loadMetrics();
      setDeclineVisible(false);
      Alert.alert(t('profile.ownerDashboard.pending.declineSuccess'));
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.message || String(e));
    } finally {
      setIsDeclining(false);
    }
  }, [declineBookingId, declineReason, loadMetrics, t]);

  const MAX_PENDING_SHOWN = 3;
  const visiblePending = pendingBookings.slice(0, MAX_PENDING_SHOWN);

  const handleTilePress = (tile: ManageTile) => {
    if (tile.route && activeSalonId) {
      router.push({ pathname: tile.route as any, params: tile.routeParams });
    } else {
      Alert.alert(t('profile.ownerDashboard.comingSoon'));
    }
  };

  // Permission guard: wait for salons + permissions, then block if no role
  if (isLoadingSalons || (activeSalonId && permLoading)) {
    return <View style={[s.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (activeSalonId && role === 'none') {
    return <AccessDenied />;
  }

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Top bar */}
      <View style={[s.topBar, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons
            name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'}
            size={24}
            color={colors.onSurface}
          />
        </TouchableOpacity>
        <Text style={[s.topTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.title')}</Text>
      </View>

      {/* Category pills */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={s.pillsScroll} contentContainerStyle={s.pillsContainer}>
        {CATEGORIES.map((cat) => {
          const active = cat === 'activity';
          return (
            <View
              key={cat}
              style={[
                s.pill,
                active
                  ? { backgroundColor: colors.primary }
                  : { backgroundColor: colors.surface, borderWidth: 1.5, borderColor: colors.outlineVariant },
              ]}
            >
              <Text style={[s.pillText, { color: active ? colors.onPrimary : colors.onSurface }]}>
                {t(`profile.ownerDashboard.${cat}`)}
              </Text>
            </View>
          );
        })}
      </ScrollView>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
        contentContainerStyle={s.scrollContent}
      >
        {/* Salon banner */}
        {isLoadingSalons ? (
          <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 24 }} />
        ) : salons.length === 0 ? (
          <View style={[s.emptyCard, { backgroundColor: colors.surface }]}>
            <MaterialCommunityIcons name="store-off-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
              {t('profile.ownerDashboard.noSalon')}
            </Text>
            <TouchableOpacity
              style={[s.createBtn, { backgroundColor: colors.primary }]}
              onPress={() => router.push({ pathname: '/create-salon', params: { ownerId: user?.id || '' } })}
            >
              <Text style={[s.createBtnText, { color: colors.onPrimary }]}>
                {t('profile.ownerDashboard.createSalon')}
              </Text>
            </TouchableOpacity>
          </View>
        ) : (
          <TouchableOpacity
            style={[s.salonBanner, { backgroundColor: colors.surface }]}
            onPress={() => {
              if (salons.length > 1) {
                // Simple salon picker via Alert
                Alert.alert(
                  t('profile.ownerDashboard.menuLabel'),
                  undefined,
                  salons.map((salon) => ({
                    text: `${salon.name} — ${salon.city}`,
                    onPress: () => setActiveSalonId(salon.id),
                  })),
                );
              }
            }}
            activeOpacity={salons.length > 1 ? 0.7 : 1}
          >
            <View style={[s.salonThumb, { backgroundColor: colors.surfaceContainerHigh }]}>
              {activeSalon?.coverPhotoUrl && (
                <Image source={{ uri: resolveMediaUrl(activeSalon.coverPhotoUrl) }} style={s.salonThumb} contentFit="cover" />
              )}
            </View>
            <View style={{ flex: 1, marginStart: 12 }}>
              <Text style={[s.salonName, { color: colors.onSurface }]} numberOfLines={1}>{activeSalon?.name || '—'}</Text>
              <Text style={[s.salonCity, { color: colors.onSurfaceVariant }]}>{activeSalon?.city || ''}</Text>
              {/* T6 — Profile completeness bar */}
              <View style={s.completenessRow}>
                <View style={[s.completenessTrack, { backgroundColor: colors.surfaceContainerHigh }]}>
                  <View style={[s.completenessFill, { width: `${profileCompleteness}%`, backgroundColor: profileCompleteness === 100 ? colors.tertiary : colors.primary }]} />
                </View>
                <Text style={[s.completenessLabel, { color: colors.onSurfaceVariant }]}>
                  {t('profile.ownerDashboard.completeness', { pct: profileCompleteness })}
                </Text>
              </View>
            </View>
            {salons.length > 1 && (
              <MaterialCommunityIcons name="chevron-down" size={22} color={colors.onSurfaceVariant} />
            )}
          </TouchableOpacity>
        )}

        {/* Metrics cards 2x2 */}
        {activeSalonId && (
          <View style={s.metricsGrid}>
            {metrics.map((m, i) => (
              <View key={i} style={[s.metricCard, { backgroundColor: colors.surface }]}>
                <MaterialCommunityIcons name={m.icon} size={24} color={colors.primary} />
                <Text style={[s.metricValue, { color: colors.onSurface }]}>{isLoadingMetrics ? '...' : m.value}</Text>
                <Text style={[s.metricLabel, { color: colors.onSurfaceVariant }]}>{m.label}</Text>
              </View>
            ))}
          </View>
        )}

        {/* T7+T8 — Chart: Evolution des réservations */}
        {activeSalonId && (
          <View style={s.chartSection}>
            <Text style={[s.sectionTitle, { color: colors.onSurface, marginTop: 0, marginBottom: 8 }]}>
              {t('profile.ownerDashboard.chart.title')}
            </Text>
            {/* Period pills */}
            <View style={s.periodRow}>
              {PERIODS.map((p) => {
                const active = activePeriod === p.key;
                return (
                  <TouchableOpacity
                    key={p.key}
                    style={[s.periodPill, active ? { backgroundColor: colors.primary } : { backgroundColor: colors.surfaceContainerHigh }]}
                    onPress={() => setActivePeriod(p.key)}
                  >
                    <Text style={[s.periodPillText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>
                      {t(`profile.ownerDashboard.chart.${p.key}`)}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
            {/* Chart area */}
            {isLoadingChart ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 24 }} />
            ) : chartError ? (
              <Text style={[s.distEmpty, { color: colors.error }]}>{chartError}</Text>
            ) : (
              <DailyChart data={dailySeries} colors={colors} t={t} />
            )}
          </View>
        )}

        {/* T3 — Pending bookings */}
        {activeSalonId && (
          <View style={{ marginTop: 20 }}>
            <View style={s.pendingHeader}>
              <Text style={[s.sectionTitle, { color: colors.onSurface, marginTop: 0, marginBottom: 0 }]}>
                {t('profile.ownerDashboard.pending.title')}
              </Text>
              {pendingBookings.length > 0 && (
                <View style={[s.badge, { backgroundColor: colors.error }]}>
                  <Text style={[s.badgeText, { color: colors.onError }]}>
                    {t('profile.ownerDashboard.pending.badge', { count: pendingBookings.length })}
                  </Text>
                </View>
              )}
            </View>

            {isLoadingBookings ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 16 }} />
            ) : pendingBookings.length === 0 ? (
              <Text style={[s.pendingEmpty, { color: colors.onSurfaceVariant }]}>
                {t('profile.ownerDashboard.pending.empty')}
              </Text>
            ) : (
              <>
                {visiblePending.map((booking) => (
                  <View key={booking.id} style={[s.pendingCard, { backgroundColor: colors.surface }]}>
                    <View style={s.pendingCardBody}>
                      <View style={[s.pendingInitials, { backgroundColor: colors.primaryContainer }]}>
                        <Text style={[s.pendingInitialsText, { color: colors.onPrimaryContainer }]}>
                          {(booking.clientName || '??').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()}
                        </Text>
                      </View>
                      <View style={{ flex: 1, marginStart: 12 }}>
                        <Text style={[s.pendingName, { color: colors.onSurface }]} numberOfLines={1}>
                          {booking.clientName}
                        </Text>
                        <Text style={[s.pendingService, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                          {booking.serviceName}
                        </Text>
                        <Text style={[s.pendingDate, { color: colors.onSurfaceVariant }]}>
                          {new Date(booking.bookingDatetime).toLocaleDateString(undefined, {
                            weekday: 'short', day: 'numeric', month: 'short',
                          })}{' '}
                          {new Date(booking.bookingDatetime).toLocaleTimeString(undefined, {
                            hour: '2-digit', minute: '2-digit',
                          })}
                          {booking.formattedPrice ? ` — ${booking.formattedPrice}` : ''}
                        </Text>
                      </View>
                    </View>
                    <View style={s.pendingActions}>
                      <TouchableOpacity
                        style={[s.pendingBtn, { borderColor: colors.error }]}
                        onPress={() => openDeclineModal(booking.id)}
                        disabled={processingId === booking.id}
                      >
                        <Text style={[s.pendingBtnText, { color: colors.error }]}>
                          {t('profile.ownerDashboard.pending.decline')}
                        </Text>
                      </TouchableOpacity>
                      <TouchableOpacity
                        style={[s.pendingBtn, s.pendingBtnFilled, { backgroundColor: colors.primary }]}
                        onPress={() => handleConfirm(booking.id)}
                        disabled={processingId === booking.id}
                      >
                        {processingId === booking.id ? (
                          <ActivityIndicator size="small" color={colors.onPrimary} />
                        ) : (
                          <Text style={[s.pendingBtnText, { color: colors.onPrimary }]}>
                            {t('profile.ownerDashboard.pending.confirm')}
                          </Text>
                        )}
                      </TouchableOpacity>
                    </View>
                  </View>
                ))}
                {pendingBookings.length > MAX_PENDING_SHOWN && (
                  <TouchableOpacity
                    style={{ alignSelf: 'center', paddingVertical: 8 }}
                    onPress={() => router.push({ pathname: '/owner-bookings' as any, params: { salonId: activeSalonId } })}
                  >
                    <Text style={[s.viewAllText, { color: colors.primary }]}>
                      {t('profile.ownerDashboard.pending.viewAll')}
                    </Text>
                  </TouchableOpacity>
                )}
              </>
            )}
          </View>
        )}

        {/* T5 — Service distribution */}
        {activeSalonId && (
          <View style={s.distSection}>
            <Text style={[s.sectionTitle, { color: colors.onSurface, marginTop: 0, marginBottom: 12 }]}>
              {t('profile.ownerDashboard.topServices')}
            </Text>
            {serviceDistribution.length === 0 ? (
              <Text style={[s.distEmpty, { color: colors.onSurfaceVariant }]}>
                {t('profile.ownerDashboard.topServicesEmpty')}
              </Text>
            ) : (
              serviceDistribution.map((item) => {
                const maxCount = serviceDistribution[0].count;
                const pct = maxCount > 0 ? (item.count / maxCount) * 100 : 0;
                return (
                  <View key={item.name} style={s.distRow}>
                    <Text style={[s.distName, { color: colors.onSurface }]} numberOfLines={1}>{item.name}</Text>
                    <View style={[s.distTrack, { backgroundColor: colors.surfaceContainerHigh }]}>
                      <View style={[s.distFill, { width: `${pct}%`, backgroundColor: colors.primary }]} />
                    </View>
                    <Text style={[s.distCount, { color: colors.onSurfaceVariant }]}>{item.count}</Text>
                  </View>
                );
              })
            )}
          </View>
        )}

        {/* Manage grid */}
        {activeSalonId && (
          <>
            <Text style={[s.sectionTitle, { color: colors.onSurface }]}>
              {t('profile.ownerDashboard.manage')}
            </Text>
            <View style={s.tilesGrid}>
              {tiles.map((tile) => {
                const isActive = !!tile.route;
                return (
                  <TouchableOpacity
                    key={tile.labelKey}
                    style={[s.tile, { backgroundColor: colors.surface, opacity: isActive ? 1 : 0.5 }]}
                    onPress={() => handleTilePress(tile)}
                    activeOpacity={0.7}
                  >
                    <MaterialCommunityIcons name={tile.icon} size={28} color={isActive ? colors.primary : colors.onSurfaceVariant} />
                    <Text style={[s.tileLabel, { color: isActive ? colors.onSurface : colors.onSurfaceVariant }]}>
                      {t(`profile.ownerDashboard.tiles.${tile.labelKey}`)}
                    </Text>
                    {!isActive && (
                      <Text style={[s.tileComingSoon, { color: colors.onSurfaceVariant }]}>
                        {t('profile.ownerDashboard.comingSoon')}
                      </Text>
                    )}
                  </TouchableOpacity>
                );
              })}
            </View>
          </>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* T3 — Decline modal */}
      <EditBottomSheet
        visible={declineVisible}
        onClose={() => setDeclineVisible(false)}
        title={t('profile.ownerDashboard.pending.declineTitle')}
        onSave={handleDecline}
        isSaving={isDeclining}
        saveLabel={t('profile.ownerDashboard.pending.declineConfirm')}
      >
        <TextInput
          style={[s.declineInput, { color: colors.onSurface, borderColor: colors.outlineVariant, backgroundColor: colors.surfaceContainerHigh }]}
          placeholder={t('profile.ownerDashboard.pending.declineReason')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={declineReason}
          onChangeText={setDeclineReason}
          multiline
          numberOfLines={3}
          textAlignVertical="top"
        />
      </EditBottomSheet>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  topBar: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  topTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  pillsScroll: { flexGrow: 0, marginTop: 4 },
  pillsContainer: { paddingHorizontal: 16, paddingVertical: 10, gap: 10 },
  pill: { paddingVertical: 10, paddingHorizontal: 20, borderRadius: 999, alignItems: 'center' as const, justifyContent: 'center' as const },
  pillText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', lineHeight: 20 },
  scrollContent: { paddingHorizontal: 16 },
  // Salon banner
  salonBanner: { flexDirection: 'row', alignItems: 'center', borderRadius: 16, padding: 12, marginTop: 8 },
  salonThumb: { width: 56, height: 56, borderRadius: 12 },
  salonName: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  salonCity: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2 },
  // Empty state
  emptyCard: { borderRadius: 16, padding: 32, alignItems: 'center', marginTop: 8, gap: 12 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
  createBtn: { paddingVertical: 12, paddingHorizontal: 24, borderRadius: 999 },
  createBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  // Metrics 2x2
  metricsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginTop: 16 },
  metricCard: { width: '48%', borderRadius: 16, padding: 16, gap: 6, flexGrow: 1, flexBasis: '45%' },
  metricValue: { fontFamily: 'Manrope-Bold', fontSize: 20, fontWeight: '700' },
  metricLabel: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  // Manage section
  sectionTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 20, fontWeight: '600', marginTop: 24, marginBottom: 12 },
  tilesGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  tile: { width: '31%', borderRadius: 16, padding: 14, alignItems: 'center', gap: 6, flexGrow: 1, flexBasis: '29%', minHeight: 100, justifyContent: 'center' },
  tileLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', textAlign: 'center' },
  tileComingSoon: { fontFamily: 'Manrope-Regular', fontSize: 9, textAlign: 'center' },
  // T3 — Pending section
  pendingHeader: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 },
  badge: { paddingHorizontal: 10, paddingVertical: 3, borderRadius: 999 },
  badgeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  pendingEmpty: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', paddingVertical: 12 },
  pendingCard: { borderRadius: 14, padding: 14, marginBottom: 10 },
  pendingCardBody: { flexDirection: 'row', alignItems: 'center' },
  pendingInitials: { width: 42, height: 42, borderRadius: 21, alignItems: 'center', justifyContent: 'center' },
  pendingInitialsText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },
  pendingName: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  pendingService: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 1 },
  pendingDate: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  pendingActions: { flexDirection: 'row', gap: 10, marginTop: 10 },
  pendingBtn: { flex: 1, paddingVertical: 10, borderRadius: 999, alignItems: 'center', justifyContent: 'center', borderWidth: 1.5 },
  pendingBtnFilled: { borderWidth: 0 },
  pendingBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700' },
  viewAllText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  declineInput: { borderWidth: 1, borderRadius: 12, padding: 12, fontSize: 14, fontFamily: 'Manrope-Regular', minHeight: 80 },
  // T6 — Completeness bar
  completenessRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 6 },
  completenessTrack: { flex: 1, height: 4, borderRadius: 999, overflow: 'hidden' },
  completenessFill: { height: '100%', borderRadius: 999 },
  completenessLabel: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  // T5 — Service distribution
  distSection: { marginTop: 20 },
  distRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 },
  distName: { fontFamily: 'Manrope-Regular', fontSize: 13, width: 110 },
  distTrack: { flex: 1, height: 6, borderRadius: 999, overflow: 'hidden' },
  distFill: { height: '100%', borderRadius: 999 },
  distCount: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', width: 28, textAlign: 'right' },
  distEmpty: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', paddingVertical: 12 },
  // T7+T8 — Chart section
  chartSection: { marginTop: 20 },
  periodRow: { flexDirection: 'row', gap: 8, marginBottom: 12 },
  periodPill: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999 },
  periodPillText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
});
