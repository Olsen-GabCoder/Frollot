import { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, I18nManager } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { resolveMediaUrl } from '../src/utils/media';
import { useAuthStore } from '../src/stores/authStore';
import { portfoliosApi } from '../src/api/portfolios';
import { PortfolioResponse } from '../src/types';

export default function PortfoliosListScreen() {
  const { ownerId, ownerType } = useLocalSearchParams<{ ownerId: string; ownerType: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { user } = useAuthStore();
  const [portfolios, setPortfolios] = useState<PortfolioResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const isOwner = user?.id === ownerId;

  useEffect(() => {
    if (!ownerId) return;
    portfoliosApi.getPortfoliosByOwner(ownerId, ownerType || 'user', isOwner)
      .then(setPortfolios).catch(() => {}).finally(() => setIsLoading(false));
  }, [ownerId]);

  if (isLoading) return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}><MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} /></TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>{t('profile.portfolios')}</Text>
      </View>
      <FlatList data={portfolios} keyExtractor={(i) => i.id}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.card, { backgroundColor: colors.surface }]} onPress={() => router.push(`/portfolio/${item.id}`)}>
            {item.coverImageUrl && <Image source={{ uri: resolveMediaUrl(item.coverImageUrl) }} style={styles.cardImg} contentFit="cover" />}
            <View style={styles.cardInfo}>
              <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.title}</Text>
              {item.description && <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{item.description}</Text>}
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginTop: 4 }]}>{t('collections.postCount', { count: item.postsCount })}</Text>
            </View>
          </TouchableOpacity>
        )}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <MaterialIcons name="photo-library" size={48} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>{t('portfolio.empty')}</Text>
            {isOwner && (
              <TouchableOpacity style={[styles.createBtn, { backgroundColor: colors.primary }]}
                onPress={() => router.push({ pathname: '/create-portfolio', params: { ownerId: ownerId!, ownerType: ownerType || 'user' } })}>
                <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>{t('portfolio.create')}</Text>
              </TouchableOpacity>
            )}
          </View>
        }
        contentContainerStyle={styles.list}
      />
      {isOwner && portfolios.length > 0 && (
        <TouchableOpacity style={[styles.fab, { backgroundColor: colors.primary }]}
          onPress={() => router.push({ pathname: '/create-portfolio', params: { ownerId: ownerId!, ownerType: ownerType || 'user' } })}>
          <MaterialIcons name="add" size={28} color={colors.onPrimary} />
        </TouchableOpacity>
      )}
    </View>
  );
}
const styles = StyleSheet.create({
  container: { flex: 1 }, centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  card: { flexDirection: 'row', borderRadius: 16, marginBottom: 10, overflow: 'hidden' },
  cardImg: { width: 100, height: 80 }, cardInfo: { flex: 1, padding: 12, justifyContent: 'center' },
  emptyState: { alignItems: 'center', padding: 48 }, createBtn: { marginTop: 16, paddingVertical: 10, paddingHorizontal: 24, borderRadius: 28 },
  fab: { position: 'absolute', bottom: 24, end: 24, width: 56, height: 56, borderRadius: 28, justifyContent: 'center', alignItems: 'center', elevation: 4 },
});
