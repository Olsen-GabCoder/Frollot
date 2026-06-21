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
  Dimensions,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import Svg, { Path, Defs, LinearGradient, Stop, Circle, Line, Text as SvgText, Rect } from 'react-native-svg';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { salonsApi } from '../src/api/salons';
import { bookingsApi } from '../src/api/bookings';
import { reviewsApi } from '../src/api/reviews';
import { Salon, BookingStatistics, SalonReviewStats, BookingResponse, BookingStatus, BookingSummary } from '../src/types';
import { EditBottomSheet } from '../src/components/profile';
import { resolveMediaUrl } from '../src/utils/media';
import { AccessDenied, Avatar } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';

// ── Period selector ──────────────────────────────────────────────────
const PERIODS = [
  { key: '7d', pastDays: 6, futureDays: 7 },
  { key: '28d', pastDays: 29, futureDays: 14 },
  { key: 'year', pastDays: 364, futureDays: 30 },
] as const;
type PeriodKey = typeof PERIODS[number]['key'];

function fmtDate(d: Date): string { return d.toISOString().slice(0, 10); }

function getPeriodRange(key: PeriodKey) {
  const p = PERIODS.find((x) => x.key === key)!;
  const now = new Date();
  const from = new Date(); from.setDate(now.getDate() - p.pastDays);
  const to = new Date(); to.setDate(now.getDate() + p.futureDays);
  return { from: fmtDate(from), to: fmtDate(to), todayStr: fmtDate(now), pastDays: p.pastDays, futureDays: p.futureDays };
}

// ── SVG Area Chart ───────────────────────────────────────────────────
const SCREEN_W = Dimensions.get('window').width;

function AreaChart({ data, todayStr, colors, t, height = 140 }: {
  data: BookingSummary[]; todayStr: string; colors: any; t: any; height?: number;
}) {
  if (data.length === 0) return null;

  const W = SCREEN_W - 48; // 24px padding each side
  const H = height;
  const PAD = { top: 16, bottom: 24, left: 28, right: 28 };
  const drawW = W - PAD.left - PAD.right;
  const drawH = H - PAD.top - PAD.bottom;
  const maxCount = Math.max(...data.map((d) => d.count), 1);
  const todayIdx = data.findIndex((d) => d.date === todayStr);

  const pts = data.map((d, i) => ({
    x: PAD.left + (data.length === 1 ? drawW / 2 : (i / (data.length - 1)) * drawW),
    y: PAD.top + drawH - (d.count / maxCount) * drawH,
  }));

  // Smooth curve helper
  const curvePath = (points: { x: number; y: number }[]) => {
    if (points.length < 2) return `M${points[0].x},${points[0].y}`;
    let path = `M${points[0].x.toFixed(1)},${points[0].y.toFixed(1)}`;
    for (let i = 1; i < points.length; i++) {
      const cp1x = (points[i - 1].x + points[i].x) / 2;
      path += ` C${cp1x.toFixed(1)},${points[i - 1].y.toFixed(1)} ${cp1x.toFixed(1)},${points[i].y.toFixed(1)} ${points[i].x.toFixed(1)},${points[i].y.toFixed(1)}`;
    }
    return path;
  };

  // Split at today
  const pastPts = todayIdx >= 0 ? pts.slice(0, todayIdx + 1) : pts;
  const futurePts = todayIdx >= 0 ? pts.slice(todayIdx) : [];

  const pastLine = curvePath(pastPts);
  const pastArea = pastPts.length > 1
    ? pastLine + ` L${pastPts[pastPts.length - 1].x.toFixed(1)},${H - PAD.bottom} L${pastPts[0].x.toFixed(1)},${H - PAD.bottom} Z`
    : '';

  const futureLine = futurePts.length > 1 ? curvePath(futurePts) : '';
  const futureArea = futurePts.length > 1
    ? futureLine + ` L${futurePts[futurePts.length - 1].x.toFixed(1)},${H - PAD.bottom} L${futurePts[0].x.toFixed(1)},${H - PAD.bottom} Z`
    : '';

  // Date labels
  const labelIndices = data.length <= 3
    ? data.map((_, i) => i)
    : [0, ...(todayIdx > 0 && todayIdx < data.length - 1 ? [todayIdx] : [Math.floor(data.length / 2)]), data.length - 1];
  const fmtLabel = (s: string) => { const d = new Date(s + 'T00:00:00'); return `${d.getDate()}/${d.getMonth() + 1}`; };

  return (
    <View style={{ alignItems: 'center', marginTop: 4 }}>
      <Svg width={W} height={H} viewBox={`0 0 ${W} ${H}`}>
        <Defs>
          <LinearGradient id="pastGrad" x1="0" y1="0" x2="0" y2="1">
            <Stop offset="0%" stopColor={colors.primary} stopOpacity="0.25" />
            <Stop offset="100%" stopColor={colors.primary} stopOpacity="0.02" />
          </LinearGradient>
          <LinearGradient id="futureGrad" x1="0" y1="0" x2="0" y2="1">
            <Stop offset="0%" stopColor={colors.tertiary} stopOpacity="0.18" />
            <Stop offset="100%" stopColor={colors.tertiary} stopOpacity="0.02" />
          </LinearGradient>
        </Defs>
        {/* Baseline */}
        <Line x1={PAD.left} y1={H - PAD.bottom} x2={W - PAD.right} y2={H - PAD.bottom} stroke={colors.outlineVariant} strokeWidth={0.5} />
        {/* Today vertical line */}
        {todayIdx >= 0 && (
          <Line x1={pts[todayIdx].x} y1={PAD.top} x2={pts[todayIdx].x} y2={H - PAD.bottom} stroke={colors.outline} strokeWidth={0.5} strokeDasharray="4,3" />
        )}
        {/* Past area + line */}
        {pastArea ? <Path d={pastArea} fill="url(#pastGrad)" /> : null}
        {pastPts.length > 1 && <Path d={pastLine} fill="none" stroke={colors.primary} strokeWidth={2.5} strokeLinecap="round" strokeLinejoin="round" />}
        {/* Future area + line (dashed) */}
        {futureArea ? <Path d={futureArea} fill="url(#futureGrad)" /> : null}
        {futurePts.length > 1 && <Path d={futureLine} fill="none" stroke={colors.tertiary} strokeWidth={2} strokeLinecap="round" strokeDasharray="6,4" />}
        {/* Dots (limited to avoid clutter) */}
        {data.length <= 14 && pts.map((p, i) => (
          <Circle key={i} cx={p.x} cy={p.y} r={i === todayIdx ? 4 : 2.5}
            fill={i <= (todayIdx >= 0 ? todayIdx : pts.length) ? colors.primary : colors.tertiary}
            stroke={i === todayIdx ? colors.surface : 'none'} strokeWidth={i === todayIdx ? 2 : 0} />
        ))}
        {/* Date labels */}
        {labelIndices.map((idx) => {
          const anchor = idx === 0 ? 'start' : idx === data.length - 1 ? 'end' : 'middle';
          return (
            <SvgText key={idx} x={pts[idx].x} y={H - 4} fontSize={10} fill={idx === todayIdx ? colors.primary : colors.onSurfaceVariant} textAnchor={anchor} fontWeight={idx === todayIdx ? '700' : '400'}>
              {idx === todayIdx ? t('profile.ownerDashboard.chart.todayLabel') : fmtLabel(data[idx].date)}
            </SvgText>
          );
        })}
      </Svg>
      {/* Legend */}
      <View style={{ flexDirection: 'row', gap: 16, marginTop: 6 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
          <View style={{ width: 12, height: 3, borderRadius: 2, backgroundColor: colors.primary }} />
          <Text style={{ fontFamily: 'Manrope-Regular', fontSize: 10, color: colors.onSurfaceVariant }}>{t('profile.ownerDashboard.chart.past')}</Text>
        </View>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 4 }}>
          <View style={{ width: 12, height: 3, borderRadius: 2, backgroundColor: colors.tertiary, opacity: 0.7 }} />
          <Text style={{ fontFamily: 'Manrope-Regular', fontSize: 10, color: colors.onSurfaceVariant }}>{t('profile.ownerDashboard.chart.upcoming')}</Text>
        </View>
      </View>
    </View>
  );
}

// ── Status bar chart ─────────────────────────────────────────────────
function StatusBars({ stats, colors, t }: { stats: BookingStatistics; colors: any; t: any }) {
  const items = [
    { key: 'pending', count: stats.pendingBookings, color: colors.warning },
    { key: 'confirmed', count: stats.confirmedBookings, color: colors.info },
    { key: 'completed', count: stats.completedBookings, color: colors.success },
    { key: 'cancelled', count: stats.cancelledBookings, color: colors.error },
  ];
  const max = Math.max(...items.map((i) => i.count), 1);

  return (
    <View style={{ gap: 10 }}>
      {items.map((item) => (
        <View key={item.key} style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
          <Text style={{ fontFamily: 'Manrope-Regular', fontSize: 12, color: colors.onSurfaceVariant, width: 80 }} numberOfLines={1}>
            {t(`profile.ownerDashboard.status.${item.key}`)}
          </Text>
          <View style={{ flex: 1, height: 8, borderRadius: 4, backgroundColor: colors.surfaceContainerHigh, overflow: 'hidden' }}>
            <View style={{ height: '100%', borderRadius: 4, backgroundColor: item.color, width: `${(item.count / max) * 100}%` }} />
          </View>
          <Text style={{ fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', color: colors.onSurface, width: 28, textAlign: 'right' }}>
            {item.count}
          </Text>
        </View>
      ))}
    </View>
  );
}

// ══════════════════════════════════════════════════════════════════════
// MAIN SCREEN
// ══════════════════════════════════════════════════════════════════════
export default function OwnerDashboardScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  const [salons, setSalons] = useState<Salon[]>([]);
  const [activeSalonId, setActiveSalonId] = useState<string | null>(null);
  const [isLoadingSalons, setIsLoadingSalons] = useState(true);
  const { role, isLoading: permLoading, can } = usePermissions(activeSalonId);
  const [refreshing, setRefreshing] = useState(false);

  // Data
  const [bookingStats, setBookingStats] = useState<BookingStatistics | null>(null);
  const [reviewStats, setReviewStats] = useState<SalonReviewStats | null>(null);
  const [followersCount, setFollowersCount] = useState<number | null>(null);
  const [allBookings, setAllBookings] = useState<BookingResponse[]>([]);
  const [dailySeries, setDailySeries] = useState<BookingSummary[]>([]);
  const [isLoadingMetrics, setIsLoadingMetrics] = useState(false);
  const [isLoadingBookings, setIsLoadingBookings] = useState(false);
  const [isLoadingChart, setIsLoadingChart] = useState(false);

  // Period
  const [activePeriod, setActivePeriod] = useState<PeriodKey>('28d');
  const periodRange = useMemo(() => getPeriodRange(activePeriod), [activePeriod]);

  // Pending actions
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [declineVisible, setDeclineVisible] = useState(false);
  const [declineBookingId, setDeclineBookingId] = useState<string | null>(null);
  const [declineReason, setDeclineReason] = useState('');
  const [isDeclining, setIsDeclining] = useState(false);

  const activeSalon = salons.find((s) => s.id === activeSalonId) || null;

  // Profile completeness
  const profileCompleteness = useMemo(() => {
    if (!activeSalon) return 0;
    const fields = [!!activeSalon.name, !!activeSalon.description?.trim(), !!activeSalon.address, !!activeSalon.city, !!activeSalon.coverPhotoUrl, !!activeSalon.postalCode?.trim()];
    return Math.round((fields.filter(Boolean).length / fields.length) * 100);
  }, [activeSalon]);

  // Derived data
  const pendingBookings = useMemo(() => allBookings.filter((b) => b.status === BookingStatus.PENDING), [allBookings]);
  const upcomingBookings = useMemo(() => {
    const now = new Date().toISOString();
    return allBookings
      .filter((b) => b.bookingDatetime > now && b.status !== BookingStatus.CANCELLED && b.status !== BookingStatus.NO_SHOW)
      .sort((a, b) => a.bookingDatetime.localeCompare(b.bookingDatetime))
      .slice(0, 5);
  }, [allBookings]);

  const ENGAGED = new Set([BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED]);
  const serviceDistribution = useMemo(() => {
    const engaged = allBookings.filter((b) => ENGAGED.has(b.status));
    const counts = new Map<string, number>();
    for (const b of engaged) counts.set(b.serviceName, (counts.get(b.serviceName) || 0) + 1);
    const total = engaged.length || 1;
    return [...counts.entries()]
      .map(([name, count]) => ({ name, count, pct: Math.round((count / total) * 100) }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  }, [allBookings]);

  // Loaders
  const loadSalons = useCallback(async () => {
    if (!user) return;
    setIsLoadingSalons(true);
    try {
      const list = await salonsApi.getSalonsByOwner(user.id);
      setSalons(list);
      if (list.length > 0 && !activeSalonId) setActiveSalonId(list[0].id);
    } catch { /* handled by empty state */ } finally { setIsLoadingSalons(false); }
  }, [user, activeSalonId]);

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

  const loadBookings = useCallback(async () => {
    if (!activeSalonId) return;
    setIsLoadingBookings(true);
    try {
      const list = await bookingsApi.getSalonBookings(activeSalonId);
      setAllBookings(list);
    } catch { setAllBookings([]); } finally { setIsLoadingBookings(false); }
  }, [activeSalonId]);

  const loadChart = useCallback(async () => {
    if (!activeSalonId) return;
    setIsLoadingChart(true);
    try {
      const data = await bookingsApi.getDailyBookings(activeSalonId, periodRange.from, periodRange.to);
      setDailySeries(data);
    } catch { setDailySeries([]); } finally { setIsLoadingChart(false); }
  }, [activeSalonId, periodRange]);

  useEffect(() => { loadSalons(); }, [loadSalons]);
  useEffect(() => { if (activeSalonId) { loadMetrics(); loadBookings(); loadChart(); } }, [activeSalonId, loadMetrics, loadBookings, loadChart]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadSalons();
    await Promise.all([loadMetrics(), loadBookings(), loadChart()]);
    setRefreshing(false);
  }, [loadSalons, loadMetrics, loadBookings, loadChart]);

  // Pending actions
  const handleConfirm = useCallback(async (bookingId: string) => {
    setProcessingId(bookingId);
    try {
      await bookingsApi.updateBookingStatus(bookingId, { status: BookingStatus.CONFIRMED });
      setAllBookings((prev) => prev.map((b) => b.id === bookingId ? { ...b, status: BookingStatus.CONFIRMED } : b));
      loadMetrics();
      Alert.alert(t('profile.ownerDashboard.pending.confirmSuccess'));
    } catch (e: any) { Alert.alert(t('common.states.error'), e?.message || String(e)); }
    finally { setProcessingId(null); }
  }, [loadMetrics, t]);

  const openDeclineModal = useCallback((bookingId: string) => {
    setDeclineBookingId(bookingId); setDeclineReason(''); setDeclineVisible(true);
  }, []);

  const handleDecline = useCallback(async () => {
    if (!declineBookingId) return;
    setIsDeclining(true);
    try {
      await bookingsApi.updateBookingStatus(declineBookingId, { status: BookingStatus.CANCELLED, ...(declineReason.trim() ? { notesSalon: declineReason.trim() } : {}) });
      setAllBookings((prev) => prev.map((b) => b.id === declineBookingId ? { ...b, status: BookingStatus.CANCELLED } : b));
      loadMetrics(); setDeclineVisible(false);
      Alert.alert(t('profile.ownerDashboard.pending.declineSuccess'));
    } catch (e: any) { Alert.alert(t('common.states.error'), e?.message || String(e)); }
    finally { setIsDeclining(false); }
  }, [declineBookingId, declineReason, loadMetrics, t]);

  // Manage tiles
  const tiles = [
    { icon: 'calendar-clock' as const, labelKey: 'bookings', route: '/owner-bookings', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'content-cut' as const, labelKey: 'services', route: '/owner-services', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'account-multiple-outline' as const, labelKey: 'staff', route: '/owner-staff', routeParams: { salonId: activeSalonId || '' } },
    ...(can('salon.update_info') ? [{ icon: 'store-edit-outline' as const, labelKey: 'editSalon', route: '/edit-salon', routeParams: { salonId: activeSalonId || '' } }] : []),
    { icon: 'clock-outline' as const, labelKey: 'hours', route: '/edit-opening-hours', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'star-half-full' as const, labelKey: 'reviews', route: '/owner-reviews', routeParams: { salonId: activeSalonId || '' } },
    { icon: 'credit-card-outline' as const, labelKey: 'payments' as const },
    { icon: 'human-queue' as const, labelKey: 'queue', route: '/queue-management', routeParams: { salonId: activeSalonId || '' } },
  ];

  // ── Guards ──
  if (isLoadingSalons || (activeSalonId && permLoading)) {
    return <View style={[s.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (activeSalonId && role === 'none') return <AccessDenied />;

  // ── KPI cards ──
  const kpis = [
    { icon: 'calendar-check-outline' as const, label: t('profile.ownerDashboard.metrics.bookings'), value: bookingStats?.totalBookings?.toString() ?? '—', accent: colors.primary },
    ...(can('payment.view_salon') ? [{ icon: 'cash-multiple' as const, label: t('profile.ownerDashboard.metrics.revenue'), value: bookingStats?.totalRevenue != null ? `${Math.round(bookingStats.totalRevenue).toLocaleString()} FCFA` : '—', accent: colors.tertiary }] : []),
    { icon: 'star-outline' as const, label: t('profile.ownerDashboard.metrics.rating'), value: reviewStats ? `${reviewStats.averageRating.toFixed(1)} (${reviewStats.totalReviews})` : '—', accent: colors.secondary },
    { icon: 'calendar-arrow-right' as const, label: t('profile.ownerDashboard.metrics.upcoming'), value: upcomingBookings.length.toString(), accent: colors.info },
  ];

  const MAX_PENDING = 3;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* ── Header ── */}
      <View style={[s.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.title')}</Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
        contentContainerStyle={s.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* ── Salon banner ── */}
        {salons.length === 0 ? (
          <View style={[s.emptyCard, { backgroundColor: colors.surface }]}>
            <MaterialCommunityIcons name="store-off-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.noSalon')}</Text>
            <TouchableOpacity style={[s.createBtn, { backgroundColor: colors.primary }]} onPress={() => router.push({ pathname: '/create-salon', params: { ownerId: user?.id || '' } })}>
              <Text style={[s.createBtnText, { color: colors.onPrimary }]}>{t('profile.ownerDashboard.createSalon')}</Text>
            </TouchableOpacity>
          </View>
        ) : (
          <TouchableOpacity
            style={[s.salonBanner, { backgroundColor: colors.surface }]}
            onPress={() => { if (salons.length > 1) Alert.alert(t('profile.ownerDashboard.menuLabel'), undefined, salons.map((salon) => ({ text: `${salon.name} — ${salon.city}`, onPress: () => setActiveSalonId(salon.id) }))); }}
            activeOpacity={salons.length > 1 ? 0.7 : 1}
          >
            <View style={[s.salonThumb, { backgroundColor: colors.surfaceContainerHigh }]}>
              {activeSalon?.coverPhotoUrl && <Image source={{ uri: resolveMediaUrl(activeSalon.coverPhotoUrl) }} style={s.salonThumb} contentFit="cover" />}
            </View>
            <View style={{ flex: 1, marginStart: 12 }}>
              <Text style={[s.salonName, { color: colors.onSurface }]} numberOfLines={1}>{activeSalon?.name || '—'}</Text>
              <Text style={[s.salonCity, { color: colors.onSurfaceVariant }]}>{activeSalon?.city || ''}</Text>
              <View style={s.completenessRow}>
                <View style={[s.completenessTrack, { backgroundColor: colors.surfaceContainerHigh }]}>
                  <View style={[s.completenessFill, { width: `${profileCompleteness}%`, backgroundColor: profileCompleteness === 100 ? colors.tertiary : colors.primary }]} />
                </View>
                <Text style={[s.completenessLabel, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.completeness', { pct: profileCompleteness })}</Text>
              </View>
            </View>
            {salons.length > 1 && <MaterialCommunityIcons name="chevron-down" size={22} color={colors.onSurfaceVariant} />}
          </TouchableOpacity>
        )}

        {activeSalonId && (
          <>
            {/* ── KPI cards ── */}
            <View style={s.kpiGrid}>
              {kpis.map((kpi, i) => (
                <View key={i} style={[s.kpiCard, { backgroundColor: colors.surface }]}>
                  <View style={[s.kpiIconCircle, { backgroundColor: kpi.accent + '18' }]}>
                    <MaterialCommunityIcons name={kpi.icon} size={20} color={kpi.accent} />
                  </View>
                  <Text style={[s.kpiValue, { color: colors.onSurface }]}>{isLoadingMetrics ? '...' : kpi.value}</Text>
                  <Text style={[s.kpiLabel, { color: colors.onSurfaceVariant }]}>{kpi.label}</Text>
                </View>
              ))}
            </View>

            {/* ── Chart section ── */}
            <View style={[s.section, { backgroundColor: colors.surface }]}>
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.chart.title')}</Text>
              {/* Period pills */}
              <View style={s.periodRow}>
                {PERIODS.map((p) => {
                  const active = activePeriod === p.key;
                  return (
                    <TouchableOpacity key={p.key} style={[s.periodPill, active ? { backgroundColor: colors.primary } : { backgroundColor: colors.surfaceContainerHigh }]} onPress={() => setActivePeriod(p.key)}>
                      <Text style={[s.periodText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>{t(`profile.ownerDashboard.chart.${p.key}`)}</Text>
                    </TouchableOpacity>
                  );
                })}
              </View>
              {isLoadingChart ? (
                <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 24 }} />
              ) : dailySeries.every((d) => d.count === 0) ? (
                <Text style={[s.emptySection, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.chart.empty')}</Text>
              ) : (
                <AreaChart data={dailySeries} todayStr={periodRange.todayStr} colors={colors} t={t} />
              )}
            </View>

            {/* ── Status distribution ── */}
            {bookingStats && (
              <View style={[s.section, { backgroundColor: colors.surface }]}>
                <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.statusTitle')}</Text>
                <StatusBars stats={bookingStats} colors={colors} t={t} />
              </View>
            )}

            {/* ── Pending bookings ── */}
            <View style={[s.section, { backgroundColor: colors.surface }]}>
              <View style={s.sectionHeader}>
                <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.pending.title')}</Text>
                {pendingBookings.length > 0 && (
                  <View style={[s.badge, { backgroundColor: colors.error }]}>
                    <Text style={[s.badgeText, { color: colors.onError }]}>{pendingBookings.length}</Text>
                  </View>
                )}
              </View>
              {isLoadingBookings ? (
                <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 12 }} />
              ) : pendingBookings.length === 0 ? (
                <Text style={[s.emptySection, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.pending.empty')}</Text>
              ) : (
                <>
                  {pendingBookings.slice(0, MAX_PENDING).map((booking) => (
                    <View key={booking.id} style={[s.pendingCard, { borderColor: colors.outlineVariant }]}>
                      <View style={s.pendingBody}>
                        <Avatar
                          initials={(booking.clientName || '??').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()}
                          size={38}
                          tone="secondary"
                          imageUrl={booking.staffAvatarUrl || booking.clientAvatarUrl}
                        />
                        <View style={{ flex: 1, marginStart: 10 }}>
                          <Text style={[s.pendingName, { color: colors.onSurface }]} numberOfLines={1}>{booking.clientName}</Text>
                          <Text style={[s.pendingMeta, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                            {booking.serviceName} · {new Date(booking.bookingDatetime).toLocaleDateString(undefined, { day: 'numeric', month: 'short' })} {new Date(booking.bookingDatetime).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}
                          </Text>
                        </View>
                        {booking.formattedPrice && <Text style={[s.pendingPrice, { color: colors.onSurface }]}>{booking.formattedPrice}</Text>}
                      </View>
                      <View style={s.pendingActions}>
                        <TouchableOpacity style={[s.pendingBtn, { borderColor: colors.error }]} onPress={() => openDeclineModal(booking.id)} disabled={processingId === booking.id}>
                          <Text style={[s.pendingBtnText, { color: colors.error }]}>{t('profile.ownerDashboard.pending.decline')}</Text>
                        </TouchableOpacity>
                        <TouchableOpacity style={[s.pendingBtnFill, { backgroundColor: colors.primary }]} onPress={() => handleConfirm(booking.id)} disabled={processingId === booking.id}>
                          {processingId === booking.id ? <ActivityIndicator size="small" color={colors.onPrimary} /> : <Text style={[s.pendingBtnText, { color: colors.onPrimary }]}>{t('profile.ownerDashboard.pending.confirm')}</Text>}
                        </TouchableOpacity>
                      </View>
                    </View>
                  ))}
                  {pendingBookings.length > MAX_PENDING && (
                    <TouchableOpacity style={{ alignSelf: 'center', paddingVertical: 6 }} onPress={() => router.push({ pathname: '/owner-bookings' as any, params: { salonId: activeSalonId } })}>
                      <Text style={{ fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', color: colors.primary }}>{t('profile.ownerDashboard.pending.viewAll')}</Text>
                    </TouchableOpacity>
                  )}
                </>
              )}
            </View>

            {/* ── Upcoming bookings ── */}
            <View style={[s.section, { backgroundColor: colors.surface }]}>
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.upcomingTitle')}</Text>
              {upcomingBookings.length === 0 ? (
                <Text style={[s.emptySection, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.upcomingEmpty')}</Text>
              ) : (
                upcomingBookings.map((b) => (
                  <View key={b.id} style={[s.upcomingRow, { borderColor: colors.outlineVariant }]}>
                    <View style={[s.upcomingDot, { backgroundColor: b.status === BookingStatus.CONFIRMED ? colors.success : colors.warning }]} />
                    <View style={{ flex: 1 }}>
                      <Text style={[s.upcomingService, { color: colors.onSurface }]}>{b.serviceName}</Text>
                      <Text style={[s.upcomingMeta, { color: colors.onSurfaceVariant }]}>
                        {b.clientName} · {new Date(b.bookingDatetime).toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short' })} {new Date(b.bookingDatetime).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}
                      </Text>
                    </View>
                    {b.staffName && <Text style={[s.upcomingStaff, { color: colors.onSurfaceVariant }]}>{b.staffName?.split(' ')[0]}</Text>}
                  </View>
                ))
              )}
            </View>

            {/* ── Top services ── */}
            <View style={[s.section, { backgroundColor: colors.surface }]}>
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.topServices')}</Text>
              {serviceDistribution.length === 0 ? (
                <Text style={[s.emptySection, { color: colors.onSurfaceVariant }]}>{t('profile.ownerDashboard.topServicesEmpty')}</Text>
              ) : (
                serviceDistribution.map((item, i) => {
                  const maxC = serviceDistribution[0].count;
                  return (
                    <View key={item.name} style={{ flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 10 }}>
                      <Text style={{ fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700', color: colors.primary, width: 20, textAlign: 'center' }}>{i + 1}</Text>
                      <Text style={{ fontFamily: 'Manrope-Regular', fontSize: 13, color: colors.onSurface, flex: 1 }} numberOfLines={1}>{item.name}</Text>
                      <View style={{ width: 80, height: 6, borderRadius: 3, backgroundColor: colors.surfaceContainerHigh, overflow: 'hidden' }}>
                        <View style={{ height: '100%', borderRadius: 3, backgroundColor: colors.primary, width: `${(item.count / maxC) * 100}%` }} />
                      </View>
                      <Text style={{ fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', color: colors.onSurfaceVariant, width: 36, textAlign: 'right' }}>{item.pct}%</Text>
                    </View>
                  );
                })
              )}
            </View>

            {/* ── Manage grid ── */}
            <Text style={[s.manageSectionTitle, { color: colors.onSurface }]}>{t('profile.ownerDashboard.manage')}</Text>
            <View style={s.tilesGrid}>
              {tiles.map((tile) => {
                const isActive = !!tile.route;
                return (
                  <TouchableOpacity
                    key={tile.labelKey}
                    style={[s.tile, { backgroundColor: colors.surface, opacity: isActive ? 1 : 0.5 }]}
                    onPress={() => { if (isActive && activeSalonId) router.push({ pathname: tile.route as any, params: tile.routeParams }); else Alert.alert(t('profile.ownerDashboard.comingSoon')); }}
                    activeOpacity={0.7}
                  >
                    <MaterialCommunityIcons name={tile.icon} size={26} color={isActive ? colors.primary : colors.onSurfaceVariant} />
                    <Text style={[s.tileLabel, { color: isActive ? colors.onSurface : colors.onSurfaceVariant }]}>{t(`profile.ownerDashboard.tiles.${tile.labelKey}`)}</Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            <View style={{ height: 40 }} />
          </>
        )}
      </ScrollView>

      {/* Decline modal */}
      <EditBottomSheet visible={declineVisible} onClose={() => setDeclineVisible(false)} title={t('profile.ownerDashboard.pending.declineTitle')} onSave={handleDecline} isSaving={isDeclining} saveLabel={t('profile.ownerDashboard.pending.declineConfirm')}>
        <TextInput style={[s.declineInput, { color: colors.onSurface, borderColor: colors.outlineVariant, backgroundColor: colors.surfaceContainerHigh }]} placeholder={t('profile.ownerDashboard.pending.declineReason')} placeholderTextColor={colors.onSurfaceVariant} value={declineReason} onChangeText={setDeclineReason} multiline numberOfLines={3} textAlignVertical="top" />
      </EditBottomSheet>
    </View>
  );
}

// ═══════════════════════════════════════════════════════════════════════
const s = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  headerTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600', flex: 1 },
  scrollContent: { paddingHorizontal: 16, paddingBottom: 32 },
  // Salon banner
  salonBanner: { flexDirection: 'row', alignItems: 'center', borderRadius: 16, padding: 12, marginTop: 8 },
  salonThumb: { width: 56, height: 56, borderRadius: 12 },
  salonName: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  salonCity: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2 },
  completenessRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 6 },
  completenessTrack: { flex: 1, height: 4, borderRadius: 999, overflow: 'hidden' },
  completenessFill: { height: '100%', borderRadius: 999 },
  completenessLabel: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  // Empty
  emptyCard: { borderRadius: 16, padding: 32, alignItems: 'center', marginTop: 8, gap: 12 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
  createBtn: { paddingVertical: 12, paddingHorizontal: 24, borderRadius: 999 },
  createBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  // KPIs
  kpiGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginTop: 16 },
  kpiCard: { borderRadius: 16, padding: 14, gap: 4, flexGrow: 1, flexBasis: '45%' },
  kpiIconCircle: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center', marginBottom: 2 },
  kpiValue: { fontFamily: 'Manrope-Bold', fontSize: 20, fontWeight: '700' },
  kpiLabel: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  // Sections
  section: { borderRadius: 16, padding: 16, marginTop: 12 },
  sectionTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 18, fontWeight: '600', marginBottom: 12 },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 12 },
  emptySection: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', paddingVertical: 12 },
  // Period
  periodRow: { flexDirection: 'row', gap: 8, marginBottom: 8 },
  periodPill: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999 },
  periodText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Badge
  badge: { paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999 },
  badgeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  // Pending
  pendingCard: { borderWidth: 1, borderRadius: 12, padding: 12, marginBottom: 8 },
  pendingBody: { flexDirection: 'row', alignItems: 'center' },
  pendingInitials: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  pendingInitialsText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  pendingName: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  pendingMeta: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 1 },
  pendingPrice: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  pendingActions: { flexDirection: 'row', gap: 8, marginTop: 10 },
  pendingBtn: { flex: 1, paddingVertical: 9, borderRadius: 999, alignItems: 'center', borderWidth: 1.5 },
  pendingBtnFill: { flex: 1, paddingVertical: 9, borderRadius: 999, alignItems: 'center' },
  pendingBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700' },
  // Upcoming
  upcomingRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 10, borderBottomWidth: 0.5 },
  upcomingDot: { width: 8, height: 8, borderRadius: 4 },
  upcomingService: { fontFamily: 'Manrope-SemiBold', fontSize: 13.5, fontWeight: '600' },
  upcomingMeta: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 1 },
  upcomingStaff: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  // Manage
  manageSectionTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 18, fontWeight: '600', marginTop: 20, marginBottom: 12 },
  tilesGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  tile: { borderRadius: 14, padding: 14, alignItems: 'center', gap: 6, flexGrow: 1, flexBasis: '29%', minHeight: 88, justifyContent: 'center' },
  tileLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 11.5, fontWeight: '600', textAlign: 'center' },
  // Decline
  declineInput: { borderWidth: 1, borderRadius: 12, padding: 12, fontSize: 14, fontFamily: 'Manrope-Regular', minHeight: 80 },
});
