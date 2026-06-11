import { useEffect, useState, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, RefreshControl, FlatList } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
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
import { Avatar, RatingStars } from '../../src/components/common';
import { PrimaryButton, OutlineButton } from '../../src/components/ui';
import { LoadingState, ErrorState } from '../../src/components/lists';
import { mediaApi } from '../../src/api/media';
import { PostCard } from '../../src/components/social';
import { Salon, SalonService, StaffMember, Review, SalonReviewStats, QueueStatusResponse, PostResponse } from '../../src/types';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';

type SalonTab = 'services' | 'team' | 'reviews' | 'posts' | 'info';
const TABS: { key: SalonTab; label: string }[] = [
  { key: 'services', label: 'Services' }, { key: 'team', label: 'Équipe' },
  { key: 'reviews', label: 'Avis' }, { key: 'posts', label: 'Posts' }, { key: 'info', label: 'Info' },
];

export default function SalonDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

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
  const [isUploadingCover, setIsUploadingCover] = useState(false);
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
    } catch (e: any) { setError(e?.message || 'Erreur'); } finally { setIsLoading(false); }
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
    } catch (e: any) { Alert.alert('Erreur', e?.response?.data?.message || 'Erreur'); }
    finally { setIsJoining(false); }
  };

  const isOwner = user?.id === salon?.ownerId;

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

  const handlePickCover = async () => {
    if (!isOwner || !id) return;
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (result.canceled || !result.assets[0]) return;
    setIsUploadingCover(true);
    try {
      const fileName = `salon_cover_${id}_${Date.now()}.jpg`;
      const path = await mediaApi.uploadImage(result.assets[0].uri, fileName);
      await salonsApi.updateSalonCoverPhoto(id, path);
      setSalon(prev => prev ? { ...prev, coverPhotoUrl: path } : prev);
    } catch {} finally {
      setIsUploadingCover(false);
    }
  };

  const handleLeaveQueue = async () => {
    if (!id || !user || isLeaving) return;
    setIsLeaving(true);
    try {
      const myEntry = queueStatus?.entries?.find(e => e.clientId === user.id && e.status === 'WAITING');
      if (!myEntry) return;
      await queueApi.leaveQueue(id, { entryId: myEntry.entryId });
      setQueueStatus(await queueApi.getQueueStatus(id));
    } catch (e: any) {
      Alert.alert('Erreur', e?.response?.data?.message || 'Erreur');
    } finally {
      setIsLeaving(false);
    }
  };

  const isInQueue = queueStatus?.entries?.some(e => e.clientId === user?.id && e.status === 'WAITING');

  if (isLoading) return <LoadingState />;
  if (error || !salon) return <ErrorState message={error || 'Salon introuvable'} onRetry={loadSalon} />;

  const queueSize = queueStatus?.entries?.filter(e => e.status === 'WAITING').length ?? 0;
  const avgRating = reviewStats?.averageRating ?? 0;
  const totalReviews = reviewStats?.totalReviews ?? 0;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      <ScrollView style={{ flex: 1 }} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}>
        {/* Cover */}
        <TouchableOpacity style={s.coverWrap} activeOpacity={isOwner ? 0.8 : 1} onPress={isOwner ? handlePickCover : undefined}>
          {salon.coverPhotoUrl ? (
            <Image source={{ uri: resolveMediaUrl(salon.coverPhotoUrl) }} style={s.coverImage} contentFit="cover" />
          ) : (
            <View style={[s.coverImage, { backgroundColor: colors.primary }]} />
          )}
          {isOwner && (
            <View style={[s.coverEditBadge, { backgroundColor: colors.primary }]}>
              {isUploadingCover ? (
                <ActivityIndicator size="small" color={colors.onPrimary} />
              ) : (
                <MaterialCommunityIcons name="camera" size={18} color={colors.onPrimary} />
              )}
            </View>
          )}
          {/* design-fixed — editorial cover gradient overlay */}
          <LinearGradient
            colors={['rgba(40,23,51,0.4)', 'transparent', 'transparent', 'rgba(40,23,51,0.25)']}
            locations={[0, 0.3, 0.6, 1]}
            style={StyleSheet.absoluteFill}
          />
          <View style={s.coverActions}>
            <TouchableOpacity style={s.coverBtn} onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')}>
              <MaterialCommunityIcons name="arrow-left" size={24} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
            <View style={{ flex: 1 }} />
            <TouchableOpacity style={s.coverBtn}>
              <MaterialCommunityIcons name="share-variant" size={22} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
            <TouchableOpacity style={s.coverBtn}>
              <MaterialCommunityIcons name="heart" size={22} color="#FFFFFF" />{/* design-fixed */}
            </TouchableOpacity>
          </View>
        </TouchableOpacity>

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
              <Text style={[s.ratingCount, { color: colors.onTertiaryContainer }]}>{totalReviews} avis</Text>
            </View>
          </View>

          {/* Queue banner */}
          {queueStatus && (
            <View style={[s.queueBanner, { backgroundColor: colors.successContainer }]}>
              <View style={[s.queueDot, { backgroundColor: colors.success }]} />
              <Text style={[s.queueText, { color: colors.onSuccessContainer }]}>File ouverte · ~{queueStatus.estimatedWaitForNew || 15} min d'attente</Text>
              <Text style={[s.queueCount, { color: colors.success }]}>{queueSize} en attente</Text>
              {isInQueue && (
                <TouchableOpacity
                  style={[s.queueLeaveBtn, { borderColor: colors.onSuccessContainer }]}
                  onPress={handleLeaveQueue}
                  disabled={isLeaving}
                >
                  <Text style={[s.queueLeaveText, { color: colors.onSuccessContainer }]}>
                    {isLeaving ? 'Sortie...' : 'Quitter'}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          )}

          {/* Tabs */}
          <View style={[s.tabRow, { borderBottomColor: colors.outlineVariant }]}>
            {TABS.filter(tab => tab.key !== 'team' || isOwner).map((tab) => (
              <TouchableOpacity key={tab.key} onPress={() => setSelectedTab(tab.key)} style={s.tab}>
                <Text style={[s.tabLabel, { color: selectedTab === tab.key ? colors.primary : colors.onSurfaceVariant }]}>{tab.label}</Text>
                {selectedTab === tab.key && <View style={[s.tabIndicator, { backgroundColor: colors.primary }]} />}
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Tab content */}
        <View style={[s.tabContent, { backgroundColor: colors.surface }]}>
          {selectedTab === 'services' && services.map((svc, i) => (
            <View key={svc.id} style={[s.serviceItem, i < services.length - 1 && [s.serviceItemBorder, { borderBottomColor: colors.outlineVariant }]]}>
              <View style={{ flex: 1 }}>
                <Text style={[s.serviceCategory, { color: colors.secondary }]}>{svc.categoryLabel || svc.category}</Text>
                <Text style={[s.serviceName, { color: colors.onSurface }]}>{svc.name}</Text>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: 5 }}>
                  <MaterialCommunityIcons name="clock-outline" size={15} color={colors.onSurfaceVariant} />
                  <Text style={[s.serviceDuration, { color: colors.onSurfaceVariant }]}>{svc.formattedDuration || `${svc.durationMinutes} min`}</Text>
                  <Text style={{ color: colors.outlineVariant, marginHorizontal: 4 }}>·</Text>
                  <Text style={[s.servicePrice, { color: colors.onSurface }]}>{svc.price} €</Text>
                </View>
              </View>
              <TouchableOpacity style={[s.reserveBtn, { borderColor: colors.primary, backgroundColor: colors.surface }]} onPress={() => router.push(`/booking/new?salonId=${id}&serviceId=${svc.id}`)}>
                <Text style={[s.reserveBtnText, { color: colors.primary }]}>Réserver</Text>
              </TouchableOpacity>
            </View>
          ))}

          {selectedTab === 'team' && staff.map((m) => (
            <View key={m.id} style={[s.staffItem, { borderBottomColor: colors.outlineVariant }]}>
              <Avatar initials={`${m.userFirstName?.[0] || ''}${m.userLastName?.[0] || ''}`} size={48} tone="secondary" imageUrl={m.userAvatarUrl} />
              <View style={{ flex: 1 }}>
                <Text style={[s.staffName, { color: colors.onSurface }]}>{m.userFirstName} {m.userLastName}</Text>
                <Text style={[s.staffSpecialty, { color: colors.onSurfaceVariant }]}>{m.specialties?.join(', ')}</Text>
              </View>
            </View>
          ))}

          {selectedTab === 'reviews' && (
            <>
              {reviewStats && (
                <View style={[s.statsCard, { backgroundColor: colors.tertiaryContainer }]}>
                  <Text style={[s.statsRating, { color: colors.onTertiaryContainer }]}>{avgRating.toFixed(1)}</Text>
                  <RatingStars value={avgRating} size={20} />
                  <Text style={[s.statsCount, { color: colors.onTertiaryContainer }]}>{totalReviews} avis</Text>
                </View>
              )}
              {reviews.map((rev) => (
                <View key={rev.id} style={[s.reviewItem, { borderBottomColor: colors.outlineVariant }]}>
                  <View style={{ flexDirection: 'row', gap: 10, marginBottom: 8 }}>
                    <Avatar initials={rev.clientName?.[0] || 'U'} size={40} tone="tertiary" />
                    <View>
                      <Text style={[s.reviewAuthor, { color: colors.onSurface }]}>{rev.clientName}</Text>
                      <RatingStars value={rev.rating} size={14} />
                    </View>
                  </View>
                  {rev.content && <Text style={[s.reviewContent, { color: colors.onSurface }]}>{rev.content}</Text>}
                </View>
              ))}
            </>
          )}

          {selectedTab === 'posts' && (
            isLoadingPosts ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 20 }} />
            ) : salonPosts.length === 0 ? (
              <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>Aucun post pour ce salon.</Text>
            ) : (
              salonPosts.map((post) => (
                <PostCard
                  key={post.id}
                  post={post}
                  currentUserId={user?.id}
                  onPress={() => router.push(`/post/${post.id}`)}
                  onComment={() => router.push(`/comments/${post.id}`)}
                />
              ))
            )
          )}

          {selectedTab === 'info' && (
            <View style={{ gap: 12 }}>
              <Text style={[s.infoLabel, { color: colors.secondary }]}>Adresse</Text>
              <Text style={[s.infoValue, { color: colors.onSurface }]}>{salon.address}, {salon.postalCode} {salon.city}</Text>
              {salon.description && <>
                <Text style={[s.infoLabel, { color: colors.secondary }]}>Description</Text>
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
            <PrimaryButton icon="calendar-check" onPress={() => router.push(`/owner-bookings?salonId=${id}`)} style={s.bookBtnShadow}>
              Réservations
            </PrimaryButton>
            <OutlineButton icon="account-group" onPress={() => router.push(`/queue-management?salonId=${id}`)}>
              File d'attente
            </OutlineButton>
          </>
        ) : (
          <>
            <OutlineButton icon="account-plus" onPress={handleToggleFollow}>
              {isFollowing ? 'Suivi' : 'Suivre'}
            </OutlineButton>
            <PrimaryButton icon="calendar-month" full onPress={() => router.push(`/booking/new?salonId=${id}`)} style={s.bookBtnShadow}>
              Réserver une prestation
            </PrimaryButton>
          </>
        )}
      </LinearGradient>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1, position: 'relative' },
  // Cover
  coverWrap: { position: 'relative', height: 230 },
  coverImage: { width: '100%', height: 230 },
  coverActions: { position: 'absolute', top: 12, left: 8, right: 8, flexDirection: 'row', alignItems: 'center' },
  coverBtn: { width: 44, height: 44, borderRadius: 22, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', marginLeft: 4 }, // design-fixed
  coverEditBadge: { position: 'absolute', bottom: 12, right: 12, width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
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
  // Tabs
  tabRow: { flexDirection: 'row', gap: 22, marginTop: 18, borderBottomWidth: 1 },
  tab: { paddingVertical: 10, position: 'relative' },
  tabLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  tabIndicator: { position: 'absolute', bottom: -1, left: 0, right: 0, height: 3, borderRadius: 3 },
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
  staffSpecialty: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  // Reviews
  statsCard: { alignItems: 'center', padding: 20, marginVertical: 12, borderRadius: 16, gap: 4 },
  statsRating: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 36, fontWeight: '600' },
  statsCount: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  reviewItem: { paddingVertical: 14, borderBottomWidth: 1 },
  reviewAuthor: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reviewContent: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20 },
  // Info
  infoLabel: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700', letterSpacing: 2, textTransform: 'uppercase' },
  infoValue: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20 },
  // Floating bar
  floatingBar: { position: 'absolute', left: 0, right: 0, bottom: 0, paddingHorizontal: 16, paddingTop: 14, paddingBottom: 16, flexDirection: 'row', gap: 10 },
  bookBtnShadow: { shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.1, shadowRadius: 8, elevation: 3 }, // design-fixed
});
