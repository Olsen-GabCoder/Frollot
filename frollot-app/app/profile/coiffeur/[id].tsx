import { useEffect, useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../../src/theme';
import { resolveMediaUrl } from '../../../src/utils/media';
import { useAuthStore } from '../../../src/stores/authStore';
import { profilesApi } from '../../../src/api/profiles';
import { socialApi } from '../../../src/api/social';
import { CoiffeurProfileResponse, PostResponse } from '../../../src/types';

export default function CoiffeurProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { user } = useAuthStore();
  const { colors, typography: typo } = theme;

  const [profile, setProfile] = useState<CoiffeurProfileResponse | null>(null);
  const [pinnedPosts, setPinnedPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);

  const loadProfile = async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      const [profileData, pinned] = await Promise.allSettled([
        profilesApi.getCoiffeurProfile(id),
        socialApi.getPinnedPosts(id),
      ]);
      if (profileData.status === 'fulfilled') {
        setProfile(profileData.value);
        setIsFollowing(profileData.value.isFollowedByCurrentUser ?? false);
      }
      if (pinned.status === 'fulfilled') setPinnedPosts(pinned.value);
    } catch (e: any) {
      setError(e?.message || t('common.error'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => { loadProfile(); }, [id]);

  const handleFollow = async () => {
    if (!id) return;
    try {
      if (isFollowing) {
        await socialApi.unfollowCoiffeur(id);
      } else {
        await socialApi.followCoiffeur(id);
      }
      setIsFollowing(!isFollowing);
    } catch {}
  };

  if (isLoading) {
    return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (error || !profile) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[typo.bodyLarge, { color: colors.error }]}>{error}</Text>
        <TouchableOpacity style={[styles.retryBtn, { backgroundColor: colors.primary }]} onPress={loadProfile}>
          <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>{t('common.retry')}</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const stats = [
    { label: 'Posts', value: profile.postsCount },
    { label: t('profile.followers'), value: profile.followersCount },
    { label: t('profile.portfolios'), value: profile.portfoliosCount },
    { label: t('salon.reviews'), value: profile.totalReviews ?? 0 },
  ];

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>{t('profile.profile')}</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Profile header */}
        <View style={[styles.profileCard, { backgroundColor: colors.surface }]}>
          <View style={[styles.avatar, { backgroundColor: colors.primaryContainer }]}>
            {profile.avatarUrl ? (
              <Image source={{ uri: resolveMediaUrl(profile.avatarUrl) }} style={styles.avatar} contentFit="cover" />
            ) : (
              <Text style={[typo.headlineLarge, { color: colors.onPrimaryContainer }]}>
                {profile.firstName[0]?.toUpperCase()}
              </Text>
            )}
          </View>
          <Text style={[typo.headlineSmall, { color: colors.onSurface, marginTop: 12 }]}>
            {profile.firstName} {profile.lastName}
          </Text>
          {profile.isVerified && (
            <View style={styles.verifiedRow}>
              <MaterialIcons name="verified" size={16} color={colors.primary} />
              <Text style={[typo.labelSmall, { color: colors.primary, marginLeft: 4 }]}>{t('verification.verified')}</Text>
            </View>
          )}
          {profile.salonName && (
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 4 }]}>
              {profile.salonName}
            </Text>
          )}
          {profile.averageRating != null && (
            <View style={styles.ratingRow}>
              <MaterialIcons name="star" size={16} color={colors.tertiary} />
              <Text style={[typo.labelMedium, { color: colors.onSurface, marginLeft: 4 }]}>
                {profile.averageRating.toFixed(1)}
              </Text>
            </View>
          )}

          {/* Stats */}
          <View style={styles.statsRow}>
            {stats.map((s, i) => (
              <View key={i} style={styles.statItem}>
                <Text style={[typo.titleMedium, { color: colors.onSurface }]}>{s.value}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{s.label}</Text>
              </View>
            ))}
          </View>

          {/* Follow button */}
          {user?.id !== id && (
            <TouchableOpacity
              style={[styles.followBtn, { backgroundColor: isFollowing ? colors.surfaceContainerHigh : colors.primary }]}
              onPress={handleFollow}
            >
              <Text style={[typo.labelLarge, { color: isFollowing ? colors.onSurfaceVariant : colors.onPrimary }]}>
                {isFollowing ? t('salon.following') : t('salon.follow')}
              </Text>
            </TouchableOpacity>
          )}
        </View>

        {/* Bio */}
        {profile.bio && (
          <View style={[styles.section, { backgroundColor: colors.surface }]}>
            <Text style={[typo.titleSmall, { color: colors.onSurface, marginBottom: 8 }]}>Bio</Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant }]}>{profile.bio}</Text>
          </View>
        )}

        {/* Specialties */}
        {profile.specialties.length > 0 && (
          <View style={[styles.section, { backgroundColor: colors.surface }]}>
            <Text style={[typo.titleSmall, { color: colors.onSurface, marginBottom: 8 }]}>{t('staff.specialties')}</Text>
            <View style={styles.chipsRow}>
              {profile.specialties.map((s, i) => (
                <View key={i} style={[styles.chip, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>{s}</Text>
                </View>
              ))}
            </View>
          </View>
        )}

        {/* Experience */}
        {profile.yearsExperience != null && (
          <View style={[styles.section, { backgroundColor: colors.surface }]}>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant }]}>
              {profile.yearsExperience} ans d'experience
            </Text>
          </View>
        )}

        {/* Pinned posts */}
        {pinnedPosts.length > 0 && (
          <View style={styles.sectionHeader}>
            <Text style={[typo.titleMedium, { color: colors.onBackground }]}>Posts epingles</Text>
          </View>
        )}
        {pinnedPosts.map((post) => (
          <TouchableOpacity
            key={post.id}
            style={[styles.postCard, { backgroundColor: colors.surface }]}
            onPress={() => router.push(`/post/${post.id}`)}
          >
            <Text style={[typo.bodyMedium, { color: colors.onSurface }]} numberOfLines={2}>{post.content}</Text>
            <View style={styles.postMeta}>
              <MaterialIcons name="favorite" size={14} color={colors.onSurfaceVariant} />
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>{post.likesCount}</Text>
              <MaterialIcons name="chat-bubble-outline" size={14} color={colors.onSurfaceVariant} style={{ marginLeft: 12 }} />
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>{post.commentsCount}</Text>
            </View>
          </TouchableOpacity>
        ))}

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
  profileCard: { borderRadius: 16, padding: 24, alignItems: 'center', marginBottom: 12 },
  avatar: { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center' },
  verifiedRow: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  ratingRow: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 20, marginBottom: 16 },
  statItem: { alignItems: 'center' },
  followBtn: { paddingVertical: 10, paddingHorizontal: 32, borderRadius: 999 },
  section: { borderRadius: 16, padding: 16, marginBottom: 12 },
  chipsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999 },
  sectionHeader: { marginTop: 8, marginBottom: 8 },
  postCard: { borderRadius: 12, padding: 16, marginBottom: 8 },
  postMeta: { flexDirection: 'row', alignItems: 'center', marginTop: 8 },
});
