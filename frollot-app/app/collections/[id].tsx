import { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, I18nManager } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../src/theme';
import { collectionsApi } from '../../src/api/portfolios';
import { CollectionResponse, CollectionPostResponse } from '../../src/types';
import { resolveMediaUrl } from '../../src/utils/media';
import { LoadingState } from '../../src/components/lists/LoadingState';
import { ErrorState } from '../../src/components/lists/ErrorState';
import { EmptyState } from '../../src/components/lists/EmptyState';

const PAGE_SIZE = 20;

export default function CollectionDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();

  const [collection, setCollection] = useState<CollectionResponse | null>(null);
  const [items, setItems] = useState<CollectionPostResponse[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [loadError, setLoadError] = useState('');

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  const load = async () => {
    if (!id) return;
    setIsLoading(true);
    setLoadError('');
    try {
      const [col, postsPage] = await Promise.all([
        collectionsApi.getCollectionById(id),
        collectionsApi.getCollectionPosts(id, 0, PAGE_SIZE),
      ]);
      setCollection(col);
      setItems(postsPage.content);
      setPage(0);
      setHasMore(!postsPage.last);
    } catch (error: any) {
      setLoadError(error?.response?.data?.message || t('collections.loadError'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    (async () => {
      if (!id) return;
      setIsLoading(true);
      setLoadError('');
      try {
        const [col, postsPage] = await Promise.all([
          collectionsApi.getCollectionById(id),
          collectionsApi.getCollectionPosts(id, 0, PAGE_SIZE),
        ]);
        if (!ignore) {
          setCollection(col);
          setItems(postsPage.content);
          setPage(0);
          setHasMore(!postsPage.last);
        }
      } catch (error: any) {
        if (!ignore) setLoadError(error?.response?.data?.message || t('collections.loadError'));
      } finally {
        if (!ignore) setIsLoading(false);
      }
    })();
    return () => { ignore = true; };
  }, [id]);

  const loadMore = async () => {
    if (!id || loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const next = page + 1;
      const postsPage = await collectionsApi.getCollectionPosts(id, next, PAGE_SIZE);
      setItems((prev) => [...prev, ...postsPage.content]);
      setPage(next);
      setHasMore(!postsPage.last);
    } catch {
      // fin de pagination silencieuse refusée : on garde la liste et on retentera au prochain scroll
      setHasMore(true);
    } finally {
      setLoadingMore(false);
    }
  };

  if (isLoading) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <LoadingState />
      </View>
    );
  }

  if (loadError) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <View style={styles.header}>
          <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
            <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={22} color={colors.onSurface} />
          </TouchableOpacity>
        </View>
        <ErrorState message={loadError} onRetry={load} />
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={items}
        keyExtractor={(i) => i.id}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        onEndReached={loadMore}
        onEndReachedThreshold={0.4}
        ListHeaderComponent={
          collection ? (
            <View style={styles.titleBlock}>
              <Text style={[typo.overline, { color: colors.secondary }]}>{t('collections.detailOverline')}</Text>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
                {collection.name}
              </Text>
              {!!collection.description && (
                <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 6 }]}>
                  {collection.description}
                </Text>
              )}
              <Text style={[typo.labelMedium, { color: colors.primary, marginTop: 8 }]}>
                {t('collections.postCount', { count: collection.postsCount })}
              </Text>
            </View>
          ) : null
        }
        renderItem={({ item }) => {
          const image = resolveMediaUrl(item.post.imageUrl);
          return (
            <TouchableOpacity
              style={[styles.postCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}
              activeOpacity={0.7}
              onPress={() => router.push(`/post/${item.post.id}`)}
            >
              {image && <Image source={{ uri: image }} style={styles.postImage} contentFit="cover" />}
              <View style={styles.postBody}>
                <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                  {item.post.authorName}
                </Text>
                <Text style={[typo.bodyMedium, { color: colors.onSurface, marginTop: 2 }]} numberOfLines={2}>
                  {item.post.content}
                </Text>
              </View>
              <MaterialIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={22} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          );
        }}
        ListEmptyComponent={
          <EmptyState
            icon="bookmark-multiple-outline"
            title={t('collections.detailEmpty')}
            message={t('collections.detailEmptyMessage')}
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  titleBlock: { paddingTop: 12, paddingBottom: 16 },
  list: { paddingHorizontal: 16, paddingBottom: 40, flexGrow: 1 },
  postCard: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    borderRadius: 16, borderWidth: 1, padding: 12, marginBottom: 10,
  },
  postImage: { width: 56, height: 56, borderRadius: 10 },
  postBody: { flex: 1 },
});
