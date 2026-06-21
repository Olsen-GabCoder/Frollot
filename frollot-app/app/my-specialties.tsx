import { useEffect, useState, useCallback, useMemo } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
  I18nManager,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { ServiceCategory, SERVICE_CATEGORY_META } from '../src/types/salon';

export default function MySpecialtiesScreen() {
  const { t } = useTranslation();
  const { colors, elevation: elev } = useTheme();

  const [selected, setSelected] = useState<ServiceCategory[]>([]);
  const [initial, setInitial] = useState<ServiceCategory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const showToast = useCallback((msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }, []);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await salonsApi.getMySpecialties();
      const specs = data.specialties.map((s) => s as ServiceCategory);
      setSelected(specs);
      setInitial(specs);
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setIsLoading(false);
    }
  }, [t]);

  useEffect(() => { load(); }, [load]);

  const hasChanges = useMemo(() => {
    if (selected.length !== initial.length) return true;
    return selected.some((s) => !initial.includes(s));
  }, [selected, initial]);

  const isGeneralist = selected.length === 0;

  const toggleCategory = useCallback((cat: ServiceCategory) => {
    setSelected((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat],
    );
  }, []);

  const handleSave = useCallback(async () => {
    setIsSaving(true);
    try {
      await salonsApi.updateMySpecialties(selected);
      setInitial([...selected]);
      showToast(t('mySpecialties.saveSuccess'));
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setIsSaving(false);
    }
  }, [selected, t, showToast]);

  if (isLoading) {
    return (
      <View style={[s.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (error) {
    return (
      <View style={[s.container, { backgroundColor: colors.background }]}>
        <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
          <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
            <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('mySpecialties.title')}</Text>
        </View>
        <View style={s.errorCenter}>
          <Text style={[s.errorText, { color: colors.error }]}>{error}</Text>
          <TouchableOpacity onPress={load}>
            <Text style={[s.retryText, { color: colors.primary }]}>{t('common.actions.retry')}</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.header, { backgroundColor: colors.surface }, elev[1]]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.headerTitle, { color: colors.onSurface }]}>{t('mySpecialties.title')}</Text>
      </View>

      <ScrollView contentContainerStyle={s.scrollContent} showsVerticalScrollIndicator={false}>
        {/* Explanation */}
        <View style={[s.infoCard, { backgroundColor: colors.primaryContainer }]}>
          <MaterialCommunityIcons name="information-outline" size={20} color={colors.onPrimaryContainer} />
          <Text style={[s.infoText, { color: colors.onPrimaryContainer }]}>
            {t('mySpecialties.description')}
          </Text>
        </View>

        {/* Generalist badge */}
        <View style={[s.statusCard, { backgroundColor: colors.surface }, elev[1]]}>
          <MaterialCommunityIcons
            name={isGeneralist ? 'star-circle' : 'tune-vertical'}
            size={28}
            color={isGeneralist ? colors.success : colors.tertiary}
          />
          <View style={{ flex: 1, marginStart: 12 }}>
            <Text style={[s.statusTitle, { color: colors.onSurface }]}>
              {isGeneralist ? t('mySpecialties.generalist') : t('mySpecialties.specialized', { count: selected.length })}
            </Text>
            <Text style={[s.statusHint, { color: colors.onSurfaceVariant }]}>
              {isGeneralist ? t('mySpecialties.generalistHint') : t('mySpecialties.specializedHint')}
            </Text>
          </View>
        </View>

        {/* Category chips */}
        <Text style={[s.sectionLabel, { color: colors.onSurface }]}>{t('mySpecialties.selectCategories')}</Text>
        <View style={s.chipsGrid}>
          {Object.values(ServiceCategory).map((cat) => {
            const meta = SERVICE_CATEGORY_META[cat];
            const isSelected = selected.includes(cat);
            return (
              <TouchableOpacity
                key={cat}
                style={[
                  s.chip,
                  isSelected
                    ? { backgroundColor: colors.primaryContainer }
                    : { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.outlineVariant },
                ]}
                onPress={() => toggleCategory(cat)}
                activeOpacity={0.7}
              >
                <MaterialCommunityIcons
                  name={meta.icon}
                  size={18}
                  color={isSelected ? colors.onPrimaryContainer : colors.onSurfaceVariant}
                />
                <Text style={[
                  s.chipText,
                  { color: isSelected ? colors.onPrimaryContainer : colors.onSurfaceVariant },
                ]}>
                  {t(meta.labelKey)}
                </Text>
                {isSelected && (
                  <MaterialCommunityIcons name="check" size={16} color={colors.onPrimaryContainer} />
                )}
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Reassurance */}
        <View style={[s.reassurance, { backgroundColor: colors.surfaceContainerHigh }]}>
          <MaterialCommunityIcons name="shield-check-outline" size={16} color={colors.onSurfaceVariant} />
          <Text style={[s.reassuranceText, { color: colors.onSurfaceVariant }]}>
            {t('mySpecialties.bookingsNote')}
          </Text>
        </View>

        <View style={{ height: 100 }} />
      </ScrollView>

      {/* Save button */}
      {hasChanges && (
        <View style={[s.bottomBar, { backgroundColor: colors.surface }, elev[2]]}>
          <TouchableOpacity
            style={[s.saveBtn, { backgroundColor: colors.primary }]}
            onPress={handleSave}
            disabled={isSaving}
            activeOpacity={0.7}
          >
            {isSaving ? (
              <ActivityIndicator size="small" color={colors.onPrimary} />
            ) : (
              <Text style={[s.saveBtnText, { color: colors.onPrimary }]}>{t('common.actions.save')}</Text>
            )}
          </TouchableOpacity>
        </View>
      )}

      {/* Toast */}
      {toast && (
        <View style={[s.toast, { backgroundColor: colors.inverseSurface }]}>
          <Text style={[s.toastText, { color: colors.inverseOnSurface }]}>{toast}</Text>
        </View>
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingTop: 52, paddingBottom: 14, gap: 12 },
  headerTitle: { fontFamily: 'Cormorant-Bold', fontSize: 24, fontWeight: '700', flex: 1 },
  scrollContent: { paddingBottom: 100 },

  infoCard: { flexDirection: 'row', alignItems: 'flex-start', gap: 10, marginHorizontal: 16, marginTop: 16, padding: 14, borderRadius: 14 },
  infoText: { fontFamily: 'Manrope-Regular', fontSize: 13, flex: 1, lineHeight: 20 },

  statusCard: { flexDirection: 'row', alignItems: 'center', marginHorizontal: 16, marginTop: 16, padding: 16, borderRadius: 16 },
  statusTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  statusHint: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },

  sectionLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', marginHorizontal: 16, marginTop: 24, marginBottom: 12 },

  chipsGrid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 12, gap: 8 },
  chip: {
    flexDirection: 'row', alignItems: 'center', gap: 8,
    paddingHorizontal: 14, paddingVertical: 10, borderRadius: 999,
  },
  chipText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },

  reassurance: { flexDirection: 'row', alignItems: 'flex-start', gap: 8, marginHorizontal: 16, marginTop: 24, padding: 12, borderRadius: 12 },
  reassuranceText: { fontFamily: 'Manrope-Regular', fontSize: 12, flex: 1, lineHeight: 18 },

  bottomBar: { position: 'absolute', bottom: 0, left: 0, right: 0, padding: 16, paddingBottom: 32 },
  saveBtn: { height: 48, borderRadius: 999, alignItems: 'center', justifyContent: 'center' },
  saveBtnText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },

  errorCenter: { alignItems: 'center', justifyContent: 'center', paddingVertical: 64, gap: 12 },
  errorText: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  retryText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },

  toast: { position: 'absolute', bottom: 40, left: 24, right: 24, paddingVertical: 14, paddingHorizontal: 20, borderRadius: 12, alignItems: 'center' },
  toastText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
