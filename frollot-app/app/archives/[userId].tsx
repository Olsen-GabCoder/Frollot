import { useEffect, useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, Pressable,
  StyleSheet, RefreshControl, Modal, ActivityIndicator,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { socialApi } from '../../src/api/social';
import { PostCard } from '../../src/components/social';
import { Toast, type ToastType } from '../../src/components/ui';
import { LoadingState, EmptyState, ErrorState } from '../../src/components/lists';
import { PostResponse } from '../../src/types';
import { sharePostExternally, isShareCancellation } from '../../src/utils/share';

type PendingAction = { type: 'unarchive' | 'delete'; post: PostResponse } | null;

export default function ArchivesScreen() {
  const { userId } = useLocalSearchParams<{ userId: string }>();
  const { colors, typography: typo } = useTheme();
  const { user } = useAuthStore();

  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  // Modal de confirmation (désarchiver / supprimer)
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [isActing, setIsActing] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  // Toast feedback (erreur like — B27)
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  const loadArchives = useCallback(async (p = 0, refresh = false) => {
    if (!userId) return;
    setHasError(false);
    try {
      const data = await socialApi.getArchivedPosts(userId, p, 20);
      setPosts((prev) => (refresh ? data.content : [...prev, ...data.content]));
      setHasMore(!data.last);
      setPage(p);
    } catch {
      // Backend owner-only : 403 si on tente de voir les archives d'autrui
      setHasError(true);
    } finally {
      setIsLoading(false);
      setRefreshing(false);
    }
  }, [userId]);

  useEffect(() => { setIsLoading(true); loadArchives(0, true); }, [loadArchives]);

  const onRefresh = () => { setRefreshing(true); loadArchives(0, true); };
  const onEndReached = () => { if (hasMore && !isLoading && !refreshing) loadArchives(page + 1); };

  // Mise à jour optimiste : retrait immédiat de la liste, rollback si le serveur refuse
  const confirmAction = async () => {
    if (!pendingAction) return;
    const { type, post } = pendingAction;
    const previousPosts = posts;
    setIsActing(true);
    setActionError(null);
    setPosts((prev) => prev.filter((p) => p.id !== post.id));
    try {
      if (type === 'unarchive') await socialApi.unarchivePost(post.id);
      else await socialApi.deletePost(post.id);
      setPendingAction(null);
    } catch {
      setPosts(previousPosts); // rollback
      setActionError(type === 'unarchive'
        ? 'Impossible de désarchiver ce post. Réessayez.'
        : 'Impossible de supprimer ce post. Réessayez.');
    } finally {
      setIsActing(false);
    }
  };

  const closeModal = () => { if (!isActing) { setPendingAction(null); setActionError(null); } };

  // B27 : fusion sélective depuis la réponse serveur (source de vérité du like)
  const handleLike = async (postId: string) => {
    try {
      const updated = await socialApi.toggleLike(postId);
      setPosts((prev) => prev.map((p) => p.id === postId
        ? { ...p, isLikedByCurrentUser: updated.isLikedByCurrentUser, likesCount: updated.likesCount }
        : p));
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || "Impossible de mettre à jour le j'aime.", type: 'error' });
    }
  };

  // B30 : signet = favori partout — le signet du PostCard était mort ici (onBookmark jamais passé)
  const handleBookmark = async (postId: string) => {
    try {
      const updated = await socialApi.toggleFavorite(postId);
      setPosts((prev) => prev.map((p) => p.id === postId
        ? { ...p, isFavoritedByCurrentUser: updated.isFavoritedByCurrentUser }
        : p));
    } catch (error: any) {
      setToast({ message: error?.response?.data?.message || 'Impossible de mettre à jour le favori.', type: 'error' });
    }
  };

  // B31 : partage externe natif. Annulation = silencieux normal.
  const handleShare = async (post: PostResponse) => {
    try {
      await sharePostExternally(post);
    } catch (error) {
      if (isShareCancellation(error)) return;
      setToast({ message: "Le partage n'est pas disponible sur cet appareil.", type: 'error' });
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface, borderBottomColor: colors.outlineVariant }]}>
        <TouchableOpacity style={styles.iconBtn} onPress={goBack}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <View style={styles.headerTexts}>
          <Text style={[typo.overline, { color: colors.secondary }]}>Profil</Text>
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>Archives</Text>
        </View>
      </View>

      {isLoading && posts.length === 0 ? (
        <LoadingState />
      ) : hasError ? (
        <ErrorState
          message="Impossible de charger vos archives"
          onRetry={() => { setIsLoading(true); loadArchives(0, true); }}
        />
      ) : posts.length === 0 ? (
        <EmptyState
          icon="archive-outline"
          title="Aucune archive"
          message="Les posts que vous archivez apparaîtront ici."
        />
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
              onUnarchive={() => setPendingAction({ type: 'unarchive', post: item })}
              onDelete={() => setPendingAction({ type: 'delete', post: item })}
            />
          )}
          contentContainerStyle={styles.listContent}
          ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={colors.primary} />}
          onEndReached={onEndReached}
          onEndReachedThreshold={0.5}
        />
      )}

      {/* Confirmation désarchiver / supprimer (pattern B22) */}
      <Modal visible={!!pendingAction} transparent animationType="fade" onRequestClose={closeModal}>
        <Pressable style={styles.modalOverlay} onPress={closeModal}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <View style={[styles.modalIconCircle, {
              backgroundColor: pendingAction?.type === 'delete' ? colors.errorContainer : colors.primaryContainer,
            }]}>
              <MaterialCommunityIcons
                name={pendingAction?.type === 'delete' ? 'delete-outline' : 'archive-arrow-up-outline'}
                size={26}
                color={pendingAction?.type === 'delete' ? colors.error : colors.primary}
              />
            </View>
            <Text style={[styles.modalTitle, { color: colors.onSurface }]}>
              {pendingAction?.type === 'delete' ? 'Supprimer ce post ?' : 'Désarchiver ce post ?'}
            </Text>
            <Text style={[styles.modalBody, { color: colors.onSurfaceVariant }]}>
              {pendingAction?.type === 'delete'
                ? 'Le post sera définitivement supprimé. Cette action est irréversible.'
                : 'Le post redeviendra visible dans le fil et sur votre profil.'}
            </Text>
            {actionError && (
              <View style={[styles.modalErrorCard, { backgroundColor: colors.errorContainer }]}>
                <Text style={[styles.modalErrorText, { color: colors.onErrorContainer }]}>{actionError}</Text>
              </View>
            )}
            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={closeModal}
                disabled={isActing}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>Annuler</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: pendingAction?.type === 'delete' ? colors.error : colors.primary }]}
                onPress={confirmAction}
                disabled={isActing}
              >
                {isActing ? (
                  <ActivityIndicator size="small" color={colors.onPrimary} />
                ) : (
                  <Text style={[styles.modalBtnText, { color: colors.onPrimary }]}>
                    {pendingAction?.type === 'delete' ? 'Supprimer' : 'Désarchiver'}
                  </Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Toast feedback (erreur like) */}
      {toast && (
        <Toast message={toast.message} type={toast.type} visible onDismiss={() => setToast(null)} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    paddingTop: 52, paddingBottom: 12, paddingHorizontal: 8,
    borderBottomWidth: 1,
  },
  iconBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTexts: { flex: 1 },
  headerTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600', marginTop: 1 },
  listContent: { paddingVertical: 12, paddingBottom: 40 },
  // Modal (B22)
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24, alignItems: 'center' },
  modalIconCircle: { width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', marginBottom: 14 },
  modalTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', textAlign: 'center' },
  modalBody: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 21, textAlign: 'center', marginTop: 8 },
  modalErrorCard: { borderRadius: 12, paddingVertical: 10, paddingHorizontal: 14, marginTop: 14, alignSelf: 'stretch' },
  modalErrorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', textAlign: 'center' },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 20, alignSelf: 'stretch' },
  modalBtn: { flex: 1, height: 48, borderRadius: 999, alignItems: 'center', justifyContent: 'center' },
  modalBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
