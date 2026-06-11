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
import { profilesApi } from '../../../src/api/profiles';
import { socialApi } from '../../../src/api/social';
import { SalonSocialProfileResponse } from '../../../src/types';

export default function SalonSocialProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [profile, setProfile] = useState<SalonSocialProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const data = await profilesApi.getSalonSocialProfile(id);
        setProfile(data);
        setIsFollowing(data.isFollowedByCurrentUser ?? false);
      } catch (e: any) {
        setError(e?.message || t('common.error'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, [id]);

  const handleFollow = async () => {
    if (!id) return;
    try {
      if (isFollowing) await socialApi.unfollowSalon(id);
      else await socialApi.followSalon(id);
      setIsFollowing(!isFollowing);
    } catch {}
  };

  if (isLoading) return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  if (error || !profile) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[typo.bodyLarge, { color: colors.error }]}>{error}</Text>
      </View>
    );
  }

  const stats = [
    { label: 'Posts', value: profile.postsCount },
    { label: t('profile.followers'), value: profile.followersCount },
    { label: t('profile.portfolios'), value: profile.portfoliosCount },
  ];

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>{profile.name}</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Cover */}
        {profile.coverPhotoUrl && (
          <Image source={{ uri: resolveMediaUrl(profile.coverPhotoUrl) }} style={styles.coverImage} contentFit="cover" />
        )}

        {/* Profile info */}
        <View style={[styles.profileCard, { backgroundColor: colors.surface }]}>
          <Text style={[typo.headlineSmall, { color: colors.onSurface }]}>{profile.name}</Text>
          <View style={styles.row}>
            <MaterialIcons name="location-on" size={16} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>{profile.city}</Text>
          </View>
          {profile.isVerified && (
            <View style={styles.row}>
              <MaterialIcons name="verified" size={16} color={colors.primary} />
              <Text style={[typo.labelSmall, { color: colors.primary, marginLeft: 4 }]}>{t('verification.verified')}</Text>
            </View>
          )}
          {profile.averageRating != null && (
            <View style={styles.row}>
              <MaterialIcons name="star" size={16} color={colors.tertiary} />
              <Text style={[typo.labelMedium, { color: colors.onSurface, marginLeft: 4 }]}>
                {profile.averageRating.toFixed(1)} ({profile.totalReviews} {t('salon.reviews').toLowerCase()})
              </Text>
            </View>
          )}

          <View style={styles.statsRow}>
            {stats.map((s, i) => (
              <View key={i} style={styles.statItem}>
                <Text style={[typo.titleMedium, { color: colors.onSurface }]}>{s.value}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{s.label}</Text>
              </View>
            ))}
          </View>

          <TouchableOpacity
            style={[styles.followBtn, { backgroundColor: isFollowing ? colors.surfaceContainerHigh : colors.primary }]}
            onPress={handleFollow}
          >
            <Text style={[typo.labelLarge, { color: isFollowing ? colors.onSurfaceVariant : colors.onPrimary }]}>
              {isFollowing ? t('salon.following') : t('salon.follow')}
            </Text>
          </TouchableOpacity>
        </View>

        {/* Description */}
        {profile.description && (
          <View style={[styles.section, { backgroundColor: colors.surface }]}>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant }]}>{profile.description}</Text>
          </View>
        )}

        {/* View full salon */}
        <TouchableOpacity
          style={[styles.section, { backgroundColor: colors.primaryContainer }]}
          onPress={() => router.push(`/salon/${profile.salonId}`)}
        >
          <Text style={[typo.labelLarge, { color: colors.onPrimaryContainer, textAlign: 'center' }]}>
            Voir le salon complet →
          </Text>
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scrollContent: { paddingHorizontal: 16, paddingTop: 0 },
  coverImage: { width: '100%', height: 180, borderRadius: 16, marginBottom: 12, marginTop: 12 },
  profileCard: { borderRadius: 16, padding: 20, alignItems: 'center', marginBottom: 12 },
  row: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 20, marginBottom: 16 },
  statItem: { alignItems: 'center' },
  followBtn: { paddingVertical: 10, paddingHorizontal: 32, borderRadius: 999 },
  section: { borderRadius: 16, padding: 16, marginBottom: 12 },
});
