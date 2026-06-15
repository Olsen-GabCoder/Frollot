import { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  I18nManager,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { resolveMediaUrl } from '../src/utils/media';
import { socialApi } from '../src/api/social';
import { salonsApi } from '../src/api/salons';
import { PostResponse, HairHashtagResponse, Salon, TrendPeriod } from '../src/types';

type Section = 'posts' | 'hashtags' | 'salons';

const PERIOD_KEYS: { key: TrendPeriod; i18nKey: string }[] = [
  { key: TrendPeriod.LAST_24H, i18nKey: 'social.trending.last24h' },
  { key: TrendPeriod.LAST_7D, i18nKey: 'social.trending.last7d' },
  { key: TrendPeriod.LAST_30D, i18nKey: 'social.trending.last30d' },
];

export default function TrendingScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [section, setSection] = useState<Section>('posts');
  const [period, setPeriod] = useState<TrendPeriod>(TrendPeriod.LAST_7D);

  // Posts
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [postsLoading, setPostsLoading] = useState(true);
  const [postsPage, setPostsPage] = useState(0);
  const [hasMorePosts, setHasMorePosts] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  // Hashtags
  const [hashtags, setHashtags] = useState<HairHashtagResponse[]>([]);
  const [hashtagsLoading, setHashtagsLoading] = useState(true);

  // Salons
  const [salons, setSalons] = useState<Salon[]>([]);
  const [salonsLoading, setSalonsLoading] = useState(true);

  const loadPosts = useCallback(async (reset = false) => {
    const page = reset ? 0 : postsPage;
    if (reset) {
      setPostsLoading(true);
    } else {
      setLoadingMore(true);
    }
    try {
      const result = await socialApi.getTrendingPosts(period, page, 20);
      if (reset) {
        setPosts(result.content);
        setPostsPage(0);
      } else {
        setPosts((prev) => [...prev, ...result.content]);
      }
      setHasMorePosts(!result.last);
    } catch {} finally {
      setPostsLoading(false);
      setLoadingMore(false);
    }
  }, [period, postsPage]);

  useEffect(() => {
    loadPosts(true);
  }, [period]);

  useEffect(() => {
    const load = async () => {
      setHashtagsLoading(true);
      try {
        const data = await socialApi.getTrendingHashtags(20);
        setHashtags(data);
      } catch {} finally {
        setHashtagsLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    const load = async () => {
      setSalonsLoading(true);
      try {
        const data = await salonsApi.getTrendingSalons(10);
        setSalons(data);
      } catch {} finally {
        setSalonsLoading(false);
      }
    };
    load();
  }, []);

  const handleLoadMore = () => {
    if (hasMorePosts && !loadingMore) {
      setPostsPage((p) => p + 1);
      loadPosts(false);
    }
  };

  // B27 : fusion sélective — la réponse de toggleLike est partielle (pas de media),
  // remplacer le post entier effacerait l'image.
  const handleLike = async (postId: string) => {
    try {
      const updated = await socialApi.toggleLike(postId);
      setPosts((prev) => prev.map((p) => (p.id === postId
        ? { ...p, isLikedByCurrentUser: updated.isLikedByCurrentUser, likesCount: updated.likesCount }
        : p)));
    } catch {}
  };

  const sections: { key: Section; i18nKey: string }[] = [
    { key: 'posts', i18nKey: 'social.trending.posts' },
    { key: 'hashtags', i18nKey: 'social.trending.hashtags' },
    { key: 'salons', i18nKey: 'social.trending.salons' },
  ];

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>
          {t('social.tabs.trending')}
        </Text>
      </View>

      {/* Section tabs */}
      <View style={styles.tabsRow}>
        {sections.map((s) => (
          <TouchableOpacity
            key={s.key}
            style={[styles.tab, {
              backgroundColor: section === s.key ? colors.primaryContainer : colors.surfaceContainerHigh,
            }]}
            onPress={() => setSection(s.key)}
          >
            <Text style={[typo.labelMedium, {
              color: section === s.key ? colors.onPrimaryContainer : colors.onSurfaceVariant,
            }]}>
              {t(s.i18nKey)}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Posts */}
      {section === 'posts' && (
        <>
          {/* Period filters */}
          <View style={styles.periodRow}>
            {PERIOD_KEYS.map((p) => (
              <TouchableOpacity
                key={p.key}
                style={[styles.periodChip, {
                  backgroundColor: period === p.key ? colors.primary : 'transparent',
                  borderColor: period === p.key ? colors.primary : colors.outline,
                }]}
                onPress={() => setPeriod(p.key)}
              >
                <Text style={[typo.labelMedium, {
                  color: period === p.key ? colors.onPrimary : colors.onSurfaceVariant,
                }]}>
                  {t(p.i18nKey)}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          {postsLoading ? (
            <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 40 }} />
          ) : (
            <FlatList
              data={posts}
              keyExtractor={(item) => item.id}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={[styles.postCard, { backgroundColor: colors.surface }]}
                  onPress={() => router.push(`/post/${item.id}`)}
                >
                  <View style={styles.postHeader}>
                    <View style={[styles.postAvatar, { backgroundColor: colors.secondaryContainer }]}>
                      <Text style={[typo.labelMedium, { color: colors.onSecondaryContainer }]}>
                        {(item.authorName?.[0] || '?').toUpperCase()}
                      </Text>
                    </View>
                    <Text style={[typo.titleSmall, { color: colors.onSurface, flex: 1, marginStart: 8 }]}>
                      {item.authorName}
                    </Text>
                  </View>
                  <Text style={[typo.bodyMedium, { color: colors.onSurface }]} numberOfLines={3}>
                    {item.content}
                  </Text>
                  {item.imageUrl && (
                    <Image source={{ uri: resolveMediaUrl(item.imageUrl) }} style={styles.postImage} contentFit="cover" />
                  )}
                  <View style={styles.postFooter}>
                    <TouchableOpacity style={styles.iconBtn} onPress={() => handleLike(item.id)}>
                      <MaterialIcons
                        name={item.isLikedByCurrentUser ? 'favorite' : 'favorite-border'}
                        size={20}
                        color={item.isLikedByCurrentUser ? colors.error : colors.onSurfaceVariant}
                      />
                      <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>
                        {item.likesCount}
                      </Text>
                    </TouchableOpacity>
                    <View style={styles.iconBtn}>
                      <MaterialIcons name="chat-bubble-outline" size={20} color={colors.onSurfaceVariant} />
                      <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, marginStart: 4 }]}>
                        {item.commentsCount}
                      </Text>
                    </View>
                  </View>
                </TouchableOpacity>
              )}
              onEndReached={handleLoadMore}
              onEndReachedThreshold={0.3}
              ListFooterComponent={loadingMore ? <ActivityIndicator color={colors.primary} style={{ padding: 16 }} /> : null}
              ListEmptyComponent={
                <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 40 }]}>
                  {t('social.noPosts')}
                </Text>
              }
              contentContainerStyle={styles.list}
            />
          )}
        </>
      )}

      {/* Hashtags */}
      {section === 'hashtags' && (
        hashtagsLoading ? (
          <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 40 }} />
        ) : (
          <FlatList
            data={hashtags}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <TouchableOpacity style={[styles.hashtagCard, { backgroundColor: colors.surface }]}>
                <View style={[styles.hashIcon, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.titleMedium, { color: colors.onPrimaryContainer }]}>#</Text>
                </View>
                <View style={{ flex: 1, marginStart: 12 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.name}</Text>
                  {item.category && (
                    <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{item.category}</Text>
                  )}
                </View>
                <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant }]}>
                  {t('social.hashtagCount', { count: item.usageCount })}
                </Text>
              </TouchableOpacity>
            )}
            contentContainerStyle={styles.list}
          />
        )
      )}

      {/* Salons */}
      {section === 'salons' && (
        salonsLoading ? (
          <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 40 }} />
        ) : (
          <FlatList
            data={salons}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={[styles.salonCard, { backgroundColor: colors.surface }]}
                onPress={() => router.push(`/salon/${item.id}`)}
              >
                <View style={[styles.salonThumb, { backgroundColor: colors.surfaceContainerHigh }]}>
                  {item.coverPhotoUrl && (
                    <Image source={{ uri: resolveMediaUrl(item.coverPhotoUrl) }} style={styles.salonThumb} contentFit="cover" />
                  )}
                </View>
                <View style={{ flex: 1, marginStart: 12 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.name}</Text>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{item.address}</Text>
                </View>
              </TouchableOpacity>
            )}
            contentContainerStyle={styles.list}
          />
        )
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  tabsRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 8 },
  tab: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 999 },
  periodRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, marginBottom: 8 },
  periodChip: { paddingVertical: 6, paddingHorizontal: 14, borderRadius: 999, borderWidth: 1 },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  postCard: { borderRadius: 16, padding: 16, marginBottom: 10 },
  postHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
  postAvatar: { width: 32, height: 32, borderRadius: 16, justifyContent: 'center', alignItems: 'center' },
  postImage: { width: '100%', height: 180, borderRadius: 12, marginTop: 8 },
  postFooter: { flexDirection: 'row', gap: 20, marginTop: 10 },
  iconBtn: { flexDirection: 'row', alignItems: 'center' },
  hashtagCard: { flexDirection: 'row', alignItems: 'center', padding: 16, borderRadius: 12, marginBottom: 8 },
  hashIcon: { width: 40, height: 40, borderRadius: 20, justifyContent: 'center', alignItems: 'center' },
  salonCard: { flexDirection: 'row', alignItems: 'center', padding: 12, borderRadius: 16, marginBottom: 8 },
  salonThumb: { width: 64, height: 64, borderRadius: 12 },
});
