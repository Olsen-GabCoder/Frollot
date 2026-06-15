import { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl, I18nManager } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';
import { socialApi } from '../../src/api/social';
import { Toast, type ToastType } from '../../src/components/ui';
import { ErrorState } from '../../src/components/lists';
import { PostResponse } from '../../src/types';

export default function FavoritesScreen() {
  const { userId } = useLocalSearchParams<{ userId: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  const loadFavorites = async (reset = false) => {
    if (!userId) return;
    const p = reset ? 0 : page;
    setLoadError(null);
    try {
      const r = await socialApi.getFavoritesByUser(userId, p, 20);
      if (reset) { setPosts(r.content); setPage(0); }
      else setPosts((prev) => [...prev, ...r.content]);
      setHasMore(!r.last);
    } catch (error: any) {
      // Backend owner-only : 403 si on tente de voir les favoris d'autrui — message honnête
      setLoadError(error?.response?.status === 403
        ? t('profile.favoritesPrivate')
        : t('profile.favoritesLoadError'));
    } finally { setIsLoading(false); setRefreshing(false); }
  };

  useEffect(() => { loadFavorites(true); }, [userId]);

  const onRefresh = () => { setRefreshing(true); loadFavorites(true); };
  const loadMore = () => { if (hasMore && !loadError) { setPage((p) => p + 1); loadFavorites(false); } };

  // Mise à jour optimiste : retrait immédiat, rollback visible + toast si le serveur refuse
  const handleUnfavorite = async (postId: string) => {
    const previousPosts = posts;
    setPosts((prev) => prev.filter((p) => p.id !== postId));
    try {
      await socialApi.toggleFavorite(postId);
    } catch (error: any) {
      setPosts(previousPosts); // rollback : le post revient dans la liste
      setToast({ message: error?.response?.data?.message || t('profile.removeFavoriteError'), type: 'error' });
    }
  };

  if (isLoading) return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}><MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} /></TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>{t('profile.favorites')}</Text>
      </View>
      {loadError && posts.length === 0 ? (
        <ErrorState message={loadError} onRetry={() => { setIsLoading(true); loadFavorites(true); }} />
      ) : (
      <FlatList data={posts} keyExtractor={(i) => i.id}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.postCard, { backgroundColor: colors.surface }]} onPress={() => router.push(`/post/${item.id}`)}>
            <View style={styles.postRow}>
              <View style={{ flex: 1 }}>
                <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.authorName}</Text>
                <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 4 }]} numberOfLines={2}>{item.content}</Text>
              </View>
              {item.imageUrl && <Image source={{ uri: resolveMediaUrl(item.imageUrl) }} style={styles.thumb} contentFit="cover" />}
            </View>
            <View style={styles.postFooter}>
              <View style={styles.iconRow}><MaterialIcons name="favorite" size={14} color={colors.onSurfaceVariant} /><Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>{item.likesCount}</Text></View>
              <TouchableOpacity onPress={() => handleUnfavorite(item.id)}>
                <MaterialIcons name="bookmark-remove" size={20} color={colors.error} />
              </TouchableOpacity>
            </View>
          </TouchableOpacity>
        )}
        onEndReached={loadMore} onEndReachedThreshold={0.3}
        ListEmptyComponent={<View style={styles.emptyState}><MaterialIcons name="favorite-border" size={48} color={colors.onSurfaceVariant} /><Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>{t('profile.favoritesEmpty')}</Text></View>}
        contentContainerStyle={styles.list}
      />
      )}

      {/* Toast feedback (erreur retrait favori) */}
      {toast && (
        <Toast message={toast.message} type={toast.type} visible onDismiss={() => setToast(null)} />
      )}
    </View>
  );
}
const styles = StyleSheet.create({
  container: { flex: 1 }, centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  list: { paddingHorizontal: 16, paddingBottom: 40 }, postCard: { borderRadius: 12, padding: 16, marginBottom: 8 },
  postRow: { flexDirection: 'row' }, thumb: { width: 60, height: 60, borderRadius: 8, marginStart: 12 },
  postFooter: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 },
  iconRow: { flexDirection: 'row', alignItems: 'center' }, emptyState: { alignItems: 'center', padding: 48 },
});
