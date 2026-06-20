import { useEffect, useState, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, RefreshControl, FlatList, I18nManager, Modal, Pressable, TextInput } from 'react-native';

import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { LinearGradient } from 'expo-linear-gradient';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { salonsApi } from '../../src/api/salons';
import { socialApi } from '../../src/api/social';
import { queueApi } from '../../src/api/queue';
import { reviewsApi } from '../../src/api/reviews';
import { bookingsApi } from '../../src/api/bookings';
import { Avatar, RatingStars, ServiceImageStack } from '../../src/components/common';
import { PrimaryButton, OutlineButton } from '../../src/components/ui';
import { LoadingState, ErrorState } from '../../src/components/lists';
import { useToast } from '../../src/contexts/ToastContext';
import { usePermissions } from '../../src/hooks/usePermissions';

import { PostCard } from '../../src/components/social';
import { Salon, SalonService, StaffMember, Review, SalonReviewStats, QueueStatusResponse, PostResponse, BookingResponse, BookingStatus, SERVICE_CATEGORY_META } from '../../src/types';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';
import { navigateToProfile } from '../../src/utils/navigateToProfile';

type SalonTab = 'services' | 'team' | 'reviews' | 'posts' | 'info';
const TAB_KEYS: { key: SalonTab; i18nKey: string }[] = [
  { key: 'services', i18nKey: 'salon.services' }, { key: 'team', i18nKey: 'salon.team' },
  { key: 'reviews', i18nKey: 'review.reviewsTitle' }, { key: 'posts', i18nKey: 'salon.posts' }, { key: 'info', i18nKey: 'salon.info' },
];

export default function SalonDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();
  const { can } = usePermissions(id);

  const [salon, setSalon] = useState<Salon | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<SalonTab>('services');
  const [services, setServices] = useState<SalonService[]>([]);
  const [staff, setStaff] = useState<StaffMember[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [reviewStats, setReviewStats] = useState<SalonReviewStats | null>(null);
  const [queueStatus, setQueueStatus] = useState<QueueStatusResponse | null>(null);
  const [isJoining, setIsJoining] = useState(false);
  const [isLeaving, setIsLeaving] = useState(false);
  const [isFollowing, setIsFollowing] = useState(false);
  const [followersCount, setFollowersCount] = useState(0);
  const [salonPosts, setSalonPosts] = useState<PostResponse[]>([]);
  const [isLoadingPosts, setIsLoadingPosts] = useState(false);
  // L7: reply to review + write review
  const { showToast } = useToast();
  const [eligibleBooking, setEligibleBooking] = useState<BookingResponse | null>(null);
  const [replyingToId, setReplyingToId] = useState<string | null>(null);
  const [replyText, setReplyText] = useState('');
  const [isReplying, setIsReplying] = useState(false);

  const [isTogglingFollow, setIsTogglingFollow] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const loadSalon = useCallback(async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      const s = await salonsApi.getSalonById(id);
      setSalon(s);
      setIsFollowing(s.isFollowedByCurrentUser ?? false);
      setFollowersCount(s.followersCount ?? 0);
      const [svcR, staffR, revR, statsR, qR] = await Promise.allSettled([
        salonsApi.getSalonServices(id), salonsApi.getSalonStaff(id),
        reviewsApi.getAllSalonReviews(id), reviewsApi.getSalonReviewStats(id), queueApi.getQueueStatus(id),
      ]);
      if (svcR.status === 'fulfilled') setServices(svcR.value);
      if (staffR.status === 'fulfilled') setStaff(staffR.value);
      if (revR.status === 'fulfilled') setReviews(revR.value);
      if (statsR.status === 'fulfilled') setReviewStats(statsR.value);
      if (qR.status === 'fulfilled') setQueueStatus(qR.value);

      // Check if current user has a completed booking eligible for review
      if (user) {
        try {
          const bookings = await bookingsApi.getUserBookings(user.id);
          const completedForSalon = bookings.filter(
            (b) => b.salonId === id && b.status === BookingStatus.COMPLETED,
          );
          // Find one without a review already
          const reviewedBookingIds = new Set(
            (revR.status === 'fulfilled' ? revR.value : [])
              .map((r) => r.bookingId)
              .filter(Boolean),
          );
          const eligible = completedForSalon.find((b) => !reviewedBookingIds.has(b.id));
          setEligibleBooking(eligible ?? null);
        } catch {
          // Non-blocking — user just won't see the write review button
        }
      }
    } catch (e: any) { setError(e?.message || t('common.states.error')); } finally { setIsLoading(false); }
  }, [id]);

  useEffect(() => { loadSalon(); }, [loadSalon]);
  useEffect(() => {
    if (!id) return;
    const interval = setInterval(async () => { try { setQueueStatus(await queueApi.getQueueStatus(id)); } catch {} }, 30000);
    return () => clearInterval(interval);
  }, [id]);

  const onRefresh = async () => { setRefreshing(true); await loadSalon(); setRefreshing(false); };

  const handleToggleFollow = async () => {
    if (!id || isTogglingFollow) return;
    setIsTogglingFollow(true);
    try {
      if (isFollowing) { await socialApi.unfollowSalon(id); setFollowersCount(c => Math.max(0, c - 1)); }
      else { await socialApi.followSalon(id); setFollowersCount(c => c + 1); }
      setIsFollowing(!isFollowing);
    } catch {} finally { setIsTogglingFollow(false); }
  };

  const handleJoinQueue = async () => {
    if (!id || !user || isJoining) return;
    setIsJoining(true);
    try {
      await queueApi.joinQueue(id, { salonId: id, clientId: user.id });
      setQueueStatus(await queueApi.getQueueStatus(id));
    } catch (e: any) { Alert.alert(t('common.states.error'), e?.response?.data?.message || t('common.states.error')); }
    finally { setIsJoining(false); }
  };

  const isOwner = user?.id === salon?.ownerId;

  // Check if current user already has a salon review (booking=null) on this salon
  const hasExistingSalonReview = user
    ? reviews.some((r) => r.clientId === user.id && !r.bookingId)
    : true; // not logged in = hide button

  // Load posts when tab selected
  useEffect(() => {
    if (selectedTab !== 'posts' || !id) return;
    let ignore = false;
    const load = async () => {
      setIsLoadingPosts(true);
      try {
        const data = await socialApi.getPostsBySalon(id, { page: 0, size: 20 });
        if (!ignore) setSalonPosts(data.content || []);
      } catch {} finally {
        if (!ignore) setIsLoadingPosts(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, [selectedTab, id]);


  const handleLeaveQueue = async () => {
    if (!id || !user || isLeaving) return;
    setIsLeaving(true);
    try {
      const myEntry = queueStatus?.entries?.find(e => e.clientId === user.id && e.status === 'WAITING');
      if (!myEntry) return;
      await queueApi.leaveQueue(id, { entryId: myEntry.entryId });
      setQueueStatus(await queueApi.getQueueStatus(id));
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || t('common.states.error'));
    } finally {
      setIsLeaving(false);
    }
  };

  const isInQueue = queueStatus?.entries?.some(e => e.clientId === user?.id && e.status === 'WAITING');

  if (isLoading) return <LoadingState />;
  if (error || !salon) return <ErrorState message={error || t('salon.notFound')} onRetry={loadSalon} />;

  const queueSize = queueStatus?.entries?.filter(e => e.status === 'WAITING').length ?? 0;
  const avgRating = reviewStats?.averageRating ?? 0;

  const canReply = can('review.reply');

  const handleReply = async () => {
    if (!replyingToId || !replyText.trim() || !id) return;
    setIsReplying(true);
    try {
      const updated = await reviewsApi.replyToReview(id, replyingToId, replyText.trim());
      setReviews(prev => prev.map(r => r.id === replyingToId ? updated : r));
      setReplyingToId(null);
      setReplyText('');
      showToast(t('review.replySuccess'), 'success');
    } catch (err: any) {
      console.warn('[salon] replyToReview failed:', err?.message ?? err);
      showToast(t('review.replyError'), 'error');
    } finally {
      setIsReplying(false);
    }
  };
  const totalReviews = reviewStats?.totalReviews ?? 0;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      <ScrollView style={{ flex: 1 }} showsVerticalScrollIndicator={false} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}>
        {/* Cover */}
        <View style={s.coverWrap}>
          {salon.coverPhotoUrl ? (
            <Image source={{ uri: resolveMediaUrl(salon.coverPhotoUrl) }} style={s.coverImage} contentFit="cover" />
          ) : (
            <View style={[s.coverImage, { backgroundColor: colors.primary }]} />
          )}
          {/* design-fixed — editorial cover gradient overlay */}
          <LinearGradient
            colors={['rgba(40,23,51,0.4)', 'transparent', 'transparent', 'rgba(40,23,51,0.25)']}
            locations={[0, 0.3, 0.6, 1]}
            style={StyleSheet.absoluteFill}
          />
          <View style={s.coverActions}>
            <TouchableOpacity style={s.coverBtn} onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')}>
              <MaterialCommunityIcons name={I18nManager.isRTL ? "arrow-right" : "arrow-left"} size={24} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
            <View style={{ flex: 1 }} />
            <TouchableOpacity style={s.coverBtn}>
              <MaterialCommunityIcons name="share-variant" size={22} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
            <TouchableOpacity style={s.coverBtn}>
              <MaterialCommunityIcons name="heart" size={22} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
          </View>
        </View>

        {/* Identity sheet */}
        <View style={[s.sheet, { backgroundColor: colors.surface }]}>
          <View style={s.identityRow}>
            <View style={{ flex: 1 }}>
              <Text style={[s.salonName, { color: colors.onSurface }]}>{salon.name}</Text>
              <View style={s.locationRow}>
                <MaterialCommunityIcons name="map-marker" size={16} color={colors.onSurfaceVariant} />
                <Text style={[s.locationText, { color: colors.onSurfaceVariant }]}>{salon.address}, {salon.city}</Text>
              </View>
            </View>
            <View style={[s.ratingBadge, { backgroundColor: colors.tertiaryContainer }]}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 3 }}>
                <MaterialCommunityIcons name="star" size={18} color={colors.tertiary} />
                <Text style={[s.ratingValue, { color: colors.onTertiaryContainer }]}>{avgRating.toFixed(1)}</Text>
              </View>
              <Text style={[s.ratingCount, { color: colors.onTertiaryContainer }]}>{t('review.totalReviews', { count: totalReviews })}</Text>
            </View>
          </View>

          {/* Queue banner */}
          {queueStatus && (
            <View style={[s.queueBanner, { backgroundColor: colors.successContainer }]}>
              <View style={[s.queueDot, { backgroundColor: colors.success }]} />
              <Text style={[s.queueText, { color: colors.onSuccessContainer }]}>{t('salon.queueOpen', { minutes: queueStatus.estimatedWaitForNew || 15 })}</Text>
              <Text style={[s.queueCount, { color: colors.success }]}>{t('salon.queueWaiting', { count: queueSize })}</Text>
              {isInQueue && (
                <TouchableOpacity
                  style={[s.queueLeaveBtn, { borderColor: colors.onSuccessContainer }]}
                  onPress={handleLeaveQueue}
                  disabled={isLeaving}
                >
                  <Text style={[s.queueLeaveText, { color: colors.onSuccessContainer }]}>
                    {isLeaving ? t('salon.queueLeaving') : t('salon.leaveQueue')}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          )}

          {/* Tabs */}
          <View style={[s.tabRow, { borderBottomColor: colors.outlineVariant }]}>
            {TAB_KEYS.map((tab) => (
              <TouchableOpacity key={tab.key} onPress={() => setSelectedTab(tab.key)} style={s.tab}>
                <Text style={[s.tabLabel, { color: selectedTab === tab.key ? colors.primary : colors.onSurfaceVariant }]}>{t(tab.i18nKey)}</Text>
                {selectedTab === tab.key && <View style={[s.tabIndicator, { backgroundColor: colors.primary }]} />}
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Tab content */}
        <View style={[s.tabContent, { backgroundColor: colors.surface }]}>
          {selectedTab === 'services' && services.length === 0 && (
            <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>{t('salon.noServices')}</Text>
          )}
          {selectedTab === 'services' && services.map((svc, i) => {
            const catMeta = SERVICE_CATEGORY_META[svc.category];
            return (
              <View key={svc.id} style={[s.serviceItem, i < services.length - 1 && [s.serviceItemBorder, { borderBottomColor: colors.outlineVariant }]]}>
                <ServiceImageStack imageUrls={svc.imageUrls || []} category={svc.category} size={48} colors={colors} />
                <View style={{ flex: 1 }}>
                  <Text style={[s.serviceCategory, { color: colors.secondary }]}>{svc.categoryLabel || svc.category}</Text>
                  <Text style={[s.serviceName, { color: colors.onSurface }]}>{svc.name}</Text>
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: 5 }}>
                    <MaterialCommunityIcons name="clock-outline" size={15} color={colors.onSurfaceVariant} />
                    <Text style={[s.serviceDuration, { color: colors.onSurfaceVariant }]}>{svc.formattedDuration || `${svc.durationMinutes} min`}</Text>
                    <Text style={{ color: colors.outlineVariant, marginHorizontal: 4 }}>·</Text>
                    <Text style={[s.servicePrice, { color: colors.onSurface }]}>{svc.price} FCFA</Text>
                  </View>
                </View>
                <TouchableOpacity style={[s.reserveBtn, { borderColor: colors.primary, backgroundColor: colors.surface }]} onPress={() => router.push(`/booking/new?salonId=${id}&serviceId=${svc.id}`)}>
                  <Text style={[s.reserveBtnText, { color: colors.primary }]}>{t('salon.book')}</Text>
                </TouchableOpacity>
              </View>
            );
          })}

          {selectedTab === 'team' && staff.length === 0 && (
            <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>{t('salon.noTeam')}</Text>
          )}
          {selectedTab === 'team' && staff.map((m) => (
            <TouchableOpacity
              key={m.id}
              style={[s.staffItem, { borderBottomColor: colors.outlineVariant }]}
              onPress={() => navigateToProfile(m.role === 'owner' ? 'salon_owner' : 'hairstylist', m.userId)}
              activeOpacity={0.7}
            >
              <Avatar initials={`${m.userFirstName?.[0] || ''}${m.userLastName?.[0] || ''}`} size={44} ring tone={m.role === 'owner' ? 'primary' : 'secondary'} imageUrl={m.userAvatarUrl} />
              <View style={{ flex: 1 }}>
                <Text style={[s.staffName, { color: colors.onSurface }]}>{m.userFirstName} {m.userLastName}</Text>
                <Text style={[s.staffRole, { color: colors.primary }]}>{t(`salon.roles.${m.role}`)}</Text>
                {m.specialtyLabels?.length > 0 && (
                  <Text style={[s.staffSpecialty, { color: colors.onSurfaceVariant }]}>{m.specialtyLabels.join(', ')}</Text>
                )}
              </View>
              <MaterialCommunityIcons name={I18nManager.isRTL ? "chevron-left" : "chevron-right"} size={20} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          ))}

          {selectedTab === 'reviews' && (
            <>
              {reviewStats && (
                <View style={[s.statsCard, { backgroundColor: colors.tertiaryContainer }]}>
                  <Text style={[s.statsRating, { color: colors.onTertiaryContainer }]}>{avgRating.toFixed(1)}</Text>
                  <RatingStars value={avgRating} size={20} />
                  <Text style={[s.statsCount, { color: colors.onTertiaryContainer }]}>{t('review.totalReviews', { count: totalReviews })}</Text>
                  {/* Separated ratings */}
                  {(reviewStats.verifiedCount > 0 || reviewStats.generalCount > 0) && (
                    <View style={s.statsBreakdown}>
                      {reviewStats.verifiedCount > 0 && (
                        <View style={s.statsBreakdownRow}>
                          <MaterialCommunityIcons name="check-decagram" size={14} color={colors.tertiary} />
                          <Text style={[s.statsBreakdownText, { color: colors.onTertiaryContainer }]}>
                            {t('review.verifiedRating', { rating: reviewStats.verifiedAverage.toFixed(1), count: reviewStats.verifiedCount })}
                          </Text>
                        </View>
                      )}
                      {reviewStats.generalCount > 0 && (
                        <View style={s.statsBreakdownRow}>
                          <MaterialCommunityIcons name="account-outline" size={14} color={colors.onTertiaryContainer} />
                          <Text style={[s.statsBreakdownText, { color: colors.onTertiaryContainer }]}>
                            {t('review.generalRating', { rating: reviewStats.generalAverage.toFixed(1), count: reviewStats.generalCount })}
                          </Text>
                        </View>
                      )}
                    </View>
                  )}
                </View>
              )}
              {/* Avis-reservation button (existing) */}
              {eligibleBooking && salon && (
                <TouchableOpacity
                  style={[s.writeReviewBtn, { backgroundColor: colors.tertiary }]}
                  onPress={() => router.push(
                    `/create-review?salonId=${id}&salonName=${encodeURIComponent(salon.name)}&bookingId=${eligibleBooking.id}&serviceName=${encodeURIComponent(eligibleBooking.serviceName)}`
                  )}
                >
                  <MaterialCommunityIcons name="pencil-outline" size={18} color={colors.onTertiary} />
                  <Text style={[s.writeReviewBtnText, { color: colors.onTertiary }]}>{t('review.writeReview')}</Text>
                </TouchableOpacity>
              )}
              {/* Avis-salon button (new — for any logged-in non-owner without existing salon review) */}
              {user && !isOwner && !hasExistingSalonReview && salon && (
                <TouchableOpacity
                  style={[s.writeReviewBtn, { backgroundColor: colors.surface, borderWidth: 1.5, borderColor: colors.tertiary }]}
                  onPress={() => router.push(
                    `/create-review?salonId=${id}&salonName=${encodeURIComponent(salon.name)}`
                  )}
                >
                  <MaterialCommunityIcons name="star-outline" size={18} color={colors.tertiary} />
                  <Text style={[s.writeReviewBtnText, { color: colors.tertiary }]}>{t('review.writeSalonReview')}</Text>
                </TouchableOpacity>
              )}
              {reviews.map((rev) => (
                <View key={rev.id} style={[s.reviewItem, { borderBottomColor: colors.outlineVariant }]}>
                  <TouchableOpacity style={{ flexDirection: 'row', gap: 10, marginBottom: 8 }} onPress={() => navigateToProfile('client', rev.clientId)} activeOpacity={0.7}>
                    <Avatar initials={rev.clientName?.[0] || 'U'} size={40} tone="tertiary" />
                    <View>
                      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                        <Text style={[s.reviewAuthor, { color: colors.onSurface }]}>{rev.clientName}</Text>
                        {rev.isVerified && (
                          <View style={[s.verifiedBadge, { backgroundColor: colors.tertiaryContainer }]}>
                            <MaterialCommunityIcons name="check-decagram" size={11} color={colors.tertiary} />
                            <Text style={[s.verifiedText, { color: colors.tertiary }]}>{t('review.verifiedBadge')}</Text>
                          </View>
                        )}
                      </View>
                      <RatingStars value={rev.rating} size={14} />
                    </View>
                  </TouchableOpacity>
                  {rev.content && <Text style={[s.reviewContent, { color: colors.onSurface }]}>{rev.content}</Text>}

                  {/* L7: salon reply */}
                  {rev.responseSalon ? (
                    <View style={[s.replyBlock, { backgroundColor: colors.surfaceContainerHigh, borderStartColor: colors.tertiary }]}>
                      <Text style={[s.replyHeader, { color: colors.tertiary }]}>
                        {t('review.replyFrom', { salon: rev.salonName })}
                        {rev.responseByName ? (' \u00B7 ' + t('social.byAuthor', { name: rev.responseByName })) : ''}
                      </Text>
                      <Text style={[s.replyText, { color: colors.onSurface }]}>{rev.responseSalon}</Text>
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
              ))}
            </>
          )}

          {selectedTab === 'posts' && (
            isLoadingPosts ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 20 }} />
            ) : salonPosts.length === 0 ? (
              <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>{t('salon.noPosts')}</Text>
            ) : (
              <>
                {/* S6 : l'onglet est un aperçu (page 0) — la liste complète (filtres,
                    infinite scroll, actions complètes) vit sur l'écran dédié */}
                <TouchableOpacity style={s.seeAllRow} onPress={() => router.push(`/salon/${id}/posts`)}>
                  <Text style={[s.seeAllText, { color: colors.primary }]}>{t('common.actions.seeAll')}</Text>
                  <MaterialCommunityIcons name={I18nManager.isRTL ? "arrow-left" : "arrow-right"} size={18} color={colors.primary} />
                </TouchableOpacity>
                {salonPosts.map((post) => (
                  <PostCard
                    key={post.id}
                    post={post}
                    currentUserId={user?.id}
                    onProfilePress={() => navigateToProfile(post.authorUserType, post.authorId)}
                    onPress={() => router.push(`/post/${post.id}`)}
                    onComment={() => router.push(`/comments/${post.id}`)}
                  />
                ))}
              </>
            )
          )}

          {selectedTab === 'info' && (
            <View style={{ gap: 12 }}>
              <Text style={[s.infoLabel, { color: colors.secondary }]}>{t('salon.address')}</Text>
              <Text style={[s.infoValue, { color: colors.onSurface }]}>{salon.address}, {salon.postalCode} {salon.city}</Text>
              {salon.description && <>
                <Text style={[s.infoLabel, { color: colors.secondary }]}>{t('salon.description')}</Text>
                <Text style={[s.infoValue, { color: colors.onSurface }]}>{salon.description}</Text>
              </>}
            </View>
          )}
        </View>
      </ScrollView>

      {/* Floating action bar */}
      <LinearGradient colors={['transparent', colors.surface]} locations={[0, 0.28]} style={s.floatingBar}>
        {isOwner ? (
          <>
            <PrimaryButton icon="calendar-check" onPress={() => router.push(`/owner-bookings?salonId=${id}`)} style={{ ...s.barBtn, ...s.bookBtnShadow }}>
              {t('salon.reservations')}
            </PrimaryButton>
            <OutlineButton icon="account-group" onPress={() => router.push(`/queue-management?salonId=${id}`)} style={s.barBtn}>
              {t('salon.queue')}
            </OutlineButton>
          </>
        ) : (
          <>
            <OutlineButton icon="account-plus" onPress={handleToggleFollow} style={s.barBtn}>
              {isFollowing ? t('common.states.following') : t('common.actions.follow')}
            </OutlineButton>
            <PrimaryButton icon="calendar-month" onPress={() => router.push(`/booking/new?salonId=${id}`)} style={{ ...s.barBtnWide, ...s.bookBtnShadow }}>
              {t('salon.bookService')}
            </PrimaryButton>
          </>
        )}
      </LinearGradient>

      {/* L7: Reply modal */}
      <Modal visible={!!replyingToId} transparent animationType="fade" onRequestClose={() => setReplyingToId(null)}>
        <Pressable style={s.replyOverlay} onPress={() => setReplyingToId(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[s.replyCard, { backgroundColor: colors.surfaceContainerHighest, borderColor: colors.outlineVariant }]}>
            <Text style={[s.replyCardTitle, { color: colors.onSurface }]}>{t('review.replyTitle')}</Text>
            <TextInput
              style={[s.replyInput, { backgroundColor: colors.surface, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              placeholder={t('review.replyPlaceholder')}
              placeholderTextColor={colors.onSurfaceVariant}
              value={replyText}
              onChangeText={setReplyText}
              multiline
              textAlignVertical="top"
              maxLength={2000}
            />
            <View style={{ flexDirection: 'row', justifyContent: 'flex-end', gap: 12, marginTop: 12 }}>
              <TouchableOpacity onPress={() => setReplyingToId(null)} style={[s.replyActionBtn, { borderColor: colors.outline }]}>
                <Text style={[s.replyActionText, { color: colors.onSurfaceVariant }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleReply}
                disabled={isReplying || !replyText.trim()}
                style={[s.replyActionBtn, { backgroundColor: replyText.trim() ? colors.tertiary : colors.surfaceContainerHigh }]}
              >
                {isReplying ? (
                  <ActivityIndicator size="small" color={colors.onTertiary} />
                ) : (
                  <Text style={[s.replyActionText, { color: replyText.trim() ? colors.onTertiary : colors.onSurfaceVariant }]}>{t('review.replySubmit')}</Text>
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
  container: { flex: 1, position: 'relative' },
  // Cover
  coverWrap: { position: 'relative', height: 230 },
  coverImage: { width: '100%', height: 230 },
  coverActions: { position: 'absolute', top: 12, start: 8, end: 8, flexDirection: 'row', alignItems: 'center' },
  coverBtn: { width: 44, height: 44, borderRadius: 22, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', marginStart: 4 }, // design-fixed

  // Sheet
  sheet: { borderTopLeftRadius: 24, borderTopRightRadius: 24, marginTop: -22, paddingTop: 22, paddingHorizontal: 20 },
  identityRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 },
  salonName: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 30, fontWeight: '600', lineHeight: 30.6 },
  locationRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 8 },
  locationText: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  ratingBadge: { alignItems: 'center', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 8 },
  ratingValue: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  ratingCount: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  // Queue
  queueBanner: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 16, padding: 14, borderRadius: 12 },
  queueDot: { width: 8, height: 8, borderRadius: 4 },
  queueText: { flex: 1, fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  queueCount: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  queueLeaveBtn: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999, borderWidth: 1 },
  queueLeaveText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', paddingVertical: 24 },
  seeAllRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'flex-end', gap: 4, paddingVertical: 10 },
  seeAllText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  // Tabs
  tabRow: { flexDirection: 'row', gap: 22, marginTop: 18, borderBottomWidth: 1 },
  tab: { paddingVertical: 10, position: 'relative' },
  tabLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  tabIndicator: { position: 'absolute', bottom: -1, start: 0, end: 0, height: 3, borderRadius: 3 },
  // Tab content
  tabContent: { paddingHorizontal: 20, paddingTop: 8, paddingBottom: 120 },
  // Services
  serviceItem: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 16 },
  serviceItemBorder: { borderBottomWidth: 1 },
  serviceCategory: { fontFamily: 'Manrope-Bold', fontSize: 10.5, fontWeight: '800', letterSpacing: 1 },
  serviceName: { fontFamily: 'Manrope-SemiBold', fontSize: 15.5, fontWeight: '600', marginTop: 3 },
  serviceDuration: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  servicePrice: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reserveBtn: { height: 40, paddingHorizontal: 18, borderRadius: 999, borderWidth: 1, justifyContent: 'center' },
  reserveBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13.5, fontWeight: '700' },
  // Staff
  staffItem: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 12, borderBottomWidth: 1 },
  staffName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  staffRole: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', marginTop: 2 },
  staffSpecialty: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  // Reviews
  statsCard: { alignItems: 'center', padding: 20, marginVertical: 12, borderRadius: 16, gap: 4 },
  statsRating: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 36, fontWeight: '600' },
  statsCount: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  statsBreakdown: { flexDirection: 'row', gap: 16, marginTop: 8 },
  statsBreakdownRow: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  statsBreakdownText: { fontFamily: 'Manrope-Regular', fontSize: 11.5 },
  verifiedBadge: { flexDirection: 'row', alignItems: 'center', gap: 3, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 999 },
  verifiedText: { fontFamily: 'Manrope-SemiBold', fontSize: 10, fontWeight: '600' },
  writeReviewBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, paddingVertical: 12, borderRadius: 999, marginBottom: 12 },
  writeReviewBtnText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reviewItem: { paddingVertical: 14, borderBottomWidth: 1 },
  reviewAuthor: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reviewContent: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20 },
  replyBlock: { marginTop: 10, marginStart: 12, padding: 12, borderRadius: 12, borderStartWidth: 3 },
  replyHeader: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', marginBottom: 4 },
  replyText: { fontFamily: 'Manrope-Regular', fontSize: 13, lineHeight: 19 },
  replyBtn: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 8, alignSelf: 'flex-start', paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999, borderWidth: 1 },
  replyBtnText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  replyOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', paddingHorizontal: 24 },
  replyCard: { borderRadius: 20, borderWidth: 1, padding: 20 },
  replyCardTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 17, fontWeight: '600', marginBottom: 12 },
  replyInput: { minHeight: 100, borderRadius: 12, padding: 14, fontSize: 14, borderWidth: 1, fontFamily: 'Manrope-Regular' },
  replyActionBtn: { paddingVertical: 10, paddingHorizontal: 20, borderRadius: 999, borderWidth: 1 },
  replyActionText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  // Info
  infoLabel: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700', letterSpacing: 2, textTransform: 'uppercase' },
  infoValue: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20 },
  // Floating bar
  floatingBar: { position: 'absolute', start: 0, end: 0, bottom: 0, paddingHorizontal: 16, paddingTop: 14, paddingBottom: 16, flexDirection: 'row', gap: 10 },
  // Dans une row : flex (jamais width 100%, sinon débordement horizontal)
  barBtn: { flex: 1, minWidth: 0, paddingHorizontal: 12 },
  barBtnWide: { flex: 1.7, minWidth: 0, paddingHorizontal: 12 },
  bookBtnShadow: { shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.1, shadowRadius: 8, elevation: 3 }, // design-fixed
});
