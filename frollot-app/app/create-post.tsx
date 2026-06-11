import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  NativeSyntheticEvent,
  TextInputSelectionChangeEventData,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { socialApi } from '../src/api/social';
import { usersApi } from '../src/api/users';
import { mediaApi } from '../src/api/media';
import { Avatar } from '../src/components/common';
import { PostType, PostVisibility, PostMediaType, CreatePostMediaRequest, CreateTagRequest, TaggedType, User, HairHashtagResponse } from '../src/types';

const POST_TYPES = [
  { key: PostType.GENERAL, label: 'General' },
  { key: PostType.AVANT_APRES, label: 'Avant/Apres' },
  { key: PostType.PORTFOLIO, label: 'Portfolio' },
  { key: PostType.TENDANCE, label: 'Tendance' },
  { key: PostType.CONSEIL, label: 'Conseil' },
  { key: PostType.REALISATION, label: 'Realisation' },
  { key: PostType.INSPIRATION, label: 'Inspiration' },
];

const VISIBILITY_OPTIONS = [
  { key: PostVisibility.PUBLIC, label: 'Public', icon: 'public' as const },
  { key: PostVisibility.FOLLOWERS, label: 'Abonnes', icon: 'people' as const },
  { key: PostVisibility.PRIVATE, label: 'Prive', icon: 'lock' as const },
];

interface MediaItem {
  uri: string;
  mediaType: PostMediaType;
  orderIndex: number;
  uploadedUrl?: string;
}

export default function CreatePostScreen() {
  const { salonId } = useLocalSearchParams<{ salonId?: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { user } = useAuthStore();
  const { colors, typography: typo } = theme;

  const [content, setContent] = useState('');
  const [postType, setPostType] = useState<PostType>(PostType.GENERAL);
  const [visibility, setVisibility] = useState<PostVisibility>(PostVisibility.PUBLIC);
  const [media, setMedia] = useState<MediaItem[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [isPosting, setIsPosting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mentionedTags, setMentionedTags] = useState<CreateTagRequest[]>([]);

  // Cursor and suggestions
  const [cursorPos, setCursorPos] = useState(0);
  const [mentionSuggestions, setMentionSuggestions] = useState<User[]>([]);
  const [hashtagSuggestions, setHashtagSuggestions] = useState<HairHashtagResponse[]>([]);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);

  const MAX_CHARS = 5000;

  // Detect active token at cursor position
  const getActiveToken = (): { type: 'mention' | 'hashtag'; token: string; start: number } | null => {
    const textBefore = content.substring(0, cursorPos);
    const mentionMatch = textBefore.match(/@(\w*)$/);
    if (mentionMatch && mentionMatch[1].length >= 1) {
      return { type: 'mention', token: mentionMatch[1], start: cursorPos - mentionMatch[0].length };
    }
    const hashtagMatch = textBefore.match(/#(\w*)$/);
    if (hashtagMatch && hashtagMatch[1].length >= 1) {
      return { type: 'hashtag', token: hashtagMatch[1], start: cursorPos - hashtagMatch[0].length };
    }
    return null;
  };

  const activeToken = getActiveToken();

  // Debounced suggestion fetch
  useEffect(() => {
    if (!activeToken || activeToken.token.length < 2) {
      setMentionSuggestions([]);
      setHashtagSuggestions([]);
      return;
    }
    let ignore = false;
    const timer = setTimeout(async () => {
      setIsLoadingSuggestions(true);
      try {
        if (activeToken.type === 'mention') {
          const results = await usersApi.searchUsers(activeToken.token);
          if (!ignore) { setMentionSuggestions(results); setHashtagSuggestions([]); }
        } else {
          const results = await socialApi.suggestHashtags(activeToken.token, 8);
          if (!ignore) { setHashtagSuggestions(results); setMentionSuggestions([]); }
        }
      } catch {
        if (!ignore) { setMentionSuggestions([]); setHashtagSuggestions([]); }
      } finally {
        if (!ignore) setIsLoadingSuggestions(false);
      }
    }, 300);
    return () => { ignore = true; clearTimeout(timer); };
  }, [activeToken?.type, activeToken?.token]);

  // Insert mention suggestion into text + register tag with displayName for later reconciliation
  const insertMention = (u: User) => {
    if (!activeToken) return;
    const displayName = [u.firstName, u.lastName].filter(Boolean).join('');
    const before = content.substring(0, activeToken.start);
    const after = content.substring(cursorPos);
    const insertion = '@' + displayName + ' ';
    setContent(before + insertion + after);
    setCursorPos(before.length + insertion.length);
    setMentionedTags(prev => {
      if (prev.some(t => t.taggedId === u.id)) return prev;
      return [...prev, { taggedType: TaggedType.USER, taggedId: u.id, _displayName: displayName }];
    });
    setMentionSuggestions([]);
  };

  // Insert hashtag suggestion into text
  const insertHashtag = (name: string) => {
    if (!activeToken) return;
    const before = content.substring(0, activeToken.start);
    const after = content.substring(cursorPos);
    const insertion = '#' + name + ' ';
    setContent(before + insertion + after);
    setCursorPos(before.length + insertion.length);
    setHashtagSuggestions([]);
  };

  const handleSelectionChange = (e: NativeSyntheticEvent<TextInputSelectionChangeEventData>) => {
    setCursorPos(e.nativeEvent.selection.end);
  };

  const handleContentChange = (text: string) => {
    setContent(text);
    setCursorPos(text.length); // fallback if onSelectionChange doesn't fire
  };

  const pickImage = async (mediaType: PostMediaType) => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      quality: 0.8,
    });
    if (!result.canceled && result.assets[0]) {
      setMedia((prev) => [
        ...prev,
        { uri: result.assets[0].uri, mediaType, orderIndex: prev.length },
      ]);
    }
  };

  const removeMedia = (index: number) => {
    setMedia((prev) => prev.filter((_, i) => i !== index).map((m, i) => ({ ...m, orderIndex: i })));
  };

  const handlePublish = async () => {
    if (!user || !content.trim()) {
      Alert.alert(t('common.error'), 'Le contenu est requis');
      return;
    }

    if (postType === PostType.AVANT_APRES && media.length < 2) {
      Alert.alert(t('common.error'), 'Avant/Apres requiert au moins 2 images');
      return;
    }

    setIsPosting(true);
    setError(null);
    try {
      // Upload all media
      const uploadedMedia: CreatePostMediaRequest[] = [];
      for (const item of media) {
        setIsUploading(true);
        const fileName = `post_${Date.now()}_${item.orderIndex}.jpg`;
        const url = await mediaApi.uploadImage(item.uri, fileName);
        uploadedMedia.push({
          mediaUrl: url,
          mediaType: item.mediaType,
          caption: '',
          orderIndex: item.orderIndex,
        });
      }
      setIsUploading(false);

      // Reconcile: only keep mentions whose @displayName is still in the text
      const trimmedContent = content.trim();
      const reconciledTags = mentionedTags
        .filter(t => (t as any)._displayName && trimmedContent.includes('@' + (t as any)._displayName))
        .map(({ taggedType, taggedId }) => ({ taggedType, taggedId }));

      await socialApi.createPost({
        authorId: user.id,
        content: trimmedContent,
        imageUrl: uploadedMedia[0]?.mediaUrl,
        postType,
        visibility,
        tags: reconciledTags.length > 0 ? reconciledTags : undefined,
        media: uploadedMedia,
      });

      router.back();
    } catch (e: any) {
      setError(e?.response?.data?.message || t('common.error'));
    } finally {
      setIsPosting(false);
      setIsUploading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface, borderBottomColor: colors.outlineVariant }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="close" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, flex: 1, marginLeft: 16 }]}>
          {t('social.createPost')}
        </Text>
        <TouchableOpacity
          style={[styles.publishBtn, { backgroundColor: content.trim() ? colors.primary : colors.surfaceContainerHigh }]}
          onPress={handlePublish}
          disabled={isPosting || !content.trim() || content.length > MAX_CHARS}
        >
          {isPosting ? (
            <ActivityIndicator size="small" color={colors.onPrimary} />
          ) : (
            <Text style={[typo.labelLarge, { color: content.trim() ? colors.onPrimary : colors.onSurfaceVariant }]}>
              Publier
            </Text>
          )}
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled">
        {/* Author info */}
        <View style={styles.authorRow}>
          <View style={[styles.avatar, { backgroundColor: colors.primaryContainer }]}>
            <Text style={[typo.titleMedium, { color: colors.onPrimaryContainer }]}>
              {(user?.firstName?.[0] || 'F').toUpperCase()}
            </Text>
          </View>
          <View style={{ marginLeft: 12 }}>
            <Text style={[typo.titleSmall, { color: colors.onSurface }]}>
              {user?.firstName} {user?.lastName}
            </Text>
            <View style={styles.visibilityIndicator}>
              <MaterialIcons
                name={VISIBILITY_OPTIONS.find((v) => v.key === visibility)?.icon || 'public'}
                size={14}
                color={colors.onSurfaceVariant}
              />
              <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>
                {VISIBILITY_OPTIONS.find((v) => v.key === visibility)?.label}
              </Text>
            </View>
          </View>
        </View>

        {/* Post type selector */}
        <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8 }]}>
          {t('social.postType')}
        </Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsScroll}>
          {POST_TYPES.map((pt) => (
            <TouchableOpacity
              key={pt.key}
              style={[
                styles.chip,
                {
                  backgroundColor: postType === pt.key ? colors.primaryContainer : colors.surfaceContainerHigh,
                  borderColor: postType === pt.key ? colors.primary : 'transparent',
                },
              ]}
              onPress={() => setPostType(pt.key)}
            >
              <Text style={[
                typo.labelMedium,
                { color: postType === pt.key ? colors.onPrimaryContainer : colors.onSurfaceVariant },
              ]}>
                {pt.label}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {/* Visibility selector */}
        <Text style={[typo.labelLarge, { color: colors.onSurface, marginTop: 16, marginBottom: 8 }]}>
          {t('social.visibility')}
        </Text>
        <View style={styles.visibilityRow}>
          {VISIBILITY_OPTIONS.map((opt) => (
            <TouchableOpacity
              key={opt.key}
              style={[
                styles.chip,
                {
                  backgroundColor: visibility === opt.key ? colors.primaryContainer : colors.surfaceContainerHigh,
                  borderColor: visibility === opt.key ? colors.primary : 'transparent',
                },
              ]}
              onPress={() => setVisibility(opt.key)}
            >
              <MaterialIcons
                name={opt.icon}
                size={16}
                color={visibility === opt.key ? colors.onPrimaryContainer : colors.onSurfaceVariant}
              />
              <Text style={[
                typo.labelMedium,
                { color: visibility === opt.key ? colors.onPrimaryContainer : colors.onSurfaceVariant, marginLeft: 4 },
              ]}>
                {opt.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Content input */}
        <TextInput
          style={[styles.contentInput, {
            backgroundColor: colors.surface,
            color: colors.onSurface,
            borderColor: content.length > MAX_CHARS ? colors.error : colors.outlineVariant,
          }]}
          placeholder={t('social.content')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={content}
          onChangeText={handleContentChange}
          onSelectionChange={handleSelectionChange}
          multiline
          textAlignVertical="top"
        />

        {/* Character counter */}
        <View style={styles.counterRow}>
          <Text style={[styles.counterText, {
            color: content.length > MAX_CHARS ? colors.error : content.length > MAX_CHARS * 0.9 ? colors.warning : colors.onSurfaceVariant,
          }]}>
            {content.length} / {MAX_CHARS}
          </Text>
        </View>

        {/* Suggestions */}
        {(mentionSuggestions.length > 0 || hashtagSuggestions.length > 0 || isLoadingSuggestions) && (
          <View style={[styles.suggestionsCard, { backgroundColor: colors.surfaceContainerHighest, borderColor: colors.outlineVariant }]}>
            {isLoadingSuggestions && <ActivityIndicator size="small" color={colors.primary} style={{ padding: 8 }} />}
            {mentionSuggestions.map((u) => (
              <TouchableOpacity
                key={u.id}
                style={styles.suggestionItem}
                onPress={() => insertMention(u)}
              >
                <Avatar initials={(u.firstName?.[0] || u.email[0]).toUpperCase()} size={28} imageUrl={u.avatarUrl} />
                <Text style={[styles.suggestionText, { color: colors.onSurface }]} numberOfLines={1}>
                  {u.firstName} {u.lastName}
                </Text>
                <Text style={[styles.suggestionMeta, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                  {u.email}
                </Text>
              </TouchableOpacity>
            ))}
            {hashtagSuggestions.map((h) => (
              <TouchableOpacity
                key={h.id || h.name}
                style={styles.suggestionItem}
                onPress={() => insertHashtag(h.name)}
              >
                <Text style={[styles.suggestionHash, { color: colors.primary }]}>#</Text>
                <Text style={[styles.suggestionText, { color: colors.onSurface }]}>{h.name}</Text>
                {h.usageCount != null && (
                  <Text style={[styles.suggestionMeta, { color: colors.onSurfaceVariant }]}>{h.usageCount} posts</Text>
                )}
              </TouchableOpacity>
            ))}
          </View>
        )}

        {/* Media section */}
        <View style={styles.mediaSection}>
          <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8 }]}>
            Media
          </Text>
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {media.map((item, index) => (
              <View key={index} style={styles.mediaItem}>
                <Image source={{ uri: item.uri }} style={styles.mediaThumb} contentFit="cover" />
                <TouchableOpacity style={styles.removeMediaBtn} onPress={() => removeMedia(index)}>
                  <MaterialIcons name="close" size={16} color="#fff" />{/* design-fixed — white icon on dark overlay */}
                </TouchableOpacity>
                <View style={[styles.mediaTypeBadge, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>
                    {item.mediaType}
                  </Text>
                </View>
              </View>
            ))}
            <TouchableOpacity
              style={[styles.addMediaBtn, { backgroundColor: colors.surfaceContainerHigh, borderColor: colors.outlineVariant }]}
              onPress={() => pickImage(postType === PostType.AVANT_APRES
                ? (media.length === 0 ? PostMediaType.BEFORE : PostMediaType.AFTER)
                : PostMediaType.DETAIL
              )}
            >
              <MaterialIcons name="add-photo-alternate" size={32} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          </ScrollView>
        </View>

        {/* Error */}
        {error && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
          </View>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: 'row', alignItems: 'center',
    paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16,
    borderBottomWidth: 1,
  },
  publishBtn: { paddingHorizontal: 20, paddingVertical: 8, borderRadius: 999 },
  scrollContent: { padding: 16 },
  authorRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
  avatar: { width: 44, height: 44, borderRadius: 22, justifyContent: 'center', alignItems: 'center' },
  visibilityIndicator: { flexDirection: 'row', alignItems: 'center', marginTop: 2 },
  chipsScroll: { marginBottom: 8 },
  chip: {
    flexDirection: 'row', alignItems: 'center',
    paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999,
    marginRight: 8, borderWidth: 1,
  },
  visibilityRow: { flexDirection: 'row', gap: 8 },
  contentInput: {
    minHeight: 120, borderRadius: 16, padding: 16,
    marginTop: 20, fontSize: 16, borderWidth: 1,
  },
  counterRow: { alignItems: 'flex-end', marginTop: 4, paddingRight: 4 },
  counterText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Suggestions
  suggestionsCard: { borderRadius: 12, borderWidth: 1, marginTop: 8, maxHeight: 200, overflow: 'hidden' },
  suggestionItem: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 10, paddingHorizontal: 14 },
  suggestionText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  suggestionMeta: { fontFamily: 'Manrope-Regular', fontSize: 12, marginLeft: 'auto' },
  suggestionHash: { fontFamily: 'Manrope-Bold', fontSize: 18, fontWeight: '700' },
  mediaSection: { marginTop: 20 },
  mediaItem: { width: 100, height: 100, borderRadius: 12, marginRight: 8, position: 'relative' },
  mediaThumb: { width: 100, height: 100, borderRadius: 12 },
  removeMediaBtn: {
    position: 'absolute', top: 4, right: 4,
    width: 24, height: 24, borderRadius: 12,
    backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', alignItems: 'center', // design-fixed
  },
  mediaTypeBadge: {
    position: 'absolute', bottom: 4, left: 4,
    paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4,
  },
  addMediaBtn: {
    width: 100, height: 100, borderRadius: 12, borderWidth: 1, borderStyle: 'dashed',
    justifyContent: 'center', alignItems: 'center',
  },
  errorCard: { padding: 12, borderRadius: 12, marginTop: 16 },
});
