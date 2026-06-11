import { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';
import { portfoliosApi } from '../../src/api/portfolios';
import { PortfolioResponse, PostResponse } from '../../src/types';

export default function PortfolioDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null);
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const [p, postsData] = await Promise.all([
          portfoliosApi.getPortfolioById(id),
          portfoliosApi.getPortfolioPosts(id, 0, 20),
        ]);
        setPortfolio(p);
        setPosts(postsData.content);
        setHasMore(!postsData.last);
      } catch {} finally { setIsLoading(false); }
    })();
  }, [id]);

  const loadMore = async () => {
    if (!id || !hasMore) return;
    const next = page + 1;
    try {
      const r = await portfoliosApi.getPortfolioPosts(id, next, 20);
      setPosts((p) => [...p, ...r.content]);
      setPage(next);
      setHasMore(!r.last);
    } catch {}
  };

  if (isLoading) return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  if (!portfolio) return <View style={[styles.centered, { backgroundColor: colors.background }]}><Text style={[typo.bodyLarge, { color: colors.error }]}>{t('common.error')}</Text></View>;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}><MaterialIcons name="arrow-back" size={24} color={colors.onSurface} /></TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16, flex: 1 }]} numberOfLines={1}>{portfolio.title}</Text>
      </View>
      <FlatList data={posts} keyExtractor={(i) => i.id}
        ListHeaderComponent={() => (
          <View style={styles.portfolioHeader}>
            {portfolio.coverImageUrl && <Image source={{ uri: resolveMediaUrl(portfolio.coverImageUrl) }} style={styles.coverImg} contentFit="cover" />}
            <Text style={[typo.headlineSmall, { color: colors.onBackground, marginTop: 12 }]}>{portfolio.title}</Text>
            {portfolio.description && <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 4 }]}>{portfolio.description}</Text>}
            <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>{portfolio.postsCount} posts</Text>
          </View>
        )}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.postCard, { backgroundColor: colors.surface }]} onPress={() => router.push(`/post/${item.id}`)}>
            {item.imageUrl && <Image source={{ uri: resolveMediaUrl(item.imageUrl) }} style={styles.postImg} contentFit="cover" />}
            <Text style={[typo.bodyMedium, { color: colors.onSurface, padding: 12 }]} numberOfLines={2}>{item.content}</Text>
            <View style={styles.postMeta}>
              <MaterialIcons name="favorite" size={14} color={colors.onSurfaceVariant} />
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>{item.likesCount}</Text>
              <MaterialIcons name="chat-bubble-outline" size={14} color={colors.onSurfaceVariant} style={{ marginLeft: 12 }} />
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>{item.commentsCount}</Text>
            </View>
          </TouchableOpacity>
        )}
        onEndReached={loadMore} onEndReachedThreshold={0.3}
        ListEmptyComponent={<Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', padding: 40 }]}>{t('social.noPosts')}</Text>}
        contentContainerStyle={styles.list}
      />
    </View>
  );
}
const styles = StyleSheet.create({
  container: { flex: 1 }, centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  portfolioHeader: { padding: 16 }, coverImg: { width: '100%', height: 180, borderRadius: 16 },
  list: { paddingBottom: 40 }, postCard: { marginHorizontal: 16, borderRadius: 12, marginBottom: 10, overflow: 'hidden' },
  postImg: { width: '100%', height: 160 }, postMeta: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 12, paddingBottom: 12 },
});
