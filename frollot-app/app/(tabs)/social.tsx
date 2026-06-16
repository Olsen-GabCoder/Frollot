import { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TextInput, ScrollView, TouchableOpacity,
  StyleSheet, RefreshControl, ActivityIndicator,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { socialApi } from '../../src/api';
import { PostCard, CollectionPickerModal } from '../../src/components/social';
import { Toast, type ToastType } from '../../src/components/ui';
import { LoadingState, EmptyState, ErrorState } from '../../src/components/lists';
import { PostResponse, PostType, SearchResponse } from '../../src/types';
import { sharePostExternally, isShareCancellation } from '../../src/utils/share';
import { navigateToProfile } from '../../src/utils/navigateToProfile';
import { useTheme } from '../../src/theme';

const TAB_KEYS = ['social.tabs.all', 'social.tabs.following', 'social.tabs.trending'];

const POST_TYPE_FILTER_KEYS: { i18nKey: string; value: PostType | undefined }[] = [
  { i18nKey: 'social.postTypes.all', value: undefined },
  { i18nKey: 'social.postTypes.avantApres', value: PostType.AVANT_APRES },
  { i18nKey: 'social.postTypes.realisation', value: PostType.REALISATION },
  { i18nKey: 'social.postTypes.tendance', value: PostType.TENDANCE },
  { i18nKey: 'social.postTypes.conseil', value: PostType.CONSEIL },
  { i18nKey: 'social.postTypes.inspiration', value: PostType.INSPIRATION },
];

export default function SocialFeedScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  // Feed state
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [selectedPostType, setSelectedPostType] = useState(0);

  // Search state
  const [showSearch, setShowSearch] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchResponse | null>(null);
  const [isSearching, setIsSearching] = useState(false);

  // Collection save state (dialog extrait en composant partagé CollectionPickerModal)
  const [savePostId, setSavePostId] = useState<string | null>(null);

  // Toast feedback
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  // Load feed
  const loadPosts = useCallback(async (p = 0, refresh = false) => {
    setHasError(false);
    try {
      const postTypeFilter = POST_TYPE_FILTER_KEYS[selectedPostType]?.value;
      let data;
      if (activeTab === 1) {
        data = await socialApi.getFollowingFeed(p, 20);
      } else if (activeTab === 2) {
        data = await socialApi.getTrendingPosts(undefined, p, 20);
      } else {
        data = await socialApi.getFeed(p, 20);
      }
      let content = data.content || [];
      if (postTypeFilter) {
        content = content.filter((post: PostResponse) => post.postType === postTypeFilter);
      }
      setPosts(prev => refresh ? content : [...prev, ...content]);
      setHasMore(!data.last);
      setPage(p);
    } catch {
      setHasError(true);
    } finally {
      setIsLoading(false);
      setRefreshing(false);
    }
  }, [activeTab, selectedPostType]);

  useEffect(() => { setIsLoading(true); setPosts([]); loadPosts(0, true); }, [activeTab, selectedPostType]);

  const onRefresh = () => { setRefreshing(true); loadPosts(0, true); };
  const onEndReached = () => { if (hasMore && !isLoading && !refreshing) loadPosts(page + 1); };

  // Search debounced
  useEffect(() => {
    if (!searchQuery || searchQuery.length < 2) { setSearchResults(null); return; }
    let ignore = false;
    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const results = await socialApi.unifiedSearch({ q: searchQuery, type: 'all', page: 0, size: 5 });
        if (!ignore) setSearchResults(results);
      } catch {
        if (!ignore) setSearchResults(null);
      } finally {
        if (!ignore) setIsSearching(false);
      }
    }, 300);
    return () => { ignore = true; clearTimeout(timer); };
  }, [searchQuery]);

  // Post interactions
  // B27 : le serveur est la source de vérité du like — on fusionne uniquement
  // isLikedByCurrentUser + likesCount (la réponse de toggleLike est partielle : pas de media).
  const handleLike = async (postId: string) => {
    try {
      const updated = await socialApi.toggleLike(postId);
      setPosts(prev => prev.map(p => p.id === postId
        ? { ...p, isLikedByCurrentUser: updated.isLikedByCurrentUser, likesCount: updated.likesCount }
        : p));
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || t('social.likeError'), type: 'error' });
    }
  };

  // B30 : signet = favori partout (aligné sur le détail). Fusion sélective —
  // la réponse de toggleFavorite est partielle (pas de media), pas de compteur favoris.
  const handleBookmark = async (postId: string) => {
    try {
      const updated = await socialApi.toggleFavorite(postId);
      setPosts(prev => prev.map(p => p.id === postId
        ? { ...p, isFavoritedByCurrentUser: updated.isFavoritedByCurrentUser }
        : p));
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || t('social.bookmarkError'), type: 'error' });
    }
  };

  // B31 : partage externe natif (menu système). Annulation = silencieux normal.
  const handleShare = async (post: PostResponse) => {
    try {
      await sharePostExternally(post);
    } catch (error) {
      if (isShareCancellation(error)) return;
      setToast({ message: t('social.shareUnavailable'), type: 'error' });
    }
  };

  // B32 : archivage depuis le menu « ⋯ » (posts possédés uniquement). Optimiste :
  // le backend exclut les posts archivés de GET /feed pour l'archiveur — cohérent au refresh.
  const handleArchive = async (postId: string) => {
    const previousPosts = posts;
    setPosts(prev => prev.filter(p => p.id !== postId));
    try {
      await socialApi.archivePost(postId);
      setToast({ message: t('social.postArchived'), type: 'success' });
    } catch (error: any) {
      setPosts(previousPosts); // rollback : le post revient dans le fil
      setToast({ message: error?.response?.data?.message || t('social.archiveError'), type: 'error' });
    }
  };

  const handleDelete = async (postId: string) => {
    try {
      await socialApi.deletePost(postId);
      setPosts(prev => prev.filter(p => p.id !== postId));
      setToast({ message: t('social.postDeleted'), type: 'success' });
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || t('social.deleteError'), type: 'error' });
    }
  };

  // B29 : épingler/désépingler depuis le menu « ⋯ » (posts possédés). Optimiste +
  // rollback ; la limite backend de 3 posts épinglés renvoie son message (affiché tel quel).
  const handlePin = async (post: PostResponse) => {
    const wasPinned = !!post.isPinned;
    setPosts(prev => prev.map(p => p.id === post.id ? { ...p, isPinned: !wasPinned } : p));
    try {
      const updated = wasPinned
        ? await socialApi.unpinPost(post.id)
        : await socialApi.pinPost(post.id);
      setPosts(prev => prev.map(p => p.id === post.id ? { ...p, isPinned: updated.isPinned } : p));
      setToast({ message: wasPinned ? t('social.postUnpinned') : t('social.postPinned'), type: 'success' });
    } catch (error: any) {
      setPosts(prev => prev.map(p => p.id === post.id ? { ...p, isPinned: wasPinned } : p)); // rollback
      setToast({ message: error?.response?.data?.message || t('social.pinError'), type: 'error' });
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header + Tabs */}
      <View style={[styles.headerSection, { backgroundColor: colors.surface }]}>
        <View style={styles.headerRow}>
          <TouchableOpacity style={styles.iconBtn}>
            <MaterialCommunityIcons name="menu" size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>{t('social.title')}</Text>
          <TouchableOpacity style={styles.iconBtn} onPress={() => setShowSearch(!showSearch)}>
            <MaterialCommunityIcons name={showSearch ? 'close' : 'magnify'} size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.iconBtn} onPress={() => router.push('/create-post')}>
            <MaterialCommunityIcons name="plus-circle-outline" size={24} color={colors.primary} />
          </TouchableOpacity>
        </View>

        {/* Search bar */}
        {showSearch && (
          <View style={styles.searchSection}>
            <TextInput
              style={[styles.searchInput, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              placeholder={t('social.searchPlaceholder')}
              placeholderTextColor={colors.onSurfaceVariant}
              value={searchQuery}
              onChangeText={setSearchQuery}
              autoFocus
            />
            {isSearching && <ActivityIndicator size="small" color={colors.primary} style={styles.searchSpinner} />}
            {searchResults && (
              <View style={[styles.searchResults, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
                {(searchResults.posts?.length || 0) > 0 && (
                  <>
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>{t('social.searchPosts')}</Text>
                    {searchResults.posts.slice(0, 3).map((p: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); router.push(`/post/${p.id}`); }}>
                        <Text style={[styles.searchItemText, { color: colors.onSurface }]} numberOfLines={1}>{p.content || p.title}</Text>
                      </TouchableOpacity>
                    ))}
                  </>
                )}
                {(searchResults.salons?.length || 0) > 0 && (
                  <>
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>{t('social.searchSalons')}</Text>
                    {searchResults.salons.slice(0, 3).map((s: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); router.push(`/salon/${s.id}`); }}>
                        <Text style={[styles.searchItemText, { color: colors.onSurface }]} numberOfLines={1}>{s.name}</Text>
                      </TouchableOpacity>
                    ))}
                  </>
                )}
                {(searchResults.users?.length || 0) > 0 && (
                  <>
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>{t('social.searchUsers')}</Text>
                    {searchResults.users.slice(0, 3).map((u: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); navigateToProfile(u.userType, u.id); }}>
                        <Text style={[styles.searchItemText, { color: colors.onSurface }]} numberOfLines={1}>{u.firstName} {u.lastName}</Text>
                      </TouchableOpacity>
                    ))}
                  </>
                )}
              </View>
            )}
          </View>
        )}

        {/* Tabs */}
        <View style={styles.tabRow}>
          {TAB_KEYS.map((tabKey, i) => (
            <TouchableOpacity key={tabKey} style={styles.tab} onPress={() => setActiveTab(i)}>
              <Text style={[styles.tabLabel, { color: i === activeTab ? colors.primary : colors.onSurfaceVariant }]}>{t(tabKey)}</Text>
              {i === activeTab && <View style={[styles.tabIndicator, { backgroundColor: colors.primary }]} />}
            </TouchableOpacity>
          ))}
        </View>

        {/* PostType filter chips */}
        {activeTab === 0 && (
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chipsRow}>
            {POST_TYPE_FILTER_KEYS.map((f, i) => (
              <TouchableOpacity
                key={f.i18nKey}
                style={[styles.chip, { backgroundColor: i === selectedPostType ? colors.primaryContainer : colors.surfaceContainerHigh }]}
                onPress={() => setSelectedPostType(i)}
              >
                <Text style={[styles.chipText, { color: i === selectedPostType ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>{t(f.i18nKey)}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        )}

        <View style={[styles.tabDivider, { backgroundColor: colors.outlineVariant }]} />
      </View>

      {/* Feed */}
      {isLoading && posts.length === 0 ? (
        <LoadingState />
      ) : hasError ? (
        <ErrorState message={t('social.feedLoadError')} onRetry={() => { setIsLoading(true); setPosts([]); loadPosts(0, true); }} />
      ) : posts.length === 0 ? (
        <EmptyState icon="newspaper-variant-outline" title={t('social.feedEmptyTitle')} message={t('social.feedEmptyMessage')} />
      ) : (
        <FlatList
          data={posts}
          keyExtractor={(p) => p.id}
          renderItem={({ item }) => (
            <PostCard
              post={item}
              currentUserId={user?.id}
              onProfilePress={() => navigateToProfile(item.authorUserType, item.authorId)}
              onLike={() => handleLike(item.id)}
              onComment={() => router.push(`/comments/${item.id}`)}
              onShare={() => handleShare(item)}
              onPress={() => router.push(`/post/${item.id}`)}
              onBookmark={() => handleBookmark(item.id)}
              onSaveToCollection={() => setSavePostId(item.id)}
              onArchive={() => handleArchive(item.id)}
              onPin={() => handlePin(item)}
              onDelete={() => handleDelete(item.id)}
              onReport={() => router.push({ pathname: '/report', params: { entityType: 'POST', entityId: item.id } })}
            />
          )}
          contentContainerStyle={styles.feedContent}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
          onEndReached={onEndReached}
          onEndReachedThreshold={0.5}
        />
      )}

      {/* Collection save dialog (composant partagé, B30) */}
      <CollectionPickerModal
        postId={savePostId}
        onClose={() => setSavePostId(null)}
        onFeedback={(message, type) => setToast({ message, type })}
      />

      {/* Toast feedback (succès / erreur) */}
      {toast && (
        <Toast message={toast.message} type={toast.type} visible onDismiss={() => setToast(null)} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  headerSection: { paddingTop: 6 },
  headerRow: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 8, minHeight: 52 },
  iconBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { flex: 1, fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600' },
  // Search
  searchSection: { paddingHorizontal: 12, paddingBottom: 8, position: 'relative' },
  searchInput: { borderWidth: 1, borderRadius: 12, paddingHorizontal: 14, paddingVertical: 10, fontSize: 14, fontFamily: 'Manrope-Regular' },
  searchSpinner: { position: 'absolute', end: 24, top: 12 },
  searchResults: { borderWidth: 1, borderRadius: 12, marginTop: 4, padding: 8, maxHeight: 250 },
  searchLabel: { fontFamily: 'Manrope-Bold', fontSize: 10.5, fontWeight: '700', letterSpacing: 1, textTransform: 'uppercase', paddingHorizontal: 8, paddingTop: 8, paddingBottom: 4 },
  searchItem: { paddingVertical: 10, paddingHorizontal: 8 },
  searchItemText: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  // Tabs
  tabRow: { flexDirection: 'row', paddingHorizontal: 8 },
  tab: { flex: 1, alignItems: 'center', paddingVertical: 12, position: 'relative' },
  tabLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  tabIndicator: { position: 'absolute', bottom: 0, start: '28%', end: '28%', height: 3, borderRadius: 3 },
  tabDivider: { height: 1 },
  // Chips
  chipsRow: { paddingHorizontal: 12, paddingVertical: 8, gap: 8 },
  chip: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999 },
  chipText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Feed
  feedContent: { paddingVertical: 12 },
});
