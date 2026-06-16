import { useEffect, useRef, useState } from 'react';
import {
  View, Text, FlatList, ScrollView, TouchableOpacity,
  StyleSheet, RefreshControl, ActivityIndicator, I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { LinearGradient } from 'expo-linear-gradient';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../../src/stores/authStore';
import { socialApi } from '../../../src/api/social';
import { salonsApi } from '../../../src/api/salons';
import { PostCard, CollectionPickerModal } from '../../../src/components/social';
import { Toast, type ToastType } from '../../../src/components/ui';
import { LoadingState, EmptyState, ErrorState } from '../../../src/components/lists';
import { Salon, SalonService, PostResponse, PostType, SortBy } from '../../../src/types';
import { sharePostExternally, isShareCancellation } from '../../../src/utils/share';
import { navigateToProfile } from '../../../src/utils/navigateToProfile';
import { resolveMediaUrl } from '../../../src/utils/media';
import { useTheme } from '../../../src/theme';

const PAGE_SIZE = 20;

// Mêmes chips que le fil social (référence UX figée) : « Tous » + 5 types publics.
const POST_TYPE_FILTER_KEYS: { i18nKey: string; value: PostType | undefined }[] = [
  { i18nKey: 'social.postTypes.all', value: undefined },
  { i18nKey: 'social.postTypes.avantApres', value: PostType.AVANT_APRES },
  { i18nKey: 'social.postTypes.realisation', value: PostType.REALISATION },
  { i18nKey: 'social.postTypes.tendance', value: PostType.TENDANCE },
  { i18nKey: 'social.postTypes.conseil', value: PostType.CONSEIL },
  { i18nKey: 'social.postTypes.inspiration', value: PostType.INSPIRATION },
];

export default function SalonPostsScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  // Salon (header) + services (filtre) — rechargés ici : pas de dépendance à un param
  // de route, l'accès par URL directe doit fonctionner.
  const [salon, setSalon] = useState<Salon | null>(null);
  const [services, setServices] = useState<SalonService[]>([]);

  // Posts + pagination
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [refreshTick, setRefreshTick] = useState(0);

  // Filtres (parité KMP) : type de post, service, tri
  const [selectedPostType, setSelectedPostType] = useState<PostType | undefined>(undefined);
  const [selectedServiceId, setSelectedServiceId] = useState<string | null>(null);
  const [sortBy, setSortBy] = useState<SortBy>(SortBy.RECENT);

  // Compagnons du jeu d'actions (patron social.tsx)
  const [savePostId, setSavePostId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  // Clé des filtres courants — garde anti-réponse-périmée pour la pagination.
  const filtersKey = `${selectedPostType ?? ''}|${selectedServiceId ?? ''}|${sortBy}`;
  const filtersRef = useRef(filtersKey);
  filtersRef.current = filtersKey;

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  // Salon + services (une fois)
  useEffect(() => {
    if (!id) return;
    let ignore = false;
    (async () => {
      try {
        const s = await salonsApi.getSalonById(id);
        if (!ignore) setSalon(s);
      } catch {}
      try {
        const svcs = await salonsApi.getSalonServices(id);
        if (!ignore) setServices(svcs);
      } catch {}
    })();
    return () => { ignore = true; };
  }, [id]);

  // Chargement page 0 — relancé à chaque changement de filtre (reset) ou refresh.
  // Flag ignore au cleanup : les réponses obsolètes sont jetées.
  useEffect(() => {
    if (!id) return;
    let ignore = false;
    setHasError(false);
    if (refreshTick === 0) setIsLoading(true);
    (async () => {
      try {
        const data = await socialApi.getPostsBySalon(id, {
          postType: selectedPostType,
          serviceId: selectedServiceId ?? undefined,
          sortBy,
          page: 0,
          size: PAGE_SIZE,
        });
        if (!ignore) {
          setPosts(data.content || []);
          setHasMore(!data.last);
          setPage(0);
        }
      } catch {
        if (!ignore) setHasError(true);
      } finally {
        if (!ignore) { setIsLoading(false); setRefreshing(false); }
      }
    })();
    return () => { ignore = true; };
  }, [id, selectedPostType, selectedServiceId, sortBy, refreshTick]);

  const onRefresh = () => { setRefreshing(true); setRefreshTick(t => t + 1); };

  // Infinite scroll : page suivante tant que !last.
  const loadMore = async () => {
    if (!id || isLoading || refreshing || isLoadingMore || !hasMore) return;
    const key = filtersRef.current;
    const next = page + 1;
    setIsLoadingMore(true);
    try {
      const data = await socialApi.getPostsBySalon(id, {
        postType: selectedPostType,
        serviceId: selectedServiceId ?? undefined,
        sortBy,
        page: next,
        size: PAGE_SIZE,
      });
      if (filtersRef.current !== key) return; // filtres changés entre-temps : réponse périmée
      setPosts(prev => [...prev, ...(data.content || [])]);
      setHasMore(!data.last);
      setPage(next);
    } catch {} finally {
      setIsLoadingMore(false);
    }
  };

  // ----- Jeu de handlers complet (patron app/(tabs)/social.tsx, B27→B34) -----

  // B27 : fusion SÉLECTIVE — la réponse de toggleLike est partielle (pas de media).
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

  // B30 : signet = favori. Fusion sélective (réponse partielle).
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

  // B31 : partage externe natif. Annulation = silencieux normal.
  const handleShare = async (post: PostResponse) => {
    try {
      await sharePostExternally(post);
    } catch (error) {
      if (isShareCancellation(error)) return;
      setToast({ message: t('social.shareUnavailable'), type: 'error' });
    }
  };

  // B32 : archivage (posts possédés). Optimiste + rollback.
  const handleArchive = async (postId: string) => {
    const previousPosts = posts;
    setPosts(prev => prev.filter(p => p.id !== postId));
    try {
      await socialApi.archivePost(postId);
      setToast({ message: t('social.postArchived'), type: 'success' });
    } catch (error: any) {
      setPosts(previousPosts); // rollback
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

  // B29 : épingler/désépingler. Optimiste + rollback ; message backend (limite 3) affiché tel quel.
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

  const isOwner = !!user && !!salon && user.id === salon.ownerId;

  // Cover visuelle (parité KMP) — défile avec la liste.
  const listHeader = (
    <View style={s.coverWrap}>
      {salon?.coverPhotoUrl ? (
        <Image source={{ uri: resolveMediaUrl(salon.coverPhotoUrl) }} style={s.coverImage} contentFit="cover" />
      ) : (
        <View style={[s.coverImage, { backgroundColor: colors.primaryContainer }]} />
      )}
      {/* design-fixed — editorial cover gradient overlay */}
      <LinearGradient
        colors={['transparent', 'rgba(40,23,51,0.55)']}
        style={StyleSheet.absoluteFill}
      />
      {salon && (
        <Text style={s.coverName} numberOfLines={1}>{salon.name}</Text>
      )}
    </View>
  );

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header fixe : retour (garde B24b) + titre */}
      <View style={[s.headerRow, { backgroundColor: colors.surface, borderBottomColor: colors.outlineVariant }]}>
        <TouchableOpacity style={s.iconBtn} onPress={goBack}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? "arrow-right" : "arrow-left"} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.headerTitle, { color: colors.onSurface }]} numberOfLines={1}>
          {salon ? t('salon.postsHeaderWithName', { name: salon.name }) : t('salon.postsHeader')}
        </Text>
      </View>

      {/* Filtres fixes : type de post / service / tri */}
      <View style={[s.filtersBlock, { backgroundColor: colors.surface, borderBottomColor: colors.outlineVariant }]}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={s.chipsRow}>
          {POST_TYPE_FILTER_KEYS.map((f) => {
            const active = selectedPostType === f.value;
            return (
              <TouchableOpacity
                key={f.i18nKey}
                style={[s.chip, { backgroundColor: active ? colors.primary : colors.surfaceVariant }]}
                onPress={() => setSelectedPostType(f.value)}
              >
                <Text style={[s.chipText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>{t(f.i18nKey)}</Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={s.chipsRow}>
          <TouchableOpacity
            style={[s.chip, { backgroundColor: sortBy === SortBy.RECENT ? colors.secondaryContainer : colors.surfaceVariant }]}
            onPress={() => setSortBy(SortBy.RECENT)}
          >
            <Text style={[s.chipText, { color: sortBy === SortBy.RECENT ? colors.onSecondaryContainer : colors.onSurfaceVariant }]}>{t('social.sort.recent')}</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[s.chip, { backgroundColor: sortBy === SortBy.POPULAR ? colors.secondaryContainer : colors.surfaceVariant }]}
            onPress={() => setSortBy(SortBy.POPULAR)}
          >
            <Text style={[s.chipText, { color: sortBy === SortBy.POPULAR ? colors.onSecondaryContainer : colors.onSurfaceVariant }]}>{t('social.sort.popular')}</Text>
          </TouchableOpacity>
          {services.length > 0 && (
            <>
              <View style={[s.chipDivider, { backgroundColor: colors.outlineVariant }]} />
              <TouchableOpacity
                style={[s.chip, { backgroundColor: selectedServiceId === null ? colors.tertiaryContainer : colors.surfaceVariant }]}
                onPress={() => setSelectedServiceId(null)}
              >
                <Text style={[s.chipText, { color: selectedServiceId === null ? colors.onTertiaryContainer : colors.onSurfaceVariant }]}>{t('salon.allServices')}</Text>
              </TouchableOpacity>
              {services.map((svc) => {
                const active = selectedServiceId === svc.id;
                return (
                  <TouchableOpacity
                    key={svc.id}
                    style={[s.chip, { backgroundColor: active ? colors.tertiaryContainer : colors.surfaceVariant }]}
                    onPress={() => setSelectedServiceId(active ? null : svc.id)}
                  >
                    <Text style={[s.chipText, { color: active ? colors.onTertiaryContainer : colors.onSurfaceVariant }]} numberOfLines={1}>{svc.name}</Text>
                  </TouchableOpacity>
                );
              })}
            </>
          )}
        </ScrollView>
      </View>

      {/* Liste (racine FlatList — pas de ScrollView parent) */}
      {isLoading && posts.length === 0 ? (
        <LoadingState />
      ) : hasError && posts.length === 0 ? (
        <ErrorState message={t('salon.postsLoadError')} onRetry={() => { setIsLoading(true); setRefreshTick(tk => tk + 1); }} />
      ) : posts.length === 0 ? (
        <EmptyState
          icon="newspaper-variant-outline"
          title={t('salon.noPostsTitle')}
          message={t('salon.noPostsMessage')}
        />
      ) : (
        <FlatList
          data={posts}
          keyExtractor={(p) => p.id}
          ListHeaderComponent={listHeader}
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
          contentContainerStyle={s.feedContent}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
          onEndReached={loadMore}
          onEndReachedThreshold={0.5}
          ListFooterComponent={isLoadingMore ? (
            <ActivityIndicator size="small" color={colors.primary} style={{ paddingVertical: 16 }} />
          ) : null}
        />
      )}

      {/* FAB créer un post — owner uniquement (parité FAB KMP) */}
      {isOwner && (
        <TouchableOpacity
          style={[s.fab, { backgroundColor: colors.primary }]}
          onPress={() => router.push('/create-post')}
        >
          <MaterialCommunityIcons name="plus" size={26} color={colors.onPrimary} />
        </TouchableOpacity>
      )}

      {/* Collection save dialog (composant partagé, B34) */}
      <CollectionPickerModal
        postId={savePostId}
        onClose={() => setSavePostId(null)}
        onFeedback={(message, type) => setToast({ message, type })}
      />

      {/* Toast feedback */}
      {toast && (
        <Toast message={toast.message} type={toast.type} visible onDismiss={() => setToast(null)} />
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  // Header
  headerRow: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 8, minHeight: 52, borderBottomWidth: 1 },
  iconBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { flex: 1, fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600' },
  // Filtres
  filtersBlock: { paddingVertical: 6, gap: 6, borderBottomWidth: 1 },
  chipsRow: { paddingHorizontal: 12, gap: 8, alignItems: 'center' },
  chip: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999, maxWidth: 180 },
  chipText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  chipDivider: { width: 1, height: 20, marginHorizontal: 2 },
  // Cover (parité KMP)
  coverWrap: { position: 'relative', height: 160, borderRadius: 16, overflow: 'hidden', marginHorizontal: 12, marginBottom: 12 },
  coverImage: { width: '100%', height: 160 },
  coverName: { position: 'absolute', bottom: 12, start: 14, end: 14, fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600', color: '#FFFFFF' }, // design-fixed
  // Liste
  feedContent: { paddingVertical: 12 },
  // FAB
  fab: { position: 'absolute', end: 16, bottom: 24, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 4, shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.2, shadowRadius: 8 }, // design-fixed
});
