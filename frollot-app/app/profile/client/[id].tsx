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
import { collectionsApi } from '../../../src/api/portfolios';
import { ClientProfileResponse, CollectionResponse } from '../../../src/types';

export default function ClientProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [profile, setProfile] = useState<ClientProfileResponse | null>(null);
  const [collections, setCollections] = useState<CollectionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const [profileData, collectionsData] = await Promise.allSettled([
          profilesApi.getClientProfile(id),
          collectionsApi.getCollectionsByUser(id),
        ]);
        if (profileData.status === 'fulfilled') setProfile(profileData.value);
        if (collectionsData.status === 'fulfilled') setCollections(collectionsData.value);
      } catch (e: any) {
        setError(e?.message || t('common.error'));
      } finally {
        setIsLoading(false);
      }
    })();
  }, [id]);

  if (isLoading) return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  if (error || !profile) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[typo.bodyLarge, { color: colors.error }]}>{error}</Text>
      </View>
    );
  }

  const stats = [
    { label: t('booking.myBookings'), value: profile.totalBookings },
    { label: t('salon.reviews'), value: profile.totalReviews },
    { label: t('profile.favorites'), value: profile.favoriteSalonsCount },
    { label: t('profile.collections'), value: profile.collectionsCount },
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
          {profile.memberSince && (
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 4 }]}>
              Membre depuis {new Date(profile.memberSince).getFullYear()}
            </Text>
          )}

          <View style={styles.statsRow}>
            {stats.map((s, i) => (
              <View key={i} style={styles.statItem}>
                <Text style={[typo.titleMedium, { color: colors.onSurface }]}>{s.value}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{s.label}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Collections */}
        {collections.length > 0 && (
          <>
            <Text style={[typo.titleMedium, { color: colors.onBackground, marginTop: 8, marginBottom: 8 }]}>
              {t('profile.collections')}
            </Text>
            {collections.map((col) => (
              <TouchableOpacity
                key={col.id}
                style={[styles.collectionCard, { backgroundColor: colors.surface }]}
                onPress={() => router.push(`/collections/${col.id}`)}
              >
                <View style={[styles.collectionIcon, { backgroundColor: colors.tertiaryContainer }]}>
                  <MaterialIcons name="collections-bookmark" size={20} color={colors.onTertiaryContainer} />
                </View>
                <View style={{ flex: 1, marginLeft: 12 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{col.name}</Text>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{col.postsCount} posts</Text>
                </View>
                <MaterialIcons name="chevron-right" size={24} color={colors.onSurfaceVariant} />
              </TouchableOpacity>
            ))}
          </>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scrollContent: { paddingHorizontal: 16, paddingTop: 16 },
  profileCard: { borderRadius: 16, padding: 24, alignItems: 'center', marginBottom: 12 },
  avatar: { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center' },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 20 },
  statItem: { alignItems: 'center' },
  collectionCard: { flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 12, marginBottom: 8 },
  collectionIcon: { width: 40, height: 40, borderRadius: 12, justifyContent: 'center', alignItems: 'center' },
});
