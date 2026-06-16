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
import { ClientProfileResponse, PostResponse } from '../../../src/types';
import { LoadingState, ErrorState } from '../../../src/components/lists';
import { PostCard } from '../../../src/components/social';
import {
  ProfileHeader,
  ProfileTabBar,
  ProfileInfoCard,
  FollowButton,
} from '../../../src/components/profile';
import { navigateToProfile } from '../../../src/utils/navigateToProfile';

type ProfileTab = 'posts' | 'collections';

export default function ClientProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  const [profile, setProfile] = useState<ClientProfileResponse | null>(null);
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
      const profileData = await profilesApi.getClientProfile(id);
      setProfile(profileData);
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

  if (isLoading && !profile) return <LoadingState />;
  if (error && !profile) return <ErrorState message={error} onRetry={loadProfile} />;
  if (!profile) return null;

  const fullName = [profile.firstName, profile.lastName].filter(Boolean).join(' ') || '?';
  const subtitle = t('profile.type.client');

  const stats: { label: string; value: number }[] = [
    { label: t('profile.stats.followers'), value: profile.statistics.followersCount },
    { label: t('profile.stats.following'), value: profile.statistics.followingCount },
    { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
  ];

  const tabs: { key: ProfileTab; label: string }[] = [
    { key: 'posts', label: t('profile.tabs.posts') },
    { key: 'collections', label: t('profile.tabs.collections') },
  ];

  const infoCardItems: { icon: keyof typeof import('@expo/vector-icons').MaterialCommunityIcons.glyphMap; value: string }[] = [];
  if (profile.city) {
    infoCardItems.push({ icon: 'map-marker-outline', value: profile.city });
  }

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
      targetType="user"
      isFollowed={profile.isFollowedByCurrentUser ?? false}
      followLabel={t('common.actions.follow')}
      followingLabel={t('common.states.following')}
    />
  );

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
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

        {infoCardItems.length > 0 && <ProfileInfoCard items={infoCardItems} />}

        <View style={s.tabContainer}>
          <ProfileTabBar tabs={tabs} activeKey={activeTab} onChange={(k) => setActiveTab(k as ProfileTab)} />
        </View>

        <View style={s.tabContent}>
          {activeTab === 'posts' && (
            <>
              {profile.recentPosts.length > 0 ? profile.recentPosts.map((post) => (
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

          {activeTab === 'collections' && (
            <>
              {profile.collections.length > 0 ? profile.collections.map((col) => (
                <TouchableOpacity
                  key={col.id}
                  style={[s.collectionCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant + '4D' }]}
                  onPress={() => router.push(`/collections/${col.id}`)}
                  activeOpacity={0.85}
                >
                  <View style={[s.collectionIcon, { backgroundColor: colors.tertiaryContainer }]}>
                    <MaterialCommunityIcons name="bookmark-multiple-outline" size={20} color={colors.onTertiaryContainer} />
                  </View>
                  <View style={{ flex: 1, marginStart: 12 }}>
                    <Text style={[s.collectionTitle, { color: colors.onSurface }]}>{col.name}</Text>
                    <Text style={[s.collectionCount, { color: colors.onSurfaceVariant }]}>
                      {col.postsCount} {t('profile.stats.posts').toLowerCase()}
                    </Text>
                  </View>
                  <MaterialCommunityIcons
                    name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'}
                    size={24}
                    color={colors.onSurfaceVariant}
                  />
                </TouchableOpacity>
              )) : (
                <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>
                  {t('common.states.empty')}
                </Text>
              )}
            </>
          )}
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  topBar: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16, gap: 12 },
  topTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 20, fontWeight: '600', flex: 1 },
  tabContainer: { paddingHorizontal: 20 },
  tabContent: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 8 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', paddingVertical: 24 },
  actionBtn: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, paddingVertical: 10, paddingHorizontal: 16, borderRadius: 999, minHeight: 40 },
  actionLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  collectionCard: { flexDirection: 'row', alignItems: 'center', borderRadius: 16, borderWidth: 0.5, padding: 14, marginBottom: 10 },
  collectionIcon: { width: 40, height: 40, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  collectionTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  collectionCount: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
});
