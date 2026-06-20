import { useState, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  RefreshControl,
  I18nManager,
  ActivityIndicator,
  Modal,
  Pressable,
  TextInput,
} from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { useToast } from '../src/contexts/ToastContext';
import { reviewsApi } from '../src/api/reviews';
import { Review, SalonReviewStats } from '../src/types';
import { Avatar, RatingStars, AccessDenied } from '../src/components/common';
import { LoadingState, ErrorState } from '../src/components/lists';
import { usePermissions } from '../src/hooks/usePermissions';

type FilterKey = 'all' | 'unreplied' | 'replied';

export default function OwnerReviewsScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { showToast } = useToast();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);
  const canReply = can('review.reply');

  const [reviews, setReviews] = useState<Review[]>([]);
  const [stats, setStats] = useState<SalonReviewStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [filter, setFilter] = useState<FilterKey>('unreplied');

  // Reply state
  const [replyingToId, setReplyingToId] = useState<string | null>(null);
  const [replyText, setReplyText] = useState('');
  const [isReplying, setIsReplying] = useState(false);

  const loadData = useCallback(async () => {
    if (!salonId) return;
    try {
      const [revs, st] = await Promise.all([
        reviewsApi.getAllSalonReviews(salonId),
        reviewsApi.getSalonReviewStats(salonId),
      ]);
      setReviews(revs);
      setStats(st);
      setError(null);
    } catch (e: any) {
      setError(e?.message || t('common.states.error'));
    } finally {
      setIsLoading(false);
    }
  }, [salonId, t]);

  useFocusEffect(
    useCallback(() => {
      setIsLoading(true);
      loadData();
    }, [loadData]),
  );

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  const handleReply = async () => {
    if (!replyingToId || !replyText.trim() || !salonId) return;
    setIsReplying(true);
    try {
      const updated = await reviewsApi.replyToReview(salonId, replyingToId, replyText.trim());
      setReviews((prev) => prev.map((r) => (r.id === replyingToId ? updated : r)));
      setReplyingToId(null);
      setReplyText('');
      showToast(t('review.replySuccess'), 'success');
    } catch (err: any) {
      console.warn('[owner-reviews] replyToReview failed:', err?.message ?? err);
      showToast(t('review.replyError'), 'error');
    } finally {
      setIsReplying(false);
    }
  };

  // Derived
  const unrepliedCount = reviews.filter((r) => !r.responseSalon).length;
  const filtered = reviews.filter((r) => {
    if (filter === 'unreplied') return !r.responseSalon;
    if (filter === 'replied') return !!r.responseSalon;
    return true;
  });

  const FILTERS: { key: FilterKey; labelKey: string }[] = [
    { key: 'unreplied', labelKey: 'ownerReviews.filterUnreplied' },
    { key: 'replied', labelKey: 'ownerReviews.filterReplied' },
    { key: 'all', labelKey: 'ownerReviews.filterAll' },
  ];

  // Permission guard — hooks above
  if (permLoading || isLoading) return <LoadingState />;
  if (role === 'none') return <AccessDenied />;
  if (error) return <ErrorState message={error} onRetry={() => { setIsLoading(true); loadData(); }} />;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.topBar, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => (router.canGoBack() ? router.back() : router.replace('/(tabs)'))} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.topTitle, { color: colors.onSurface }]}>{t('review.reviewsTitle')}</Text>
        {unrepliedCount > 0 && (
          <View style={[s.badge, { backgroundColor: colors.error }]}>
            <Text style={[s.badgeText, { color: colors.onError }]}>{unrepliedCount}</Text>
          </View>
        )}
      </View>

      {/* Stats summary */}
      {stats && (
        <View style={[s.statsRow, { backgroundColor: colors.surface }]}>
          <View style={s.statItem}>
            <Text style={[s.statValue, { color: colors.onSurface }]}>{stats.averageRating.toFixed(1)}</Text>
            <RatingStars value={stats.averageRating} size={14} />
          </View>
          <View style={[s.statDivider, { backgroundColor: colors.outlineVariant }]} />
          <View style={s.statItem}>
            <Text style={[s.statValue, { color: colors.onSurface }]}>{stats.totalReviews}</Text>
            <Text style={[s.statLabel, { color: colors.onSurfaceVariant }]}>{t('ownerReviews.totalLabel')}</Text>
          </View>
          <View style={[s.statDivider, { backgroundColor: colors.outlineVariant }]} />
          <View style={s.statItem}>
            <Text style={[s.statValue, { color: unrepliedCount > 0 ? colors.error : colors.onSurface }]}>{unrepliedCount}</Text>
            <Text style={[s.statLabel, { color: colors.onSurfaceVariant }]}>{t('ownerReviews.unrepliedLabel')}</Text>
          </View>
        </View>
      )}

      {/* Filter chips */}
      <View style={s.filterRow}>
        {FILTERS.map((f) => {
          const active = filter === f.key;
          return (
            <TouchableOpacity
              key={f.key}
              style={[s.filterChip, active ? { backgroundColor: colors.primary } : { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.outlineVariant }]}
              onPress={() => setFilter(f.key)}
            >
              <Text style={[s.filterText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>
                {t(f.labelKey)}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Reviews list */}
      <ScrollView
        style={{ flex: 1 }}
        contentContainerStyle={s.listContent}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
      >
        {filtered.length === 0 ? (
          <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
            {filter === 'unreplied' ? t('ownerReviews.noUnreplied') : t('review.noReviews')}
          </Text>
        ) : (
          filtered.map((rev) => (
            <View key={rev.id} style={[s.reviewCard, { backgroundColor: colors.surface }]}>
              {/* Header: avatar + name + rating + verified badge */}
              <View style={s.reviewHeader}>
                <Avatar initials={rev.clientName?.[0] || 'U'} size={40} tone="tertiary" />
                <View style={{ flex: 1 }}>
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                    <Text style={[s.reviewAuthor, { color: colors.onSurface }]}>{rev.clientName}</Text>
                    {rev.isVerified && (
                      <View style={[s.verifiedBadge, { backgroundColor: colors.tertiaryContainer }]}>
                        <MaterialCommunityIcons name="check-decagram" size={11} color={colors.tertiary} />
                        <Text style={[s.verifiedText, { color: colors.tertiary }]}>{t('review.verifiedBadge')}</Text>
                      </View>
                    )}
                  </View>
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 2 }}>
                    <RatingStars value={rev.rating} size={13} />
                    {rev.createdAt && (
                      <Text style={[s.reviewDate, { color: colors.onSurfaceVariant }]}>
                        {new Date(rev.createdAt).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })}
                      </Text>
                    )}
                  </View>
                </View>
              </View>

              {/* Content */}
              {rev.title && <Text style={[s.reviewTitle, { color: colors.onSurface }]}>{rev.title}</Text>}
              {rev.content && <Text style={[s.reviewContent, { color: colors.onSurface }]}>{rev.content}</Text>}

              {/* Reply block or reply button */}
              {rev.responseSalon ? (
                <View style={[s.replyBlock, { backgroundColor: colors.surfaceContainerHigh, borderStartColor: colors.tertiary }]}>
                  <Text style={[s.replyHeader, { color: colors.tertiary }]}>
                    {t('review.replyFrom', { salon: rev.salonName })}
                    {rev.responseByName ? (' \u00B7 ' + rev.responseByName) : ''}
                  </Text>
                  <Text style={[s.replyContent, { color: colors.onSurface }]}>{rev.responseSalon}</Text>
                </View>
              ) : canReply ? (
                <TouchableOpacity
                  style={[s.replyBtn, { borderColor: colors.tertiary }]}
                  onPress={() => { setReplyingToId(rev.id); setReplyText(''); }}
                >
                  <MaterialCommunityIcons name="reply" size={16} color={colors.tertiary} />
                  <Text style={[s.replyBtnText, { color: colors.tertiary }]}>{t('review.replyButton')}</Text>
                </TouchableOpacity>
              ) : null}
            </View>
          ))
        )}
        <View style={{ height: 24 }} />
      </ScrollView>

      {/* Reply modal */}
      <Modal visible={!!replyingToId} transparent animationType="fade" onRequestClose={() => setReplyingToId(null)}>
        <Pressable style={s.modalOverlay} onPress={() => setReplyingToId(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[s.modalCard, { backgroundColor: colors.surfaceContainerHighest, borderColor: colors.outlineVariant }]}>
            <Text style={[s.modalTitle, { color: colors.onSurface }]}>{t('review.replyTitle')}</Text>
            <TextInput
              style={[s.modalInput, { backgroundColor: colors.surface, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              placeholder={t('review.replyPlaceholder')}
              placeholderTextColor={colors.onSurfaceVariant}
              value={replyText}
              onChangeText={setReplyText}
              multiline
              textAlignVertical="top"
              maxLength={2000}
            />
            <View style={{ flexDirection: 'row', justifyContent: 'flex-end', gap: 12, marginTop: 12 }}>
              <TouchableOpacity onPress={() => setReplyingToId(null)} style={[s.modalBtn, { borderColor: colors.outline }]}>
                <Text style={[s.modalBtnText, { color: colors.onSurfaceVariant }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleReply}
                disabled={isReplying || !replyText.trim()}
                style={[s.modalBtn, { backgroundColor: replyText.trim() ? colors.tertiary : colors.surfaceContainerHigh, borderColor: 'transparent' }]}
              >
                {isReplying ? (
                  <ActivityIndicator size="small" color={colors.onTertiary} />
                ) : (
                  <Text style={[s.modalBtnText, { color: replyText.trim() ? colors.onTertiary : colors.onSurfaceVariant }]}>{t('review.replySubmit')}</Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  topBar: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  topTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  badge: { minWidth: 24, height: 24, borderRadius: 12, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 6 },
  badgeText: { fontFamily: 'Manrope-Bold', fontSize: 12, fontWeight: '700' },
  // Stats
  statsRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-around', paddingVertical: 14, paddingHorizontal: 16, marginHorizontal: 16, marginTop: 8, borderRadius: 16 },
  statItem: { alignItems: 'center', gap: 4 },
  statValue: { fontFamily: 'Manrope-Bold', fontSize: 20, fontWeight: '700' },
  statLabel: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  statDivider: { width: 1, height: 32 },
  // Filter
  filterRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 12 },
  filterChip: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 999 },
  filterText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  // List
  listContent: { paddingHorizontal: 16 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', paddingVertical: 32 },
  // Review card
  reviewCard: { borderRadius: 16, padding: 16, marginBottom: 10 },
  reviewHeader: { flexDirection: 'row', gap: 12, alignItems: 'flex-start' },
  reviewAuthor: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reviewDate: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  reviewTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', marginTop: 10 },
  reviewContent: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20, marginTop: 6 },
  verifiedBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 999 },
  verifiedText: { fontFamily: 'Manrope-SemiBold', fontSize: 10, fontWeight: '600' },
  // Reply block (existing reply)
  replyBlock: { marginTop: 12, padding: 12, borderRadius: 12, borderStartWidth: 3 },
  replyHeader: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', marginBottom: 4 },
  replyContent: { fontFamily: 'Manrope-Regular', fontSize: 13, lineHeight: 19 },
  // Reply button
  replyBtn: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 10, alignSelf: 'flex-start', paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999, borderWidth: 1 },
  replyBtnText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Modal
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', paddingHorizontal: 24 },
  modalCard: { borderRadius: 20, borderWidth: 1, padding: 20 },
  modalTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 17, fontWeight: '600', marginBottom: 12 },
  modalInput: { minHeight: 100, borderRadius: 12, padding: 14, fontSize: 14, borderWidth: 1, fontFamily: 'Manrope-Regular' },
  modalBtn: { paddingVertical: 10, paddingHorizontal: 20, borderRadius: 999, borderWidth: 1 },
  modalBtnText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
