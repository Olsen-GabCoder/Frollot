import { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TextInput, ScrollView, TouchableOpacity, Pressable,
  StyleSheet, RefreshControl, Modal, ActivityIndicator,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { socialApi } from '../../src/api';
import { collectionsApi } from '../../src/api/portfolios';
import { PostCard } from '../../src/components/social';
import { Toast, type ToastType } from '../../src/components/ui';
import { LoadingState, EmptyState, ErrorState } from '../../src/components/lists';
import { PostResponse, PostType, SearchResponse } from '../../src/types';
import { sharePostExternally, isShareCancellation } from '../../src/utils/share';
import { useTheme } from '../../src/theme';

const TABS = ['Tous', 'Suivis', 'Tendances'];

const POST_TYPE_FILTERS: { label: string; value: PostType | undefined }[] = [
  { label: 'Tous', value: undefined },
  { label: 'Avant/Après', value: PostType.AVANT_APRES },
  { label: 'Réalisation', value: PostType.REALISATION },
  { label: 'Tendance', value: PostType.TENDANCE },
  { label: 'Conseil', value: PostType.CONSEIL },
  { label: 'Inspiration', value: PostType.INSPIRATION },
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

  // Collection save state
  const [savePostId, setSavePostId] = useState<string | null>(null);
  const [collections, setCollections] = useState<any[]>([]);
  const [isLoadingCollections, setIsLoadingCollections] = useState(false);

  // Toast feedback
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  // Load feed
  const loadPosts = useCallback(async (p = 0, refresh = false) => {
    setHasError(false);
    try {
      const postTypeFilter = POST_TYPE_FILTERS[selectedPostType]?.value;
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
      setToast({ message: error?.response?.data?.message || "Impossible de mettre à jour le j'aime.", type: 'error' });
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
      setToast({ message: error?.response?.data?.message || 'Impossible de mettre à jour le favori.', type: 'error' });
    }
  };

  // B31 : partage externe natif (menu système). Annulation = silencieux normal.
  const handleShare = async (post: PostResponse) => {
    try {
      await sharePostExternally(post);
    } catch (error) {
      if (isShareCancellation(error)) return;
      setToast({ message: "Le partage n'est pas disponible sur cet appareil.", type: 'error' });
    }
  };

  // B32 : archivage depuis le menu « ⋯ » (posts possédés uniquement). Optimiste :
  // le backend exclut les posts archivés de GET /feed pour l'archiveur — cohérent au refresh.
  const handleArchive = async (postId: string) => {
    const previousPosts = posts;
    setPosts(prev => prev.filter(p => p.id !== postId));
    try {
      await socialApi.archivePost(postId);
      setToast({ message: 'Post archivé. Retrouvez-le dans vos archives.', type: 'success' });
    } catch (error: any) {
      setPosts(previousPosts); // rollback : le post revient dans le fil
      setToast({ message: error?.response?.data?.message || "Impossible d'archiver ce post.", type: 'error' });
    }
  };

  const handleDelete = async (postId: string) => {
    try {
      await socialApi.deletePost(postId);
      setPosts(prev => prev.filter(p => p.id !== postId));
      setToast({ message: 'Post supprimé.', type: 'success' });
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || 'Impossible de supprimer le post.', type: 'error' });
    }
  };

  const handleSaveToCollection = async (collectionId: string, postId: string) => {
    setSavePostId(null);
    const collectionName = collections.find((c) => c.id === collectionId)?.name;
    try {
      await collectionsApi.addPostToCollection(collectionId, postId);
      setToast({ message: collectionName ? `Post ajouté à « ${collectionName} ».` : 'Post ajouté à la collection.', type: 'success' });
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || "Impossible d'ajouter le post à la collection.", type: 'error' });
    }
  };

  const openCollectionDialog = async (postId: string) => {
    setSavePostId(postId);
    if (user) {
      setIsLoadingCollections(true);
      try {
        const cols = await collectionsApi.getCollectionsByUser(user.id, true);
        setCollections(cols);
      } catch (error: any) {
        setCollections([]);
        setToast({ message: error?.response?.data?.message || 'Impossible de charger vos collections.', type: 'error' });
      } finally { setIsLoadingCollections(false); }
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
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>Fil social</Text>
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
              placeholder="Rechercher posts, salons, utilisateurs…"
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
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>Posts</Text>
                    {searchResults.posts.slice(0, 3).map((p: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); router.push(`/post/${p.id}`); }}>
                        <Text style={[styles.searchItemText, { color: colors.onSurface }]} numberOfLines={1}>{p.content || p.title}</Text>
                      </TouchableOpacity>
                    ))}
                  </>
                )}
                {(searchResults.salons?.length || 0) > 0 && (
                  <>
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>Salons</Text>
                    {searchResults.salons.slice(0, 3).map((s: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); router.push(`/salon/${s.id}`); }}>
                        <Text style={[styles.searchItemText, { color: colors.onSurface }]} numberOfLines={1}>{s.name}</Text>
                      </TouchableOpacity>
                    ))}
                  </>
                )}
                {(searchResults.users?.length || 0) > 0 && (
                  <>
                    <Text style={[styles.searchLabel, { color: colors.onSurfaceVariant }]}>Utilisateurs</Text>
                    {searchResults.users.slice(0, 3).map((u: any, i: number) => (
                      <TouchableOpacity key={i} style={styles.searchItem} onPress={() => { setShowSearch(false); setSearchQuery(''); }}>
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
          {TABS.map((tab, i) => (
            <TouchableOpacity key={i} style={styles.tab} onPress={() => setActiveTab(i)}>
              <Text style={[styles.tabLabel, { color: i === activeTab ? colors.primary : colors.onSurfaceVariant }]}>{tab}</Text>
              {i === activeTab && <View style={[styles.tabIndicator, { backgroundColor: colors.primary }]} />}
            </TouchableOpacity>
          ))}
        </View>

        {/* PostType filter chips */}
        {activeTab === 0 && (
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chipsRow}>
            {POST_TYPE_FILTERS.map((f, i) => (
              <TouchableOpacity
                key={i}
                style={[styles.chip, { backgroundColor: i === selectedPostType ? colors.primaryContainer : colors.surfaceContainerHigh }]}
                onPress={() => setSelectedPostType(i)}
              >
                <Text style={[styles.chipText, { color: i === selectedPostType ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>{f.label}</Text>
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
        <ErrorState message="Impossible de charger le fil" onRetry={() => { setIsLoading(true); setPosts([]); loadPosts(0, true); }} />
      ) : posts.length === 0 ? (
        <EmptyState icon="newspaper-variant-outline" title="Aucun post" message="Le fil est vide pour le moment." />
      ) : (
        <FlatList
          data={posts}
          keyExtractor={(p) => p.id}
          renderItem={({ item }) => (
            <PostCard
              post={item}
              currentUserId={user?.id}
              onLike={() => handleLike(item.id)}
              onComment={() => router.push(`/comments/${item.id}`)}
              onShare={() => handleShare(item)}
              onPress={() => router.push(`/post/${item.id}`)}
              onBookmark={() => handleBookmark(item.id)}
              onSaveToCollection={() => openCollectionDialog(item.id)}
              onArchive={() => handleArchive(item.id)}
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

      {/* Collection save dialog */}
      <Modal visible={!!savePostId} transparent animationType="fade" onRequestClose={() => setSavePostId(null)}>
        <Pressable style={styles.dialogOverlay} onPress={() => setSavePostId(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.dialogCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.dialogTitle, { color: colors.onSurface }]}>Enregistrer dans une collection</Text>
            {isLoadingCollections ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginVertical: 20 }} />
            ) : collections.length === 0 ? (
              <Text style={[styles.dialogEmpty, { color: colors.onSurfaceVariant }]}>Aucune collection. Créez-en une depuis votre profil.</Text>
            ) : (
              collections.map((col: any) => (
                <TouchableOpacity
                  key={col.id}
                  style={[styles.collectionItem, { borderBottomColor: colors.outlineVariant }]}
                  onPress={() => savePostId && handleSaveToCollection(col.id, savePostId)}
                >
                  <MaterialCommunityIcons name="folder-outline" size={20} color={colors.primary} />
                  <Text style={[styles.collectionName, { color: colors.onSurface }]}>{col.name}</Text>
                </TouchableOpacity>
              ))
            )}
            <TouchableOpacity onPress={() => setSavePostId(null)} style={styles.dialogClose}>
              <Text style={[styles.dialogCloseText, { color: colors.onSurfaceVariant }]}>Annuler</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>

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
  searchSpinner: { position: 'absolute', right: 24, top: 12 },
  searchResults: { borderWidth: 1, borderRadius: 12, marginTop: 4, padding: 8, maxHeight: 250 },
  searchLabel: { fontFamily: 'Manrope-Bold', fontSize: 10.5, fontWeight: '700', letterSpacing: 1, textTransform: 'uppercase', paddingHorizontal: 8, paddingTop: 8, paddingBottom: 4 },
  searchItem: { paddingVertical: 10, paddingHorizontal: 8 },
  searchItemText: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  // Tabs
  tabRow: { flexDirection: 'row', paddingHorizontal: 8 },
  tab: { flex: 1, alignItems: 'center', paddingVertical: 12, position: 'relative' },
  tabLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  tabIndicator: { position: 'absolute', bottom: 0, left: '28%', right: '28%', height: 3, borderRadius: 3 },
  tabDivider: { height: 1 },
  // Chips
  chipsRow: { paddingHorizontal: 12, paddingVertical: 8, gap: 8 },
  chip: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999 },
  chipText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Feed
  feedContent: { paddingVertical: 12 },
  // Dialog
  dialogOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  dialogCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  dialogTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 20, fontWeight: '600', marginBottom: 12 },
  dialogEmpty: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', paddingVertical: 20 },
  collectionItem: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 14, borderBottomWidth: 1 },
  collectionName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  dialogClose: { alignItems: 'center', paddingTop: 16 },
  dialogCloseText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
