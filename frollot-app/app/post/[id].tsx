import { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';
import { useAuthStore } from '../../src/stores/authStore';
import { socialApi } from '../../src/api/social';
import { sharePostExternally, isShareCancellation } from '../../src/utils/share';
import { Toast, type ToastType } from '../../src/components/ui';
import { PostResponse, CommentResponse } from '../../src/types';

export default function PostDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { user } = useAuthStore();
  const { colors, typography: typo } = theme;
  const flatListRef = useRef<FlatList>(null);

  const [post, setPost] = useState<PostResponse | null>(null);
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newComment, setNewComment] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: ToastType } | null>(null);

  const loadPost = async () => {
    if (!id) return;
    try {
      setIsLoading(true);
      const [postData, commentsData] = await Promise.all([
        socialApi.getPostById(id),
        socialApi.getCommentsByPost(id, 0, 50),
      ]);
      setPost(postData);
      setComments(commentsData.content);
      setHasMore(!commentsData.last);
    } catch (e: any) {
      setError(e?.message || t('common.error'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadPost();
  }, [id]);

  const loadMoreComments = async () => {
    if (!id || !hasMore || isLoadingMore) return;
    setIsLoadingMore(true);
    try {
      const nextPage = currentPage + 1;
      const result = await socialApi.getCommentsByPost(id, nextPage, 50);
      setComments((prev) => [...prev, ...result.content]);
      setCurrentPage(nextPage);
      setHasMore(!result.last);
    } catch {} finally {
      setIsLoadingMore(false);
    }
  };

  // B27 : fusion sélective — la réponse de toggleLike est partielle (pas de media),
  // on ne reprend que isLikedByCurrentUser + likesCount pour ne pas effacer le reste.
  const handleLike = async () => {
    if (!id || !post) return;
    try {
      const updated = await socialApi.toggleLike(id);
      setPost((prev) => prev ? { ...prev, isLikedByCurrentUser: updated.isLikedByCurrentUser, likesCount: updated.likesCount } : prev);
    } catch (e: any) {
      setToast({ message: e?.response?.data?.message || "Impossible de mettre à jour le j'aime.", type: 'error' });
    }
  };

  // B28 : fusion sélective — la réponse de toggleFavorite est partielle (pas de media),
  // remplacer le post entier effaçait l'image. Pas de compteur côté favoris.
  const handleFavorite = async () => {
    if (!id || !post) return;
    try {
      const updated = await socialApi.toggleFavorite(id);
      setPost((prev) => prev ? { ...prev, isFavoritedByCurrentUser: updated.isFavoritedByCurrentUser } : prev);
    } catch (e: any) {
      setToast({ message: e?.response?.data?.message || 'Impossible de mettre à jour le favori.', type: 'error' });
    }
  };

  // B31 : partage externe natif (remplace l'ancien toggle interne share/unshare,
  // réservé au futur repartage-profil). Annulation = silencieux normal.
  const handleShare = async () => {
    if (!post) return;
    try {
      await sharePostExternally(post);
    } catch (error) {
      if (isShareCancellation(error)) return;
      setToast({ message: "Le partage n'est pas disponible sur cet appareil.", type: 'error' });
    }
  };

  const handleSubmitComment = async () => {
    if (!id || !user || !newComment.trim()) return;
    setIsSubmitting(true);
    try {
      await socialApi.createComment(id, {
        postId: id,
        authorId: user.id,
        content: newComment.trim(),
      });
      setNewComment('');
      // Reload comments
      const result = await socialApi.getCommentsByPost(id, 0, 50);
      setComments(result.content);
      setCurrentPage(0);
      setHasMore(!result.last);
    } catch {} finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    Alert.alert(t('common.delete'), t('common.confirm'), [
      { text: t('common.cancel'), style: 'cancel' },
      {
        text: t('common.delete'),
        style: 'destructive',
        onPress: async () => {
          try {
            await socialApi.deleteComment(commentId);
            setComments((prev) => prev.filter((c) => c.id !== commentId));
          } catch {}
        },
      },
    ]);
  };

  if (isLoading) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (error || !post) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[typo.bodyLarge, { color: colors.error }]}>{error || t('common.error')}</Text>
        <TouchableOpacity style={[styles.retryBtn, { backgroundColor: colors.primary }]} onPress={loadPost}>
          <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>{t('common.retry')}</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={0}
    >
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>Publication</Text>
      </View>

      <FlatList
        ref={flatListRef}
        data={comments}
        keyExtractor={(item) => item.id}
        ListHeaderComponent={() => (
          <View>
            {/* Post card */}
            <View style={[styles.postCard, { backgroundColor: colors.surface }]}>
              {/* Author */}
              <View style={styles.authorRow}>
                <View style={[styles.authorAvatar, { backgroundColor: colors.secondaryContainer }]}>
                  <Text style={[typo.labelMedium, { color: colors.onSecondaryContainer }]}>
                    {(post.authorName?.[0] || '?').toUpperCase()}
                  </Text>
                </View>
                <View style={{ flex: 1, marginLeft: 10 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{post.authorName}</Text>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{post.authorUserType}</Text>
                </View>
              </View>

              {/* Content */}
              <Text style={[typo.bodyLarge, { color: colors.onSurface, marginVertical: 12 }]}>
                {post.content}
              </Text>

              {/* Media */}
              {post.media && post.media.length > 0 && (
                <ScrollableMediaGallery media={post.media} colors={colors} />
              )}
              {!post.media?.length && post.imageUrl && (
                <Image source={{ uri: resolveMediaUrl(post.imageUrl) }} style={styles.postImage} contentFit="contain" />
              )}

              {/* Engagement bar */}
              <View style={styles.engagementBar}>
                <TouchableOpacity style={styles.engagementBtn} onPress={handleLike}>
                  <MaterialIcons
                    name={post.isLikedByCurrentUser ? 'favorite' : 'favorite-border'}
                    size={22}
                    color={post.isLikedByCurrentUser ? colors.error : colors.onSurfaceVariant}
                  />
                  <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>
                    {post.likesCount}
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.engagementBtn}>
                  <MaterialIcons name="chat-bubble-outline" size={22} color={colors.onSurfaceVariant} />
                  <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>
                    {post.commentsCount}
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.engagementBtn} onPress={handleShare}>
                  {/* B31 : partage externe — teinte isSharedByCurrentUser retirée (état interne
                      que cette action ne pilote plus, reviendra avec le repartage-profil) */}
                  <MaterialIcons name="share" size={22} color={colors.onSurfaceVariant} />
                </TouchableOpacity>
                <TouchableOpacity style={styles.engagementBtn} onPress={handleFavorite}>
                  <MaterialIcons
                    name={post.isFavoritedByCurrentUser ? 'bookmark' : 'bookmark-border'}
                    size={22}
                    color={post.isFavoritedByCurrentUser ? colors.tertiary : colors.onSurfaceVariant}
                  />
                </TouchableOpacity>
              </View>
            </View>

            {/* Comments header */}
            <Text style={[typo.titleMedium, { color: colors.onBackground, padding: 16 }]}>
              {t('social.comments')} ({comments.length})
            </Text>
          </View>
        )}
        renderItem={({ item }) => (
          <View style={[styles.commentCard, { backgroundColor: colors.surface }]}>
            <View style={styles.commentHeader}>
              <View style={[styles.commentAvatar, { backgroundColor: colors.tertiaryContainer }]}>
                <Text style={[typo.labelSmall, { color: colors.onTertiaryContainer }]}>
                  {(item.authorName?.[0] || '?').toUpperCase()}
                </Text>
              </View>
              <View style={{ flex: 1, marginLeft: 8 }}>
                <Text style={[typo.labelMedium, { color: colors.onSurface }]}>{item.authorName}</Text>
              </View>
              {item.authorId === user?.id && (
                <TouchableOpacity onPress={() => handleDeleteComment(item.id)}>
                  <MaterialIcons name="delete-outline" size={18} color={colors.error} />
                </TouchableOpacity>
              )}
            </View>
            <Text style={[typo.bodyMedium, { color: colors.onSurface, marginTop: 4 }]}>
              {item.content}
            </Text>
          </View>
        )}
        ListEmptyComponent={
          <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', padding: 32 }]}>
            {t('social.noComments')}
          </Text>
        }
        ListFooterComponent={
          hasMore ? (
            <TouchableOpacity style={styles.loadMoreBtn} onPress={loadMoreComments}>
              {isLoadingMore ? (
                <ActivityIndicator size="small" color={colors.primary} />
              ) : (
                <Text style={[typo.labelMedium, { color: colors.primary }]}>Charger plus</Text>
              )}
            </TouchableOpacity>
          ) : null
        }
        contentContainerStyle={styles.listContent}
      />

      {/* Comment input */}
      <View style={[styles.commentInput, { backgroundColor: colors.surface, borderTopColor: colors.outlineVariant }]}>
        <View style={[styles.commentAvatar, { backgroundColor: colors.primaryContainer }]}>
          <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>
            {(user?.firstName?.[0] || 'F').toUpperCase()}
          </Text>
        </View>
        <TextInput
          style={[styles.commentTextField, {
            backgroundColor: colors.surfaceContainerHigh,
            color: colors.onSurface,
          }]}
          placeholder={t('social.writeComment')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={newComment}
          onChangeText={setNewComment}
          multiline
        />
        <TouchableOpacity
          onPress={handleSubmitComment}
          disabled={isSubmitting || !newComment.trim()}
        >
          {isSubmitting ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <MaterialIcons
              name="send"
              size={24}
              color={newComment.trim() ? colors.primary : colors.onSurfaceVariant}
            />
          )}
        </TouchableOpacity>
      </View>

      {/* Toast feedback (erreur like) */}
      {toast && (
        <Toast message={toast.message} type={toast.type} visible onDismiss={() => setToast(null)} />
      )}
    </KeyboardAvoidingView>
  );
}

// Simple horizontal media gallery
function ScrollableMediaGallery({ media, colors }: { media: any[]; colors: any }) {
  return (
    <View style={styles.mediaRow}>
      {media.map((m: any, i: number) => (
        <Image key={i} source={{ uri: resolveMediaUrl(m.mediaUrl) }} style={styles.galleryImage} contentFit="cover" />
      ))}
    </View>
  );
}

import { ScrollView } from 'react-native';

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  retryBtn: { marginTop: 16, paddingHorizontal: 24, paddingVertical: 12, borderRadius: 28 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  listContent: { paddingBottom: 80 },
  postCard: { padding: 16, marginBottom: 4 },
  authorRow: { flexDirection: 'row', alignItems: 'center' },
  authorAvatar: { width: 40, height: 40, borderRadius: 20, justifyContent: 'center', alignItems: 'center' },
  postImage: { width: '100%', height: 400, borderRadius: 12, marginBottom: 8 },
  mediaRow: { flexDirection: 'row', gap: 8, marginBottom: 8 },
  galleryImage: { flex: 1, height: 300, borderRadius: 12, minWidth: 140 },
  engagementBar: { flexDirection: 'row', justifyContent: 'space-between', paddingTop: 12 },
  engagementBtn: { flexDirection: 'row', alignItems: 'center' },
  commentCard: { marginHorizontal: 16, marginBottom: 8, padding: 12, borderRadius: 12 },
  commentHeader: { flexDirection: 'row', alignItems: 'center' },
  commentAvatar: { width: 28, height: 28, borderRadius: 14, justifyContent: 'center', alignItems: 'center' },
  loadMoreBtn: { alignItems: 'center', padding: 16 },
  commentInput: {
    flexDirection: 'row', alignItems: 'center', padding: 12, gap: 8,
    borderTopWidth: 1,
  },
  commentTextField: { flex: 1, borderRadius: 20, paddingHorizontal: 16, paddingVertical: 8, maxHeight: 80, fontSize: 14 },
});
