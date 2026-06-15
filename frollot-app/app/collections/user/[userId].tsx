import { useEffect, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  Modal,
  Pressable,
  Switch,
  RefreshControl,
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../../src/theme';
import { useAuthStore } from '../../../src/stores/authStore';
import { collectionsApi } from '../../../src/api/portfolios';
import { CollectionResponse, CollectionCategory } from '../../../src/types';
import { resolveMediaUrl } from '../../../src/utils/media';
import { LoadingState } from '../../../src/components/lists/LoadingState';
import { ErrorState } from '../../../src/components/lists/ErrorState';
import { EmptyState } from '../../../src/components/lists/EmptyState';
import { TextField } from '../../../src/components/ui/TextField';
import { PrimaryButton, TextButton } from '../../../src/components/ui/Button';

const CATEGORY_KEYS: { value: CollectionCategory; i18nKey: string }[] = [
  { value: CollectionCategory.INSPIRATION, i18nKey: 'collections.category.inspiration' },
  { value: CollectionCategory.PORTFOLIO, i18nKey: 'collections.category.portfolio' },
  { value: CollectionCategory.TENDANCE, i18nKey: 'collections.category.tendance' },
  { value: CollectionCategory.PERSONNEL, i18nKey: 'collections.category.personnel' },
];

export default function CollectionsListScreen() {
  const { userId } = useLocalSearchParams<{ userId: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { user } = useAuthStore();
  const isOwner = !!user && user.id === userId;

  const [collections, setCollections] = useState<CollectionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadError, setLoadError] = useState('');

  // Création
  const [showCreate, setShowCreate] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<CollectionCategory>(CollectionCategory.INSPIRATION);
  const [isPublic, setIsPublic] = useState(true);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  // Suppression
  const [deleteTarget, setDeleteTarget] = useState<CollectionResponse | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  const load = async (asRefresh = false) => {
    if (!userId) return;
    asRefresh ? setRefreshing(true) : setIsLoading(true);
    setLoadError('');
    try {
      const data = await collectionsApi.getCollectionsByUser(userId, isOwner);
      setCollections(data);
    } catch (error: any) {
      setLoadError(error?.response?.data?.message || t('collections.loadError'));
    } finally {
      asRefresh ? setRefreshing(false) : setIsLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    (async () => {
      if (!userId) return;
      setIsLoading(true);
      setLoadError('');
      try {
        const data = await collectionsApi.getCollectionsByUser(userId, isOwner);
        if (!ignore) setCollections(data);
      } catch (error: any) {
        if (!ignore) setLoadError(error?.response?.data?.message || t('collections.loadError'));
      } finally {
        if (!ignore) setIsLoading(false);
      }
    })();
    return () => { ignore = true; };
  }, [userId, isOwner]);

  const resetCreateForm = () => {
    setName('');
    setDescription('');
    setCategory(CollectionCategory.INSPIRATION);
    setIsPublic(true);
    setCreateError('');
  };

  const handleCreate = async () => {
    const trimmed = name.trim();
    if (!trimmed) {
      setCreateError(t('collections.nameRequired'));
      return;
    }
    setCreating(true);
    setCreateError('');
    try {
      const created = await collectionsApi.createCollection({
        name: trimmed,
        description: description.trim() || undefined,
        isPublic,
        category,
      });
      setCollections((prev) => [created, ...prev]);
      setShowCreate(false);
      resetCreateForm();
    } catch (error: any) {
      setCreateError(error?.response?.data?.message || t('collections.createError'));
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    const previous = collections;
    // Optimiste : retrait immédiat, rollback si le serveur refuse
    setCollections((prev) => prev.filter((c) => c.id !== deleteTarget.id));
    setDeleting(true);
    setDeleteError('');
    try {
      await collectionsApi.deleteCollection(deleteTarget.id);
      setDeleteTarget(null);
    } catch (error: any) {
      setCollections(previous);
      setDeleteError(error?.response?.data?.message || t('collections.deleteError'));
    } finally {
      setDeleting(false);
    }
  };

  const categoryColor = (cat: CollectionCategory) => {
    switch (cat) {
      case CollectionCategory.INSPIRATION: return { fg: colors.primary, bg: colors.primaryContainer };
      case CollectionCategory.PORTFOLIO: return { fg: colors.secondary, bg: colors.secondaryContainer };
      case CollectionCategory.TENDANCE: return { fg: colors.tertiary, bg: colors.tertiaryContainer };
      case CollectionCategory.PERSONNEL: return { fg: colors.error, bg: colors.errorContainer };
    }
  };

  const categoryLabel = (cat: CollectionCategory) => {
    const key = CATEGORY_KEYS.find((c) => c.value === cat)?.i18nKey;
    return key ? t(key) : cat;
  };

  const renderCollection = ({ item }: { item: CollectionResponse }) => {
    const cover = resolveMediaUrl(item.coverImageUrl);
    const catColors = categoryColor(item.category);
    return (
      <TouchableOpacity
        style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}
        activeOpacity={0.7}
        onPress={() => router.push(`/collections/${item.id}`)}
      >
        <View style={[styles.coverBox, { backgroundColor: colors.surfaceContainerHigh }]}>
          {cover ? (
            <Image source={{ uri: cover }} style={styles.coverImage} contentFit="cover" />
          ) : (
            <MaterialIcons name="collections-bookmark" size={30} color={colors.onSurfaceVariant} />
          )}
        </View>
        <View style={styles.cardBody}>
          <View style={styles.cardTitleRow}>
            <Text style={[typo.titleSmall, styles.cardTitle, { color: colors.onSurface }]} numberOfLines={1}>
              {item.name}
            </Text>
            {isOwner && (
              <TouchableOpacity
                hitSlop={8}
                onPress={() => { setDeleteError(''); setDeleteTarget(item); }}
              >
                <MaterialIcons name="delete-outline" size={20} color={colors.onSurfaceVariant} />
              </TouchableOpacity>
            )}
          </View>
          <View style={styles.badgeRow}>
            <View style={[styles.categoryBadge, { backgroundColor: catColors.bg }]}>
              <Text style={[typo.labelSmall, { color: catColors.fg }]}>{categoryLabel(item.category)}</Text>
            </View>
            {!item.isPublic && (
              <MaterialIcons name="lock-outline" size={14} color={colors.onSurfaceVariant} />
            )}
          </View>
          {!!item.description && (
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 4 }]} numberOfLines={2}>
              {item.description}
            </Text>
          )}
          <Text style={[typo.labelMedium, { color: colors.primary, marginTop: 6 }]}>
            {t('collections.postCount', { count: item.postsCount })}
          </Text>
        </View>
      </TouchableOpacity>
    );
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={goBack}
        >
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <View style={styles.titleBlock}>
        <Text style={[typo.overline, { color: colors.secondary }]}>{t('collections.overline')}</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
          {t('profile.collections')}
        </Text>
      </View>

      {isLoading ? (
        <LoadingState />
      ) : loadError ? (
        <ErrorState message={loadError} onRetry={() => load()} />
      ) : collections.length === 0 ? (
        <EmptyState
          icon="bookmark-multiple-outline"
          title={t('collections.emptyTitle')}
          message={isOwner
            ? t('collections.emptyOwnerMessage')
            : t('collections.emptyOtherMessage')}
          actionLabel={isOwner ? t('collections.createButton') : undefined}
          onAction={isOwner ? () => { resetCreateForm(); setShowCreate(true); } : undefined}
        />
      ) : (
        <FlatList
          data={collections}
          keyExtractor={(c) => c.id}
          renderItem={renderCollection}
          contentContainerStyle={styles.list}
          showsVerticalScrollIndicator={false}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={() => load(true)} tintColor={colors.primary} />
          }
        />
      )}

      {/* FAB création — owner uniquement (parité KMP) */}
      {isOwner && !isLoading && collections.length > 0 && (
        <TouchableOpacity
          style={[styles.fab, { backgroundColor: colors.primary }]}
          activeOpacity={0.85}
          onPress={() => { resetCreateForm(); setShowCreate(true); }}
        >
          <MaterialIcons name="add" size={26} color={colors.onPrimary} />
        </TouchableOpacity>
      )}

      {/* Modal création (pattern B22) */}
      <Modal visible={showCreate} transparent animationType="fade" onRequestClose={() => setShowCreate(false)}>
        <Pressable style={styles.modalOverlay} onPress={() => !creating && setShowCreate(false)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[typo.overline, { color: colors.secondary, textAlign: 'center' }]}>{t('profile.collections')}</Text>
            <Text style={[typo.headlineSmall, { color: colors.onSurface, textAlign: 'center', marginBottom: 16 }]}>
              {t('collections.newTitle')}
            </Text>

            <TextField
              label={t('collections.nameLabel')}
              icon="bookmark-outline"
              value={name}
              onChangeText={(v) => { setName(v); if (createError) setCreateError(''); }}
              placeholder={t('collections.namePlaceholder')}
              maxLength={200}
            />
            <View style={styles.fieldGap}>
              <TextField
                label={t('collections.descriptionLabel')}
                value={description}
                onChangeText={setDescription}
                placeholder={t('collections.descriptionPlaceholder')}
                multiline
                maxLength={2000}
              />
            </View>

            <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant, marginTop: 16, marginBottom: 8 }]}>
              {t('collections.categoryLabel')}
            </Text>
            <View style={styles.chipRow}>
              {CATEGORY_KEYS.map((cat) => {
                const on = category === cat.value;
                return (
                  <TouchableOpacity
                    key={cat.value}
                    style={[styles.chip, {
                      backgroundColor: on ? colors.primaryContainer : colors.surfaceContainer,
                      borderColor: on ? colors.primary : colors.outlineVariant,
                    }]}
                    onPress={() => setCategory(cat.value)}
                  >
                    <Text style={[typo.labelMedium, { color: on ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
                      {t(cat.i18nKey)}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            <View style={styles.switchRow}>
              <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{t('collections.publicLabel')}</Text>
              <Switch
                value={isPublic}
                onValueChange={setIsPublic}
                trackColor={{ true: colors.primary, false: colors.outlineVariant }}
                thumbColor={colors.surface}
              />
            </View>

            {!!createError && (
              <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={18} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.errorText, { color: colors.onErrorContainer }]}>
                  {createError}
                </Text>
              </View>
            )}

            <View style={styles.modalActions}>
              <TextButton onPress={() => !creating && setShowCreate(false)}>{t('common.actions.cancel')}</TextButton>
              <PrimaryButton loading={creating} disabled={!name.trim()} onPress={handleCreate}>
                {t('collections.createAction')}
              </PrimaryButton>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Modal confirmation suppression (pattern B22) */}
      <Modal
        visible={!!deleteTarget}
        transparent
        animationType="fade"
        onRequestClose={() => !deleting && setDeleteTarget(null)}
      >
        <Pressable style={styles.modalOverlay} onPress={() => !deleting && setDeleteTarget(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <View style={[styles.deleteIconCircle, { backgroundColor: colors.errorContainer }]}>
              <MaterialIcons name="delete-outline" size={28} color={colors.error} />
            </View>
            <Text style={[typo.headlineSmall, { color: colors.onSurface, textAlign: 'center', marginTop: 12 }]}>
              {t('collections.deleteTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 8 }]}>
              {t('collections.deleteMessage', { name: deleteTarget?.name })}
            </Text>

            {!!deleteError && (
              <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={18} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.errorText, { color: colors.onErrorContainer }]}>
                  {deleteError}
                </Text>
              </View>
            )}

            <View style={styles.modalActions}>
              <TextButton onPress={() => !deleting && setDeleteTarget(null)}>{t('common.actions.cancel')}</TextButton>
              <PrimaryButton loading={deleting} onPress={handleDelete}>{t('common.actions.delete')}</PrimaryButton>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  titleBlock: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 16 },
  list: { paddingHorizontal: 16, paddingBottom: 96 },
  // Card
  card: {
    flexDirection: 'row', gap: 14,
    borderRadius: 20, borderWidth: 1, padding: 14, marginBottom: 12,
  },
  coverBox: {
    width: 80, height: 80, borderRadius: 14, overflow: 'hidden',
    alignItems: 'center', justifyContent: 'center',
  },
  coverImage: { width: '100%', height: '100%' },
  cardBody: { flex: 1 },
  cardTitleRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  cardTitle: { flex: 1 },
  badgeRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 6 },
  categoryBadge: { paddingVertical: 3, paddingHorizontal: 10, borderRadius: 999 },
  // FAB
  fab: {
    position: 'absolute', end: 20, bottom: 28,
    width: 56, height: 56, borderRadius: 28,
    alignItems: 'center', justifyContent: 'center',
    elevation: 4,
    shadowColor: '#000', shadowOpacity: 0.2, shadowRadius: 8, shadowOffset: { width: 0, height: 4 }, // design-fixed — ombre
  },
  // Modals
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  fieldGap: { marginTop: 14 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999, borderWidth: 1 },
  switchRow: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 18,
  },
  errorCard: {
    flexDirection: 'row', alignItems: 'flex-start', gap: 8,
    borderRadius: 12, padding: 12, marginTop: 16,
  },
  errorText: { flex: 1 },
  modalActions: {
    flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: 8, marginTop: 20,
  },
  deleteIconCircle: {
    width: 56, height: 56, borderRadius: 28, alignSelf: 'center',
    alignItems: 'center', justifyContent: 'center',
  },
});
