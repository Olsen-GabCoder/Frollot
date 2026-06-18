import { useState, useEffect, useCallback } from 'react';
import {
  View, Text, ScrollView, TouchableOpacity, Pressable, FlatList, TextInput,
  StyleSheet, RefreshControl, Modal, KeyboardAvoidingView, Platform,
} from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { I18nManager } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuthStore } from '../../src/stores/authStore';
import { salonsApi } from '../../src/api';
import { Avatar, SectionHeader } from '../../src/components/common';
import { SalonCard } from '../../src/components/salon';
import { EmptyState, ErrorState, LoadingState } from '../../src/components/lists';
import { Salon } from '../../src/types';
import { useTheme } from '../../src/theme';
import { formatDateLong } from '../../src/utils/formatDate';

const CATEGORY_KEYS = [
  { icon: 'view-grid' as const, i18nKey: 'home.category.all', value: undefined },
  { icon: 'content-cut' as const, i18nKey: 'home.category.cut', value: 'COUPE' },
  { icon: 'palette' as const, i18nKey: 'home.category.color', value: 'COLORATION' },
  { icon: 'spa' as const, i18nKey: 'home.category.care', value: 'SOIN' },
  { icon: 'face-man' as const, i18nKey: 'home.category.beard', value: 'BARBE' },
  { icon: 'auto-fix' as const, i18nKey: 'home.category.styling', value: 'COIFFAGE' },
];

export default function HomeScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  const [salons, setSalons] = useState<Salon[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(0);
  const [cityFilter, setCityFilter] = useState('');
  const [showCityDialog, setShowCityDialog] = useState(false);
  const [cityInput, setCityInput] = useState('');

  // Load salons when category or city filter changes
  const [refreshTick, setRefreshTick] = useState(0);
  const handleRefresh = () => { setRefreshing(true); setRefreshTick(n => n + 1); };

  // Refresh salons when screen regains focus (e.g. after editing salon info/cover)
  useFocusEffect(useCallback(() => { setRefreshTick(n => n + 1); }, []));

  useEffect(() => {
    let ignore = false;
    const loadSalons = async () => {
      setIsLoading(true);
      setHasError(false);
      try {
        const category = CATEGORY_KEYS[selectedCategory]?.value;
        const city = cityFilter || undefined;
        const data = category || city
          ? await salonsApi.getSalons({ category, city })
          : await salonsApi.getTrendingSalons(20);
        if (!ignore) setSalons(data);
      } catch {
        if (!ignore) setHasError(true);
      } finally {
        if (!ignore) { setIsLoading(false); setRefreshing(false); }
      }
    };
    loadSalons();
    return () => { ignore = true; };
  }, [selectedCategory, cityFilter, refreshTick]);

  const firstName = user?.firstName || 'vous';
  const today = formatDateLong(new Date());
  const isOwner = user?.userType === 'salon_owner';

  const applyCityFilter = () => {
    setCityFilter(cityInput.trim());
    setShowCityDialog(false);
  };
  const clearCityFilter = () => {
    setCityFilter('');
    setCityInput('');
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <View style={styles.headerRow}>
          <TouchableOpacity style={styles.iconBtn}>
            <MaterialCommunityIcons name="menu" size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Text style={[styles.brandTitle, { color: colors.onSurface }]}>Frollot</Text>
          <TouchableOpacity style={styles.iconBtn}>
            <MaterialCommunityIcons name="bell-outline" size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Avatar initials={firstName[0]?.toUpperCase()} size={36} ring imageUrl={user?.avatarUrl} />
        </View>
        <TouchableOpacity style={[styles.searchBar, { backgroundColor: colors.surfaceContainerHigh }]} onPress={() => router.push('/(tabs)/explore')}>
          <MaterialCommunityIcons name="magnify" size={22} color={colors.onSurfaceVariant} />
          <Text style={[styles.searchPlaceholder, { color: colors.onSurfaceVariant }]}>{t('home.searchBarPlaceholder')}</Text>
        </TouchableOpacity>
      </View>

      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
      >
        {/* Greeting */}
        <View style={styles.greetingSection}>
          <Text style={[styles.overline, { color: colors.secondary }]}>{today}</Text>
          <Text style={[styles.greetingTitle, { color: colors.onBackground }]}>
            {t('home.greeting', { name: firstName })}{'\n'}{t('home.greetingMessage')}
          </Text>
        </View>

        {/* Categories */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.categoriesRow}>
          {CATEGORY_KEYS.map((cat, i) => (
            <TouchableOpacity key={i} onPress={() => setSelectedCategory(i)} style={styles.categoryItem}>
              <View style={[styles.categoryIcon, i === selectedCategory ? [styles.catActive, { backgroundColor: colors.primary }] : [styles.catInactive, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]]}>
                <MaterialCommunityIcons name={cat.icon} size={26} color={i === selectedCategory ? colors.onPrimary : colors.primary} />
              </View>
              <Text style={[styles.categoryLabel, { color: i === selectedCategory ? colors.primary : colors.onSurfaceVariant }]}>{t(cat.i18nKey)}</Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {/* City filter */}
        <View style={styles.filterRow}>
          <TouchableOpacity
            style={[styles.cityChip, { backgroundColor: cityFilter ? colors.primaryContainer : colors.surfaceContainerHigh }]}
            onPress={() => { setCityInput(cityFilter); setShowCityDialog(true); }}
          >
            <MaterialCommunityIcons name="map-marker" size={16} color={cityFilter ? colors.onPrimaryContainer : colors.onSurfaceVariant} />
            <Text style={[styles.cityChipText, { color: cityFilter ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
              {cityFilter || t('home.cityLabel')}
            </Text>
            {cityFilter ? (
              <TouchableOpacity onPress={clearCityFilter} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
                <MaterialCommunityIcons name="close-circle" size={16} color={colors.onPrimaryContainer} />
              </TouchableOpacity>
            ) : null}
          </TouchableOpacity>
        </View>

        {/* Content: Loading / Error / Empty / Salons */}
        {isLoading && !refreshing ? (
          <LoadingState />
        ) : hasError ? (
          <ErrorState message={t('home.loadError')} onRetry={() => setRefreshTick(n => n + 1)} />
        ) : salons.length === 0 ? (
          <EmptyState
            icon={isOwner ? 'store-plus' : 'store-search'}
            title={isOwner ? t('home.noSalonTitle') : t('home.noResultTitle')}
            message={isOwner ? t('home.noSalonMessage') : t('home.noResultMessage')}
            actionLabel={isOwner ? t('home.createSalonButton') : undefined}
            onAction={isOwner ? () => router.push(`/create-salon?ownerId=${user?.id}`) : undefined}
          />
        ) : (
          <>
            {/* Salon list */}
            <SectionHeader
              title={CATEGORY_KEYS[selectedCategory]?.value ? t('home.salonsCategory', { category: t(CATEGORY_KEYS[selectedCategory].i18nKey) }) : t('home.popularSalons')}
              onActionPress={() => router.push('/(tabs)/explore')}
            />
            <FlatList
              horizontal
              data={salons.slice(0, 8)}
              keyExtractor={(s) => s.id}
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.salonsRow}
              renderItem={({ item }) => (
                <SalonCard
                  name={item.name}
                  location={item.city || ''}
                  rating={item.averageRating ?? 0}
                  reviewCount={item.reviewCount ?? 0}
                  imageUrl={item.coverPhotoUrl}
                  onPress={() => router.push(`/salon/${item.id}`)}
                />
              )}
            />

            {/* Vertical list for remaining salons */}
            {salons.length > 8 && salons.slice(8).map((item) => (
              <TouchableOpacity
                key={item.id}
                style={[styles.salonListItem, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}
                onPress={() => router.push(`/salon/${item.id}`)}
              >
                <View style={{ flex: 1 }}>
                  <Text style={[styles.salonListName, { color: colors.onSurface }]}>{item.name}</Text>
                  <Text style={[styles.salonListCity, { color: colors.onSurfaceVariant }]}>{item.city}</Text>
                </View>
                <MaterialCommunityIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={22} color={colors.onSurfaceVariant} />
              </TouchableOpacity>
            ))}

            {/* Queue banner */}
            <View style={styles.bannerWrap}>
              {/* design-fixed — self-contained branded gradient banner, colors relative to gradient not theme */}
              <LinearGradient colors={['#6B4E78', '#4f3a5b']} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.banner}>
                <Text style={styles.bannerOverline}>{t('home.queueOverline')}</Text>
                <Text style={styles.bannerTitle}>{t('home.queueTitle', { name: salons[0]?.name || 'Salon' })}</Text>
                <TouchableOpacity style={styles.bannerBtn} onPress={() => salons[0] && router.push(`/salon/${salons[0].id}`)}>
                  <MaterialCommunityIcons name="login" size={18} color="#6B4E78" />{/* design-fixed */}
                  <Text style={styles.bannerBtnText}>{t('home.queueButton')}</Text>
                </TouchableOpacity>
              </LinearGradient>
            </View>
          </>
        )}
      </ScrollView>

      {/* City filter dialog */}
      <Modal visible={showCityDialog} transparent animationType="fade" onRequestClose={() => setShowCityDialog(false)}>
        <Pressable style={styles.dialogOverlay} onPress={() => setShowCityDialog(false)}>
          <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
            <Pressable onPress={(e) => e.stopPropagation()} style={[styles.dialogCard, { backgroundColor: colors.surface }]}>
              <Text style={[styles.dialogTitle, { color: colors.onSurface }]}>{t('home.cityDialogTitle')}</Text>
              <TextInput
                style={[styles.dialogInput, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
                placeholder={t('home.cityDialogPlaceholder')}
                placeholderTextColor={colors.onSurfaceVariant}
                value={cityInput}
                onChangeText={setCityInput}
                autoFocus
              />
              <View style={styles.dialogActions}>
                <TouchableOpacity onPress={() => setShowCityDialog(false)} style={styles.dialogBtn}>
                  <Text style={[styles.dialogBtnText, { color: colors.onSurfaceVariant }]}>{t('common.actions.cancel')}</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={applyCityFilter} style={[styles.dialogBtn, { backgroundColor: colors.primary, borderRadius: 999 }]}>
                  <Text style={[styles.dialogBtnText, { color: colors.onPrimary }]}>{t('home.cityApply')}</Text>
                </TouchableOpacity>
              </View>
            </Pressable>
          </KeyboardAvoidingView>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingHorizontal: 8, paddingTop: 6, paddingBottom: 14 },
  headerRow: { flexDirection: 'row', alignItems: 'center', gap: 6, minHeight: 52 },
  iconBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  brandTitle: { flex: 1, fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600', letterSpacing: 0.5 },
  searchBar: {
    flexDirection: 'row', alignItems: 'center', gap: 12, height: 52,
    marginHorizontal: 8, marginTop: 6, paddingHorizontal: 18,
    borderRadius: 999,
  },
  searchPlaceholder: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  scroll: { flex: 1 },
  scrollContent: { paddingTop: 20, paddingBottom: 16 },
  greetingSection: { paddingHorizontal: 20, marginBottom: 18 },
  overline: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700', letterSpacing: 2, textTransform: 'uppercase' },
  greetingTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 30, fontWeight: '600', lineHeight: 31.5, marginTop: 4 },
  categoriesRow: { paddingHorizontal: 20, paddingBottom: 4, gap: 10, marginBottom: 12 },
  categoryItem: { alignItems: 'center', gap: 8 },
  categoryIcon: { width: 58, height: 58, borderRadius: 16, alignItems: 'center', justifyContent: 'center' },
  catActive: { shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.12, shadowRadius: 3, elevation: 1 }, // design-fixed
  catInactive: { borderWidth: 1, shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.12, shadowRadius: 3, elevation: 1 }, // design-fixed
  categoryLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 12, letterSpacing: 0.2 },
  // City filter
  filterRow: { paddingHorizontal: 20, marginBottom: 20 },
  cityChip: { flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-start', paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999 },
  cityChipText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  // Salons
  salonsRow: { paddingHorizontal: 20, paddingTop: 14, paddingBottom: 4, gap: 14 },
  salonListItem: { flexDirection: 'row', alignItems: 'center', marginHorizontal: 20, marginTop: 8, padding: 14, borderRadius: 12, borderWidth: 1 },
  salonListName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  salonListCity: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  // Banner
  bannerWrap: { marginHorizontal: 20, marginTop: 24, marginBottom: 8 },
  banner: { borderRadius: 28, padding: 22, overflow: 'hidden' },
  bannerOverline: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700', letterSpacing: 2, textTransform: 'uppercase', color: 'rgba(255,255,255,0.7)' }, // design-fixed
  bannerTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', color: '#FFFFFF', marginTop: 4, maxWidth: 230, lineHeight: 24.2 }, // design-fixed
  bannerBtn: { flexDirection: 'row', alignItems: 'center', gap: 8, alignSelf: 'flex-start', marginTop: 16, backgroundColor: '#FFFFFF', height: 44, paddingHorizontal: 24, borderRadius: 999 }, // design-fixed
  bannerBtnText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700', color: '#6B4E78' }, // design-fixed
  // Dialog
  dialogOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  dialogCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  dialogTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', marginBottom: 16 },
  dialogInput: { borderWidth: 1, borderRadius: 12, paddingHorizontal: 16, paddingVertical: 12, fontSize: 14, fontFamily: 'Manrope-Regular' },
  dialogActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 12, marginTop: 20 },
  dialogBtn: { paddingVertical: 10, paddingHorizontal: 20 },
  dialogBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
