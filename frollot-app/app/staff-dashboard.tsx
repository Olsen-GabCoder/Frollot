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
  Dimensions,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import Svg, { Path, Circle, Text as SvgText } from 'react-native-svg';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { bookingsApi } from '../src/api/bookings';
import { BookingResponse, BookingStatus } from '../src/types';
import { Avatar } from '../src/components/common';
import { useMyStaffMemberships, StaffMembership } from '../src/hooks/useMyStaffMemberships';

const SCREEN_W = Dimensions.get('window').width;
const DONUT_SIZE = Math.min(SCREEN_W - 80, 160);
const DONUT_RADIUS = (DONUT_SIZE - 28) / 2;
const DONUT_STROKE = 22;
const BAR_MAX_H = 100;

const STATUS_COLORS: Record<string, (c: any) => { bg: string; fg: string }> = {
  [BookingStatus.PENDING]: (c) => ({ bg: c.warningContainer, fg: c.onWarningContainer }),
  [BookingStatus.CONFIRMED]: (c) => ({ bg: c.infoContainer, fg: c.onInfoContainer }),
  [BookingStatus.IN_PROGRESS]: (c) => ({ bg: c.primaryContainer, fg: c.onPrimaryContainer }),
  [BookingStatus.COMPLETED]: (c) => ({ bg: c.successContainer, fg: c.onSuccessContainer }),
  [BookingStatus.CANCELLED]: (c) => ({ bg: c.errorContainer, fg: c.onErrorContainer }),
  [BookingStatus.NO_SHOW]: (c) => ({ bg: c.errorContainer, fg: c.onErrorContainer }),
};

function fmtTime(iso: string): string {
  return new Date(iso).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
}

function fmtRelative(iso: string, t: any): string {
  const now = new Date();
  const target = new Date(iso);
  const diffMs = target.getTime() - now.getTime();
  if (diffMs < 0) return t('staffDashboard.now');
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 60) return t('staffDashboard.inMinutes', { count: diffMin });
  const diffH = Math.floor(diffMin / 60);
  if (diffH < 24) return t('staffDashboard.inHours', { count: diffH });
  const diffD = Math.floor(diffH / 24);
  return t('staffDashboard.inDays', { count: diffD });
}

function isSameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function startOfWeek(d: Date): Date {
  const r = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  const day = r.getDay();
  r.setDate(r.getDate() - (day === 0 ? 6 : day - 1)); // Monday-based
  return r;
}

function endOfWeek(d: Date): Date {
  const s = startOfWeek(d);
  s.setDate(s.getDate() + 6);
  s.setHours(23, 59, 59, 999);
  return s;
}

export default function StaffDashboardScreen() {
  const { t } = useTranslation();
  const { colors, elevation: elev } = useTheme();
  const user = useAuthStore((s) => s.user);

  const { memberships, isLoading: isLoadingMemberships, reload: reloadMemberships } = useMyStaffMemberships();
  const [activeMembership, setActiveMembership] = useState<StaffMembership | null>(null);

  const [bookings, setBookings] = useState<BookingResponse[]>([]);
  const [isLoadingBookings, setIsLoadingBookings] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const [toast, setToast] = useState<string | null>(null);
  const showToast = useCallback((msg: string) => { setToast(msg); setTimeout(() => setToast(null), 3000); }, []);

  useEffect(() => {
    if (memberships.length > 0 && !activeMembership) setActiveMembership(memberships[0]);
  }, [memberships, activeMembership]);

  const loadBookings = useCallback(async () => {
    if (!activeMembership) return;
    setIsLoadingBookings(true);
    try {
      const data = await bookingsApi.getStaffBookings(activeMembership.staffId);
      setBookings(data);
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setIsLoadingBookings(false);
    }
  }, [activeMembership, t, showToast]);

  useEffect(() => { loadBookings(); }, [loadBookings]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await reloadMemberships();
    await loadBookings();
    setRefreshing(false);
  }, [reloadMemberships, loadBookings]);

  // ---- Computed data ----
  const now = useMemo(() => new Date(), [bookings]); // refresh when bookings change
  const todayStr = useMemo(() => `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`, [now]);

  const activeBookings = useMemo(() => bookings.filter((b) => b.status !== BookingStatus.CANCELLED && b.status !== BookingStatus.NO_SHOW), [bookings]);

  const todayBookings = useMemo(() =>
    activeBookings
      .filter((b) => isSameDay(new Date(b.bookingDatetime), now))
      .sort((a, b) => a.bookingDatetime.localeCompare(b.bookingDatetime)),
    [activeBookings, now],
  );

  const nextBooking = useMemo(() => {
    const nowIso = now.toISOString();
    return activeBookings
      .filter((b) => b.bookingDatetime > nowIso && b.status !== BookingStatus.COMPLETED)
      .sort((a, b) => a.bookingDatetime.localeCompare(b.bookingDatetime))[0] || null;
  }, [activeBookings, now]);

  const weekEnd = useMemo(() => endOfWeek(now), [now]);

  const kpis = useMemo(() => {
    const nowIso = now.toISOString();
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);
    const weekStart = startOfWeek(now);
    return {
      today: todayBookings.length,
      thisWeek: activeBookings.filter((b) => {
        const d = new Date(b.bookingDatetime);
        return d >= weekStart && d <= weekEnd;
      }).length,
      upcoming: activeBookings.filter((b) => b.bookingDatetime > nowIso && b.status !== BookingStatus.COMPLETED).length,
      completedMonth: activeBookings.filter((b) => b.status === BookingStatus.COMPLETED && new Date(b.bookingDatetime) >= monthStart).length,
    };
  }, [activeBookings, todayBookings, now, weekEnd]);

  // Upcoming preview (next 3 non-today future bookings)
  const upcomingPreview = useMemo(() =>
    activeBookings
      .filter((b) => b.bookingDatetime > now.toISOString() && !isSameDay(new Date(b.bookingDatetime), now) && b.status !== BookingStatus.COMPLETED)
      .sort((a, b) => a.bookingDatetime.localeCompare(b.bookingDatetime))
      .slice(0, 3),
    [activeBookings, now],
  );

  // Donut chart data: service distribution (top 4 + Others)
  const donutData = useMemo(() => {
    if (activeBookings.length === 0) return [];
    const map: Record<string, number> = {};
    activeBookings.forEach((b) => { map[b.serviceName] = (map[b.serviceName] || 0) + 1; });
    const sorted = Object.entries(map).sort((a, b) => b[1] - a[1]);
    const total = activeBookings.length;
    const MAX_SEGMENTS = 4;
    const COLORS = [colors.primary, colors.secondary, colors.tertiary, colors.info, colors.success];
    if (sorted.length <= MAX_SEGMENTS + 1) {
      return sorted.map(([name, count], i) => ({ name, count, pct: Math.round((count / total) * 100), color: COLORS[i % COLORS.length] }));
    }
    const top = sorted.slice(0, MAX_SEGMENTS);
    const othersCount = sorted.slice(MAX_SEGMENTS).reduce((sum, [, c]) => sum + c, 0);
    const result = top.map(([name, count], i) => ({ name, count, pct: Math.round((count / total) * 100), color: COLORS[i] }));
    result.push({ name: t('staffDashboard.charts.others'), count: othersCount, pct: Math.round((othersCount / total) * 100), color: COLORS[4] });
    return result;
  }, [activeBookings, colors, t]);

  // Bar chart data: bookings per weekday (Mon=0 -> Sun=6)
  const weekdayData = useMemo(() => {
    const counts = [0, 0, 0, 0, 0, 0, 0];
    activeBookings.forEach((b) => {
      const d = new Date(b.bookingDatetime).getDay();
      counts[d === 0 ? 6 : d - 1]++;
    });
    return counts;
  }, [activeBookings]);

  const weekdayLabels = useMemo(() => [
    t('staffDashboard.charts.mon'), t('staffDashboard.charts.tue'), t('staffDashboard.charts.wed'),
    t('staffDashboard.charts.thu'), t('staffDashboard.charts.fri'), t('staffDashboard.charts.sat'), t('staffDashboard.charts.sun'),
  ], [t]);

  // ---- KPI card data ----
  const kpiCards: { icon: string; label: string; value: number; accent: string }[] = [
    { icon: 'calendar-today', label: t('staffDashboard.kpi.today'), value: kpis.today, accent: colors.primary },
    { icon: 'calendar-week', label: t('staffDashboard.kpi.thisWeek'), value: kpis.thisWeek, accent: colors.info },
    { icon: 'calendar-arrow-right', label: t('staffDashboard.kpi.upcoming'), value: kpis.upcoming, accent: colors.tertiary },
    { icon: 'check-circle-outline', label: t('staffDashboard.kpi.completedMonth'), value: kpis.completedMonth, accent: colors.success },
  ];

  // ---- Guards ----
  if (isLoadingMemberships) {
    return (
      <View style={[s.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (memberships.length === 0) {
    return (
      <View style={[s.container, { backgroundColor: colors.background }]}>
        <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
          <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
            <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('staffDashboard.title')}</Text>
        </View>
        <View style={s.emptyCenter}>
          <MaterialCommunityIcons name="account-hard-hat" size={56} color={colors.onSurfaceVariant} />
          <Text style={[s.emptyTitle, { color: colors.onSurface }]}>{t('mySchedule.noMembership')}</Text>
          <Text style={[s.emptyHint, { color: colors.onSurfaceVariant }]}>{t('mySchedule.noMembershipHint')}</Text>
        </View>
      </View>
    );
  }

  const greeting = (() => {
    const h = now.getHours();
    if (h < 12) return t('staffDashboard.greetingMorning');
    if (h < 18) return t('staffDashboard.greetingAfternoon');
    return t('staffDashboard.greetingEvening');
  })();

  const dateLabel = now.toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' });

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('staffDashboard.title')}</Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
        contentContainerStyle={s.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Greeting + date */}
        <View style={s.greetingSection}>
          <Text style={[s.greeting, { color: colors.onSurface }]}>{greeting}, {user?.firstName || ''}.</Text>
          <Text style={[s.dateLabel, { color: colors.onSurfaceVariant }]}>{dateLabel}</Text>
        </View>

        {/* Salon banner */}
        {activeMembership && (
          <View style={[s.salonBanner, { backgroundColor: colors.surface }, elev[1]]}>
            <View style={[s.salonIconCircle, { backgroundColor: colors.primaryContainer }]}>
              <MaterialCommunityIcons name="store" size={18} color={colors.onPrimaryContainer} />
            </View>
            <Text style={[s.salonName, { color: colors.onSurface }]} numberOfLines={1}>{activeMembership.salonName}</Text>
          </View>
        )}

        {/* Next appointment highlight */}
        {nextBooking && (
          <View style={[s.nextCard, { backgroundColor: colors.primaryContainer }, elev[2]]}>
            <View style={s.nextCardTop}>
              <MaterialCommunityIcons name="clock-fast" size={20} color={colors.onPrimaryContainer} />
              <Text style={[s.nextLabel, { color: colors.onPrimaryContainer }]}>{t('staffDashboard.nextAppointment')}</Text>
              <Text style={[s.nextIn, { color: colors.primary }]}>{fmtRelative(nextBooking.bookingDatetime, t)}</Text>
            </View>
            <View style={s.nextCardBody}>
              <Avatar
                initials={(nextBooking.clientName || '??').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()}
                size={40}
                tone="secondary"
                imageUrl={nextBooking.clientAvatarUrl}
              />
              <View style={{ flex: 1, marginStart: 12 }}>
                <Text style={[s.nextClient, { color: colors.onPrimaryContainer }]} numberOfLines={1}>{nextBooking.clientName}</Text>
                <Text style={[s.nextService, { color: colors.onPrimaryContainer }]} numberOfLines={1}>
                  {nextBooking.serviceName} · {fmtTime(nextBooking.bookingDatetime)} - {fmtTime(nextBooking.endDatetime)}
                </Text>
              </View>
            </View>
          </View>
        )}

        {/* KPIs */}
        {isLoadingBookings ? (
          <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 24 }} />
        ) : (
          <View style={s.kpiGrid}>
            {kpiCards.map((kpi, i) => (
              <View key={i} style={[s.kpiCard, { backgroundColor: colors.surface }, elev[1]]}>
                <View style={[s.kpiIconCircle, { backgroundColor: kpi.accent + '18' }]}>
                  <MaterialCommunityIcons name={kpi.icon as any} size={18} color={kpi.accent} />
                </View>
                <Text style={[s.kpiValue, { color: colors.onSurface }]}>{kpi.value}</Text>
                <Text style={[s.kpiLabel, { color: colors.onSurfaceVariant }]}>{kpi.label}</Text>
              </View>
            ))}
          </View>
        )}

        {/* Today's timeline */}
        <View style={[s.section, { backgroundColor: colors.surface }, elev[1]]}>
          <View style={s.sectionHeader}>
            <MaterialCommunityIcons name="timeline-clock-outline" size={20} color={colors.primary} />
            <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('staffDashboard.todayTimeline')}</Text>
            <Text style={[s.sectionCount, { color: colors.onSurfaceVariant }]}>{todayBookings.length}</Text>
          </View>
          {todayBookings.length === 0 ? (
            <View style={s.sectionEmpty}>
              <MaterialCommunityIcons name="weather-sunny" size={32} color={colors.onSurfaceVariant} />
              <Text style={[s.sectionEmptyText, { color: colors.onSurfaceVariant }]}>{t('staffDashboard.noBookingsToday')}</Text>
            </View>
          ) : (
            todayBookings.map((b, idx) => {
              const sc = (STATUS_COLORS[b.status] || STATUS_COLORS[BookingStatus.PENDING])(colors);
              const isPast = new Date(b.endDatetime) < now;
              return (
                <View key={b.id} style={[s.timelineRow, idx < todayBookings.length - 1 && { borderBottomWidth: 0.5, borderBottomColor: colors.outlineVariant }]}>
                  {/* Time column */}
                  <View style={s.timeCol}>
                    <Text style={[s.timeStart, { color: isPast ? colors.onSurfaceVariant : colors.onSurface }]}>{fmtTime(b.bookingDatetime)}</Text>
                    <Text style={[s.timeEnd, { color: colors.onSurfaceVariant }]}>{fmtTime(b.endDatetime)}</Text>
                  </View>
                  {/* Dot + line */}
                  <View style={s.dotCol}>
                    <View style={[s.dot, { backgroundColor: isPast ? colors.onSurfaceVariant : colors.primary }]} />
                    {idx < todayBookings.length - 1 && <View style={[s.dotLine, { backgroundColor: colors.outlineVariant }]} />}
                  </View>
                  {/* Content */}
                  <View style={[s.timelineContent, isPast && { opacity: 0.6 }]}>
                    <View style={s.timelineTopRow}>
                      <Avatar
                        initials={(b.clientName || '??').split(' ').map((w) => w[0]).join('').slice(0, 2).toUpperCase()}
                        size={34}
                        tone="secondary"
                        imageUrl={b.clientAvatarUrl}
                      />
                      <View style={{ flex: 1, marginStart: 10 }}>
                        <Text style={[s.timelineClient, { color: colors.onSurface }]} numberOfLines={1}>{b.clientName}</Text>
                        <Text style={[s.timelineService, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{b.serviceName} · {b.formattedDuration}</Text>
                      </View>
                      <View style={[s.statusBadge, { backgroundColor: sc.bg }]}>
                        <Text style={[s.statusText, { color: sc.fg }]}>
                          {t(`booking.status.${b.status === BookingStatus.IN_PROGRESS ? 'inProgress' : b.status === BookingStatus.NO_SHOW ? 'noShow' : b.status}`)}
                        </Text>
                      </View>
                    </View>
                  </View>
                </View>
              );
            })
          )}
        </View>

        {/* Upcoming preview */}
        {upcomingPreview.length > 0 && (
          <View style={[s.section, { backgroundColor: colors.surface }, elev[1]]}>
            <View style={s.sectionHeader}>
              <MaterialCommunityIcons name="calendar-arrow-right" size={20} color={colors.tertiary} />
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('staffDashboard.upcoming')}</Text>
            </View>
            {upcomingPreview.map((b) => {
              const d = new Date(b.bookingDatetime);
              const dayLabel = d.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short' });
              return (
                <View key={b.id} style={[s.previewRow, { borderBottomWidth: 0.5, borderBottomColor: colors.outlineVariant }]}>
                  <Text style={[s.previewDay, { color: colors.primary }]}>{dayLabel}</Text>
                  <View style={{ flex: 1 }}>
                    <Text style={[s.previewClient, { color: colors.onSurface }]} numberOfLines={1}>{b.clientName}</Text>
                    <Text style={[s.previewMeta, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{b.serviceName} · {fmtTime(b.bookingDatetime)}</Text>
                  </View>
                </View>
              );
            })}
          </View>
        )}

        {/* Donut chart — service distribution (replaces top services list) */}
        {donutData.length > 0 && (
          <View style={[s.section, { backgroundColor: colors.surface }, elev[1]]}>
            <View style={s.sectionHeader}>
              <MaterialCommunityIcons name="chart-donut" size={20} color={colors.secondary} />
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('staffDashboard.charts.serviceDistribution')}</Text>
            </View>
            <View style={s.donutContainer}>
              <Svg width={DONUT_SIZE} height={DONUT_SIZE} viewBox={`0 0 ${DONUT_SIZE} ${DONUT_SIZE}`}>
                {donutData.length === 1 ? (
                  <Circle cx={DONUT_SIZE / 2} cy={DONUT_SIZE / 2} r={DONUT_RADIUS} fill="none" stroke={donutData[0].color} strokeWidth={DONUT_STROKE} />
                ) : (
                  donutData.map((seg, i) => {
                    const startAngle = donutData.slice(0, i).reduce((sum, s) => sum + (s.pct / 100) * 360, 0) - 90;
                    const sweepAngle = (seg.pct / 100) * 360;
                    const startRad = (startAngle * Math.PI) / 180;
                    const endRad = ((startAngle + sweepAngle) * Math.PI) / 180;
                    const cx = DONUT_SIZE / 2;
                    const cy = DONUT_SIZE / 2;
                    const x1 = cx + DONUT_RADIUS * Math.cos(startRad);
                    const y1 = cy + DONUT_RADIUS * Math.sin(startRad);
                    const x2 = cx + DONUT_RADIUS * Math.cos(endRad);
                    const y2 = cy + DONUT_RADIUS * Math.sin(endRad);
                    const largeArc = sweepAngle > 180 ? 1 : 0;
                    return (
                      <Path
                        key={i}
                        d={`M ${x1} ${y1} A ${DONUT_RADIUS} ${DONUT_RADIUS} 0 ${largeArc} 1 ${x2} ${y2}`}
                        fill="none"
                        stroke={seg.color}
                        strokeWidth={DONUT_STROKE}
                        strokeLinecap="round"
                      />
                    );
                  })
                )}
                <SvgText x={DONUT_SIZE / 2} y={DONUT_SIZE / 2 - 6} textAnchor="middle" fill={colors.onSurface} fontSize={22} fontWeight="700" fontFamily="Cormorant-Bold">
                  {activeBookings.length}
                </SvgText>
                <SvgText x={DONUT_SIZE / 2} y={DONUT_SIZE / 2 + 12} textAnchor="middle" fill={colors.onSurfaceVariant} fontSize={11} fontFamily="Manrope-Regular">
                  {t('staffDashboard.charts.total')}
                </SvgText>
              </Svg>
            </View>
            {/* Legend */}
            <View style={s.donutLegend}>
              {donutData.map((seg) => (
                <View key={seg.name} style={s.legendItem}>
                  <View style={[s.legendDot, { backgroundColor: seg.color }]} />
                  <Text style={[s.legendName, { color: colors.onSurface }]} numberOfLines={1}>{seg.name}</Text>
                  <Text style={[s.legendPct, { color: colors.onSurfaceVariant }]}>{seg.pct}%</Text>
                </View>
              ))}
            </View>
          </View>
        )}

        {/* Bar chart — bookings per weekday */}
        {activeBookings.length > 0 && (
          <View style={[s.section, { backgroundColor: colors.surface }, elev[1]]}>
            <View style={s.sectionHeader}>
              <MaterialCommunityIcons name="chart-bar" size={20} color={colors.tertiary} />
              <Text style={[s.sectionTitle, { color: colors.onSurface }]}>{t('staffDashboard.charts.weeklyLoad')}</Text>
            </View>
            {(() => {
              const maxCount = Math.max(...weekdayData, 1);
              return (
                <View style={s.barChartContainer}>
                  {weekdayData.map((count, i) => {
                    const isMax = count === maxCount && count > 0;
                    const barH = (count / maxCount) * BAR_MAX_H;
                    return (
                      <View key={i} style={s.barCol}>
                        <Text style={[s.barValue, { color: isMax ? colors.tertiary : colors.onSurfaceVariant }]}>{count || ''}</Text>
                        <View style={[s.bar, { height: Math.max(barH, 4), backgroundColor: isMax ? colors.tertiary : colors.tertiaryContainer }]} />
                        <Text style={[s.barLabel, { color: isMax ? colors.tertiary : colors.onSurfaceVariant, fontWeight: isMax ? '700' : '400' }]}>{weekdayLabels[i]}</Text>
                      </View>
                    );
                  })}
                </View>
              );
            })()}
          </View>
        )}

        {/* Full agenda button */}
        <TouchableOpacity
          style={[s.agendaBtn, { backgroundColor: colors.primary }, elev[1]]}
          onPress={() => router.push('/my-schedule' as any)}
          activeOpacity={0.7}
        >
          <MaterialCommunityIcons name="calendar-text" size={20} color={colors.onPrimary} />
          <Text style={[s.agendaBtnText, { color: colors.onPrimary }]}>{t('staffDashboard.viewAgenda')}</Text>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={20} color={colors.onPrimary} />
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>

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
  scrollContent: { paddingBottom: 100 },

  greetingSection: { paddingHorizontal: 20, paddingTop: 16, paddingBottom: 4 },
  greeting: { fontFamily: 'Cormorant-SemiBold', fontSize: 26, fontWeight: '600' },
  dateLabel: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2, textTransform: 'capitalize' },

  salonBanner: { flexDirection: 'row', alignItems: 'center', marginHorizontal: 16, marginTop: 12, padding: 12, borderRadius: 14, gap: 10 },
  salonIconCircle: { width: 34, height: 34, borderRadius: 17, alignItems: 'center', justifyContent: 'center' },
  salonName: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', flex: 1 },

  nextCard: { marginHorizontal: 16, marginTop: 16, padding: 16, borderRadius: 16 },
  nextCardTop: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 12 },
  nextLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  nextIn: { fontFamily: 'Manrope-Bold', fontSize: 12, fontWeight: '700' },
  nextCardBody: { flexDirection: 'row', alignItems: 'center' },
  nextClient: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  nextService: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2 },

  kpiGrid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 12, marginTop: 16, gap: 8 },
  kpiCard: { width: '47%', flexGrow: 1, padding: 14, borderRadius: 14, alignItems: 'flex-start' },
  kpiIconCircle: { width: 32, height: 32, borderRadius: 16, alignItems: 'center', justifyContent: 'center', marginBottom: 8 },
  kpiValue: { fontFamily: 'Cormorant-Bold', fontSize: 28, fontWeight: '700', lineHeight: 32 },
  kpiLabel: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },

  section: { marginHorizontal: 16, marginTop: 16, borderRadius: 16, overflow: 'hidden' },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 16, paddingTop: 16, paddingBottom: 10 },
  sectionTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600', flex: 1 },
  sectionCount: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700' },
  sectionEmpty: { alignItems: 'center', paddingVertical: 24, gap: 8 },
  sectionEmptyText: { fontFamily: 'Manrope-Regular', fontSize: 14 },

  // Timeline
  timelineRow: { flexDirection: 'row', paddingHorizontal: 16, paddingVertical: 12 },
  timeCol: { width: 46, alignItems: 'flex-end', paddingEnd: 10 },
  timeStart: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  timeEnd: { fontFamily: 'Manrope-Regular', fontSize: 11, marginTop: 2 },
  dotCol: { width: 16, alignItems: 'center', paddingTop: 4 },
  dot: { width: 8, height: 8, borderRadius: 4 },
  dotLine: { width: 1.5, flex: 1, marginTop: 4 },
  timelineContent: { flex: 1, paddingStart: 8 },
  timelineTopRow: { flexDirection: 'row', alignItems: 'center' },
  timelineClient: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  timelineService: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 1 },
  statusBadge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 999 },
  statusText: { fontFamily: 'Manrope-SemiBold', fontSize: 10, fontWeight: '600' },

  // Preview
  previewRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingHorizontal: 16, paddingVertical: 10 },
  previewDay: { fontFamily: 'Manrope-Bold', fontSize: 12, fontWeight: '700', width: 70 },
  previewClient: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  previewMeta: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 1 },

  // Donut chart
  donutContainer: { alignItems: 'center', paddingVertical: 12 },
  donutLegend: { paddingHorizontal: 16, paddingBottom: 14, gap: 6 },
  legendItem: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  legendDot: { width: 10, height: 10, borderRadius: 5 },
  legendName: { fontFamily: 'Manrope-Regular', fontSize: 13, flex: 1 },
  legendPct: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },

  // Bar chart
  barChartContainer: { flexDirection: 'row', alignItems: 'flex-end', justifyContent: 'space-around', paddingHorizontal: 16, paddingBottom: 16, paddingTop: 4 },
  barCol: { alignItems: 'center', flex: 1, gap: 4 },
  bar: { width: 20, borderRadius: 4 },
  barValue: { fontFamily: 'Manrope-SemiBold', fontSize: 11, fontWeight: '600', height: 16 },
  barLabel: { fontFamily: 'Manrope-Regular', fontSize: 11 },

  agendaBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, marginHorizontal: 16, marginTop: 20, paddingVertical: 14, borderRadius: 999 },
  agendaBtnText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },

  emptyCenter: { alignItems: 'center', justifyContent: 'center', paddingVertical: 64, gap: 12 },
  emptyTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600', textAlign: 'center' },
  emptyHint: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', paddingHorizontal: 32 },

  toast: { position: 'absolute', bottom: 40, left: 24, right: 24, paddingVertical: 14, paddingHorizontal: 20, borderRadius: 12, alignItems: 'center' },
  toastText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
