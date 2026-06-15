import { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { socialApi } from '../../src/api/social';
import { PostResponse, CommentResponse } from '../../src/types';

export default function CommentsScreen() {
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

  const loadData = async () => {
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
      setError(e?.message || t('common.states.error'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const loadMore = async () => {
    if (!id || !hasMore || isLoadingMore) return;
    setIsLoadingMore(true);
    try {
      const next = currentPage + 1;
      const result = await socialApi.getCommentsByPost(id, next, 50);
      setComments((prev) => [...prev, ...result.content]);
      setCurrentPage(next);
      setHasMore(!result.last);
    } catch {} finally {
      setIsLoadingMore(false);
    }
  };

  const handleSubmit = async () => {
    if (!id || !user || !newComment.trim()) return;
    setIsSubmitting(true);
    try {
      await socialApi.createComment(id, {
        postId: id,
        authorId: user.id,
        content: newComment.trim(),
      });
      setNewComment('');
      const result = await socialApi.getCommentsByPost(id, 0, 50);
      setComments(result.content);
      setCurrentPage(0);
      setHasMore(!result.last);
      flatListRef.current?.scrollToOffset({ offset: 0, animated: true });
    } catch {} finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (commentId: string) => {
    try {
      await socialApi.deleteComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch {}
  };

  if (isLoading) {
    return (
      <View style={[styles.centered, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>
          {t('social.comments')}
        </Text>
      </View>

      <FlatList
        ref={flatListRef}
        data={comments}
        keyExtractor={(item) => item.id}
        ListHeaderComponent={
          post ? (
            <View style={[styles.postPreview, { backgroundColor: colors.surface }]}>
              <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{post.authorName}</Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 4 }]} numberOfLines={3}>
                {post.content}
              </Text>
            </View>
          ) : null
        }
        renderItem={({ item }) => (
          <View style={[styles.commentCard, { backgroundColor: colors.surface }]}>
            <View style={styles.commentRow}>
              <View style={[styles.avatar, { backgroundColor: colors.tertiaryContainer }]}>
                <Text style={[typo.labelSmall, { color: colors.onTertiaryContainer }]}>
                  {(item.authorName?.[0] || '?').toUpperCase()}
                </Text>
              </View>
              <View style={{ flex: 1, marginStart: 8 }}>
                <Text style={[typo.labelMedium, { color: colors.onSurface }]}>{item.authorName}</Text>
                <Text style={[typo.bodyMedium, { color: colors.onSurface, marginTop: 2 }]}>{item.content}</Text>
              </View>
              {item.authorId === user?.id && (
                <TouchableOpacity onPress={() => handleDelete(item.id)}>
                  <MaterialIcons name="delete-outline" size={18} color={colors.error} />
                </TouchableOpacity>
              )}
            </View>
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <MaterialIcons name="chat-bubble-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
              {t('social.noComments')}
            </Text>
          </View>
        }
        ListFooterComponent={
          hasMore ? (
            <TouchableOpacity style={styles.loadMoreBtn} onPress={loadMore}>
              {isLoadingMore ? (
                <ActivityIndicator size="small" color={colors.primary} />
              ) : (
                <Text style={[typo.labelMedium, { color: colors.primary }]}>{t('social.loadMore')}</Text>
              )}
            </TouchableOpacity>
          ) : null
        }
        contentContainerStyle={styles.list}
      />

      {/* Comment input */}
      <View style={[styles.inputBar, { backgroundColor: colors.surface, borderTopColor: colors.outlineVariant }]}>
        <View style={[styles.avatar, { backgroundColor: colors.primaryContainer }]}>
          <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>
            {(user?.firstName?.[0] || 'F').toUpperCase()}
          </Text>
        </View>
        <TextInput
          style={[styles.textField, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface }]}
          placeholder={t('social.writeCommentPlaceholder')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={newComment}
          onChangeText={setNewComment}
          multiline
        />
        <TouchableOpacity onPress={handleSubmit} disabled={isSubmitting || !newComment.trim()}>
          {isSubmitting ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <MaterialIcons name="send" size={24} color={newComment.trim() ? colors.primary : colors.onSurfaceVariant} />
          )}
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  list: { paddingBottom: 80 },
  postPreview: { padding: 16, marginBottom: 8 },
  commentCard: { marginHorizontal: 16, marginBottom: 6, padding: 12, borderRadius: 12 },
  commentRow: { flexDirection: 'row', alignItems: 'flex-start' },
  avatar: { width: 28, height: 28, borderRadius: 14, justifyContent: 'center', alignItems: 'center' },
  emptyState: { alignItems: 'center', padding: 48 },
  loadMoreBtn: { alignItems: 'center', padding: 16 },
  inputBar: { flexDirection: 'row', alignItems: 'center', padding: 12, gap: 8, borderTopWidth: 1 },
  textField: { flex: 1, borderRadius: 20, paddingHorizontal: 16, paddingVertical: 8, maxHeight: 80, fontSize: 14 },
});
