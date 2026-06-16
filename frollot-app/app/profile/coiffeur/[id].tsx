import { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Share,
  I18nManager,
  RefreshControl,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../../src/theme';
import { useAuthStore } from '../../../src/stores/authStore';
import { profilesApi } from '../../../src/api/profiles';
import { socialApi } from '../../../src/api/social';
import { CoiffeurProfileResponse, PostResponse } from '../../../src/types';
import { LoadingState, ErrorState } from '../../../src/components/lists';
import { PostCard } from '../../../src/components/social';
import { Chip } from '../../../src/components/ui';
import {
  ProfileHeader,
  ProfileTabBar,
  ProfileInfoCard,
  FollowButton,
} from '../../../src/components/profile';
import { navigateToProfile } from '../../../src/utils/navigateToProfile';
import { resolveMediaUrl } from '../../../src/utils/media';

type ProfileTab = 'posts' | 'portfolio' | 'reviews';

export default function CoiffeurProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  const [profile, setProfile] = useState<CoiffeurProfileResponse | null>(null);
  const [pinnedPosts, setPinnedPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<ProfileTab>('posts');
  const [refreshing, setRefreshing] = useState(false);

  const isOwnProfile = user?.id === id;

  const loadProfile = useCallback(async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      setError(null);
      const [profileRes, pinnedRes] = await Promise.allSettled([
        profilesApi.getCoiffeurProfile(id),
        socialApi.getPinnedPosts(id),
      ]);
      if (profileRes.status === 'fulfilled') {
        setProfile(profileRes.value);
      } else {
        setError((profileRes.reason as Error)?.message || t('common.states.error'));
      }
      if (pinnedRes.status === 'fulfilled') setPinnedPosts(pinnedRes.value);
    } catch (e: any) {
      setError(e?.message || t('common.states.error'));
    } finally {
      setIsLoading(false);
    }
  }, [id, t]);

  useEffect(() => { loadProfile(); }, [loadProfile]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadProfile();
    setRefreshing(false);
  }, [loadProfile]);

  const handleShare = useCallback(async () => {
    if (!profile) return;
    const name = [profile.firstName, profile.lastName].filter(Boolean).join(' ');
    try {
      await Share.share({ message: `${name} sur Frollot` });
    } catch {}
  }, [profile]);

  if (isLoading && !profile) {
    return <LoadingState />;
  }
  if (error && !profile) {
    return <ErrorState message={error} onRetry={loadProfile} />;
  }
  if (!profile) return null;

  const fullName = [profile.firstName, profile.lastName].filter(Boolean).join(' ') || '?';
  const mainSpecialty = profile.specialties.length > 0 ? profile.specialties[0] : undefined;
  const subtitle = [t('profile.type.hairstylist'), mainSpecialty].filter(Boolean).join(' · ');

  // Stats row
  const stats: { label: string; value: number }[] = [
    { label: t('profile.stats.followers'), value: profile.statistics.followersCount },
    { label: t('profile.stats.following'), value: profile.statistics.followingCount },
    { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
  ];
  if (profile.statistics.totalReviews > 0) {
    stats.push({
      label: `★ ${profile.statistics.averageRating.toFixed(1)}`,
      value: profile.statistics.totalReviews,
    });
  }

  // Tabs
  const tabs: { key: ProfileTab; label: string }[] = [
    { key: 'posts', label: t('profile.tabs.posts') },
    { key: 'portfolio', label: t('profile.tabs.portfolio') },
    { key: 'reviews', label: t('profile.tabs.reviews') },
  ];

  // Info card items (inline, no title)
  const infoCardItems: { icon: keyof typeof import('@expo/vector-icons').MaterialCommunityIcons.glyphMap; value: string }[] = [];
  if (profile.city) {
    infoCardItems.push({ icon: 'map-marker-outline', value: profile.city });
  }
  if (profile.instagramHandle) {
    infoCardItems.push({ icon: 'instagram', value: `@${profile.instagramHandle}` });
  }
  if (profile.yearsExperience != null) {
    infoCardItems.push({ icon: 'briefcase-outline', value: `${profile.yearsExperience} ${t('profile.experience')}` });
  }
  if (profile.certifications) {
    infoCardItems.push({ icon: 'certificate-outline', value: profile.certifications });
  }
  if (profile.specialties.length > 0) {
    infoCardItems.push({ icon: 'scissors-cutting', value: profile.specialties.join(', ') });
  }

  // Actions zone for ProfileHeader
  const actions = isOwnProfile ? (
    <TouchableOpacity
      style={[s.actionBtn, { backgroundColor: colors.surfaceContainerHigh }]}
      onPress={handleShare}
      activeOpacity={0.7}
    >
      <MaterialCommunityIcons name="share-variant-outline" size={16} color={colors.onSurfaceVariant} />
      <Text style={[s.actionLabel, { color: colors.onSurfaceVariant }]}>{t('common.actions.share')}</Text>
    </TouchableOpacity>
  ) : (
    <FollowButton
      targetId={id!}
      targetType="coiffeur"
      isFollowed={profile.isFollowedByCurrentUser ?? false}
      followLabel={t('common.actions.follow')}
      followingLabel={t('common.states.following')}
    />
  );

  // All posts = pinned first, then recentPosts (deduplicated)
  const pinnedIds = new Set(pinnedPosts.map((p) => p.id));
  const allPosts = [...pinnedPosts, ...profile.recentPosts.filter((p) => !pinnedIds.has(p.id))];

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header bar */}
      <View style={[s.topBar, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons
            name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'}
            size={24}
            color={colors.onSurface}
          />
        </TouchableOpacity>
        <Text style={[s.topTitle, { color: colors.onSurface }]} numberOfLines={1}>{fullName}</Text>
      </View>

      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
      >
        <ProfileHeader
          coverUrl={profile.coverImageUrl}
          avatarUrl={profile.avatarUrl}
          name={fullName}
          verified={profile.isVerified}
          subtitle={subtitle}
          stats={stats}
          bio={profile.bio}
          actions={actions}
          isOwnProfile={isOwnProfile}
        />

        {/* Info card */}
        {infoCardItems.length > 0 && <ProfileInfoCard items={infoCardItems} />}

        {/* Tabs */}
        <View style={s.tabContainer}>
          <ProfileTabBar tabs={tabs} activeKey={activeTab} onChange={(k) => setActiveTab(k as ProfileTab)} />
        </View>

        {/* Tab content */}
        <View style={s.tabContent}>
          {activeTab === 'posts' && (
            <>
              {pinnedPosts.length > 0 && (
                <Text style={[s.pinnedLabel, { color: colors.onSurfaceVariant }]}>
                  📌 {t('profile.pinnedPosts')}
                </Text>
              )}
              {allPosts.length > 0 ? allPosts.map((post) => (
                <PostCard
                  key={post.id}
                  post={post}
                  currentUserId={user?.id}
                  onPress={() => router.push(`/post/${post.id}`)}
                  onProfilePress={() => navigateToProfile(post.authorUserType, post.authorId)}
                />
              )) : (
                <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
                  {t('common.states.empty')}
                </Text>
              )}
            </>
          )}

          {activeTab === 'portfolio' && (
            <>
              {profile.portfolios.length > 0 ? profile.portfolios.map((p) => (
                <TouchableOpacity
                  key={p.id}
                  style={[s.portfolioCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant + '4D' }]}
                  activeOpacity={0.85}
                >
                  {p.coverImageUrl && (
                    <View style={s.portfolioCover}>
                      <View style={s.portfolioCoverImage}>
                        {/* Using View + background workaround since Image is already imported elsewhere */}
                      </View>
                    </View>
                  )}
                  <View style={s.portfolioInfo}>
                    <Text style={[s.portfolioTitle, { color: colors.onSurface }]}>{p.title}</Text>
                    {p.description ? (
                      <Text style={[s.portfolioDesc, { color: colors.onSurfaceVariant }]} numberOfLines={2}>
                        {p.description}
                      </Text>
                    ) : null}
                    <Text style={[s.portfolioCount, { color: colors.onSurfaceVariant }]}>
                      {p.postsCount} {t('profile.stats.posts').toLowerCase()}
                    </Text>
                  </View>
                </TouchableOpacity>
              )) : (
                <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
                  {t('common.states.empty')}
                </Text>
              )}
            </>
          )}

          {activeTab === 'reviews' && (
            <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
              {profile.statistics.totalReviews > 0
                ? `★ ${profile.statistics.averageRating.toFixed(1)} — ${profile.statistics.totalReviews} ${t('review.reviewsTitle').toLowerCase()}`
                : t('review.noReviews')}
            </Text>
          )}
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 52,
    paddingBottom: 12,
    paddingHorizontal: 16,
    gap: 12,
  },
  topTitle: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 20,
    fontWeight: '600',
    flex: 1,
  },
  tabContainer: {
    paddingHorizontal: 20,
  },
  tabContent: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 8,
  },
  pinnedLabel: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 8,
  },
  emptyText: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    textAlign: 'center',
    paddingVertical: 24,
  },
  actionBtn: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 999,
    minHeight: 40,
  },
  actionLabel: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
  portfolioCard: {
    borderRadius: 16,
    borderWidth: 0.5,
    marginBottom: 12,
    overflow: 'hidden',
  },
  portfolioCover: {
    height: 120,
    backgroundColor: 'rgba(0,0,0,0.05)',
  },
  portfolioCoverImage: {
    flex: 1,
  },
  portfolioInfo: {
    padding: 14,
  },
  portfolioTitle: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 16,
    fontWeight: '600',
  },
  portfolioDesc: {
    fontFamily: 'Manrope-Regular',
    fontSize: 13,
    marginTop: 4,
    lineHeight: 18,
  },
  portfolioCount: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    marginTop: 6,
  },
});
