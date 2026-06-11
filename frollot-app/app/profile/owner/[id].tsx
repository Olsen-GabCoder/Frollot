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
import { salonsApi } from '../../../src/api/salons';
import { collectionsApi } from '../../../src/api/portfolios';
import { SalonOwnerProfileResponse, Salon, CollectionResponse } from '../../../src/types';

export default function SalonOwnerProfileScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [profile, setProfile] = useState<SalonOwnerProfileResponse | null>(null);
  const [salons, setSalons] = useState<Salon[]>([]);
  const [collections, setCollections] = useState<CollectionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const [profileData, salonsData, collectionsData] = await Promise.allSettled([
          profilesApi.getSalonOwnerProfile(id),
          salonsApi.getSalonsByOwner(id),
          collectionsApi.getCollectionsByUser(id),
        ]);
        if (profileData.status === 'fulfilled') setProfile(profileData.value);
        if (salonsData.status === 'fulfilled') setSalons(salonsData.value);
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
    { label: 'Salons', value: profile.salonsCount },
    { label: t('profile.followers'), value: profile.totalFollowers },
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
        {/* Profile card */}
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

          <View style={styles.statsRow}>
            {stats.map((s, i) => (
              <View key={i} style={styles.statItem}>
                <Text style={[typo.titleMedium, { color: colors.onSurface }]}>{s.value}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{s.label}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* Owned salons */}
        {salons.length > 0 && (
          <>
            <Text style={[typo.titleMedium, { color: colors.onBackground, marginTop: 8, marginBottom: 8 }]}>
              Salons
            </Text>
            {salons.map((salon) => (
              <TouchableOpacity
                key={salon.id}
                style={[styles.salonCard, { backgroundColor: colors.surface }]}
                onPress={() => router.push(`/salon/${salon.id}`)}
              >
                <View style={[styles.salonThumb, { backgroundColor: colors.surfaceContainerHigh }]}>
                  {salon.coverPhotoUrl && (
                    <Image source={{ uri: resolveMediaUrl(salon.coverPhotoUrl) }} style={styles.salonThumb} contentFit="cover" />
                  )}
                </View>
                <View style={{ flex: 1, marginLeft: 12 }}>
                  <View style={styles.salonNameRow}>
                    <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{salon.name}</Text>
                    {salon.isVerified && (
                      <MaterialIcons name="verified" size={14} color={colors.primary} style={{ marginLeft: 4 }} />
                    )}
                  </View>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{salon.city}</Text>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
                    {t('profile.followersCount', { count: salon.followersCount ?? 0 })}
                  </Text>
                </View>
                <MaterialIcons name="chevron-right" size={24} color={colors.onSurfaceVariant} />
              </TouchableOpacity>
            ))}
          </>
        )}

        {/* Collections */}
        {collections.length > 0 && (
          <>
            <Text style={[typo.titleMedium, { color: colors.onBackground, marginTop: 16, marginBottom: 8 }]}>
              {t('profile.collections')}
            </Text>
            {collections.map((col) => (
              <TouchableOpacity
                key={col.id}
                style={[styles.collectionCard, { backgroundColor: colors.surface }]}
                onPress={() => router.push(`/collections/${col.id}`)}
              >
                <MaterialIcons name="collections-bookmark" size={20} color={colors.onSurfaceVariant} />
                <Text style={[typo.titleSmall, { color: colors.onSurface, flex: 1, marginLeft: 12 }]}>{col.name}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{col.postsCount}</Text>
                <MaterialIcons name="chevron-right" size={20} color={colors.onSurfaceVariant} />
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
  verifiedRow: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
  statsRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginTop: 20 },
  statItem: { alignItems: 'center' },
  salonCard: { flexDirection: 'row', alignItems: 'center', padding: 12, borderRadius: 16, marginBottom: 8 },
  salonThumb: { width: 60, height: 60, borderRadius: 12 },
  salonNameRow: { flexDirection: 'row', alignItems: 'center' },
  collectionCard: { flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 12, marginBottom: 8 },
});
