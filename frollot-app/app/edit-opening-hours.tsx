import { useState, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Switch,
  TextInput,
  ActivityIndicator,
  I18nManager,
  Modal,
  Pressable,
} from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { useToast } from '../src/contexts/ToastContext';
import { salonsApi } from '../src/api/salons';
import { TimeRange, OpeningHours } from '../src/types';
import { AccessDenied } from '../src/components/common';
import { LoadingState } from '../src/components/lists';
import { usePermissions } from '../src/hooks/usePermissions';

const DAYS = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'] as const;
type DayKey = typeof DAYS[number];

const DEFAULT_RANGE: TimeRange = { open: '09:00', close: '19:00' };

function parseHours(oh: OpeningHours | undefined | null): Record<DayKey, TimeRange[]> {
  const result: Record<string, TimeRange[]> = {};
  for (const day of DAYS) {
    const ranges = oh?.[day];
    result[day] = ranges && ranges.length > 0 ? ranges.map((r) => ({ ...r })) : [];
  }
  return result as Record<DayKey, TimeRange[]>;
}

function isValidTime(t: string): boolean {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test(t);
}

function validateDay(ranges: TimeRange[]): string | null {
  for (let i = 0; i < ranges.length; i++) {
    if (!isValidTime(ranges[i].open) || !isValidTime(ranges[i].close)) return 'invalidTime';
    if (ranges[i].close <= ranges[i].open) return 'closeBeforeOpen';
  }
  if (ranges.length > 1) {
    const sorted = [...ranges].sort((a, b) => a.open.localeCompare(b.open));
    for (let i = 0; i < sorted.length - 1; i++) {
      if (sorted[i].close > sorted[i + 1].open) return 'overlap';
    }
  }
  return null;
}

export default function EditOpeningHoursScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { showToast } = useToast();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);
  const canEdit = can('salon.update_info');

  const [hours, setHours] = useState<Record<DayKey, TimeRange[]>>(() => parseHours(null));
  const [timezone, setTimezone] = useState('Africa/Libreville');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [copyModalVisible, setCopyModalVisible] = useState(false);
  const [copySourceDay, setCopySourceDay] = useState<DayKey | null>(null);

  const loadSalon = useCallback(async () => {
    if (!salonId) return;
    try {
      const salon = await salonsApi.getSalonById(salonId);
      setHours(parseHours(salon.openingHours));
      setTimezone(salon.timezone || 'Africa/Libreville');
    } catch {} finally {
      setIsLoading(false);
    }
  }, [salonId]);

  useFocusEffect(useCallback(() => { setIsLoading(true); loadSalon(); }, [loadSalon]));

  const toggleDay = (day: DayKey) => {
    setHours((prev) => ({
      ...prev,
      [day]: prev[day].length > 0 ? [] : [{ ...DEFAULT_RANGE }],
    }));
  };

  const updateRange = (day: DayKey, index: number, field: 'open' | 'close', value: string) => {
    setHours((prev) => ({
      ...prev,
      [day]: prev[day].map((r, i) => (i === index ? { ...r, [field]: value } : r)),
    }));
  };

  const addRange = (day: DayKey) => {
    setHours((prev) => {
      const last = prev[day][prev[day].length - 1];
      const newOpen = last ? last.close : '14:00';
      const newClose = newOpen < '18:00' ? '19:00' : '23:00';
      return { ...prev, [day]: [...prev[day], { open: newOpen, close: newClose }] };
    });
  };

  const removeRange = (day: DayKey, index: number) => {
    setHours((prev) => ({
      ...prev,
      [day]: prev[day].filter((_, i) => i !== index),
    }));
  };

  const applyToAll = (sourceDay: DayKey) => {
    const source = hours[sourceDay];
    setHours((prev) => {
      const next = { ...prev };
      for (const day of DAYS) {
        next[day] = source.map((r) => ({ ...r }));
      }
      return next;
    });
    setCopyModalVisible(false);
    showToast(t('openingHours.appliedToAll'), 'success');
  };

  const handleSave = async () => {
    // Validate all days
    for (const day of DAYS) {
      const err = validateDay(hours[day]);
      if (err) {
        showToast(t(`openingHours.validation.${err}`, { day: t(`openingHours.days.${day}`) }), 'error');
        return;
      }
    }

    setIsSaving(true);
    try {
      const openingHours: OpeningHours = {};
      for (const day of DAYS) {
        openingHours[day] = hours[day].length > 0 ? hours[day] : null;
      }
      await salonsApi.updateOpeningHours(salonId!, { openingHours, timezone });
      showToast(t('openingHours.saveSuccess'), 'success');
      router.back();
    } catch (e: any) {
      showToast(e?.response?.data?.error || t('common.states.error'), 'error');
    } finally {
      setIsSaving(false);
    }
  };

  // Guards — hooks above
  if (permLoading || isLoading) return <LoadingState />;
  if (role === 'none' || !canEdit) return <AccessDenied />;

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.topBar, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[s.topTitle, { color: colors.onSurface }]}>{t('openingHours.title')}</Text>
        <TouchableOpacity onPress={handleSave} disabled={isSaving} hitSlop={8}>
          {isSaving ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <Text style={[s.saveBtn, { color: colors.primary }]}>{t('common.actions.save')}</Text>
          )}
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={s.scrollContent}>
        {/* Copy action */}
        <TouchableOpacity
          style={[s.copyAllBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={() => setCopyModalVisible(true)}
        >
          <MaterialCommunityIcons name="content-copy" size={18} color={colors.primary} />
          <Text style={[s.copyAllText, { color: colors.primary }]}>{t('openingHours.applyToAll')}</Text>
        </TouchableOpacity>

        {/* Days */}
        {DAYS.map((day) => {
          const isOpen = hours[day].length > 0;
          return (
            <View key={day} style={[s.dayCard, { backgroundColor: colors.surface }]}>
              <View style={s.dayHeader}>
                <Text style={[s.dayName, { color: colors.onSurface }]}>{t(`openingHours.days.${day}`)}</Text>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                  <Text style={[s.dayStatus, { color: isOpen ? colors.tertiary : colors.onSurfaceVariant }]}>
                    {isOpen ? t('openingHours.open') : t('openingHours.closed')}
                  </Text>
                  <Switch
                    value={isOpen}
                    onValueChange={() => toggleDay(day)}
                    trackColor={{ false: colors.surfaceContainerHigh, true: colors.primaryContainer }}
                    thumbColor={isOpen ? colors.primary : colors.outline}
                  />
                </View>
              </View>

              {isOpen && (
                <View style={s.rangesContainer}>
                  {hours[day].map((range, i) => (
                    <View key={i} style={s.rangeRow}>
                      <TextInput
                        style={[s.timeInput, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
                        value={range.open}
                        onChangeText={(v) => updateRange(day, i, 'open', v)}
                        placeholder="09:00"
                        placeholderTextColor={colors.onSurfaceVariant}
                        maxLength={5}
                        keyboardType="numbers-and-punctuation"
                      />
                      <Text style={[s.rangeSep, { color: colors.onSurfaceVariant }]}>-</Text>
                      <TextInput
                        style={[s.timeInput, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
                        value={range.close}
                        onChangeText={(v) => updateRange(day, i, 'close', v)}
                        placeholder="19:00"
                        placeholderTextColor={colors.onSurfaceVariant}
                        maxLength={5}
                        keyboardType="numbers-and-punctuation"
                      />
                      {hours[day].length > 1 && (
                        <TouchableOpacity onPress={() => removeRange(day, i)} hitSlop={6}>
                          <MaterialCommunityIcons name="close-circle-outline" size={22} color={colors.error} />
                        </TouchableOpacity>
                      )}
                    </View>
                  ))}
                  <TouchableOpacity style={s.addRangeBtn} onPress={() => addRange(day)}>
                    <MaterialCommunityIcons name="plus-circle-outline" size={18} color={colors.primary} />
                    <Text style={[s.addRangeText, { color: colors.primary }]}>{t('openingHours.addRange')}</Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          );
        })}

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* Copy modal */}
      <Modal visible={copyModalVisible} transparent animationType="fade" onRequestClose={() => setCopyModalVisible(false)}>
        <Pressable style={s.modalOverlay} onPress={() => setCopyModalVisible(false)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[s.modalCard, { backgroundColor: colors.surfaceContainerHighest, borderColor: colors.outlineVariant }]}>
            <Text style={[s.modalTitle, { color: colors.onSurface }]}>{t('openingHours.copyFrom')}</Text>
            {DAYS.map((day) => (
              <TouchableOpacity
                key={day}
                style={[s.modalItem, { borderBottomColor: colors.outlineVariant }]}
                onPress={() => applyToAll(day)}
              >
                <Text style={[s.modalItemText, { color: colors.onSurface }]}>{t(`openingHours.days.${day}`)}</Text>
                <Text style={[s.modalItemSub, { color: colors.onSurfaceVariant }]}>
                  {hours[day].length > 0
                    ? hours[day].map((r) => `${r.open}-${r.close}`).join(', ')
                    : t('openingHours.closed')}
                </Text>
              </TouchableOpacity>
            ))}
            <TouchableOpacity onPress={() => setCopyModalVisible(false)} style={[s.modalCancelBtn, { borderColor: colors.outline }]}>
              <Text style={[s.modalCancelText, { color: colors.onSurfaceVariant }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  topBar: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  topTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  saveBtn: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },
  scrollContent: { paddingHorizontal: 16, paddingTop: 8 },
  copyAllBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, paddingVertical: 12, borderRadius: 12, marginBottom: 12 },
  copyAllText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  // Day card
  dayCard: { borderRadius: 16, padding: 16, marginBottom: 10 },
  dayHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  dayName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  dayStatus: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  rangesContainer: { marginTop: 12, gap: 8 },
  rangeRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  timeInput: { flex: 1, minWidth: 0, height: 44, borderRadius: 10, paddingHorizontal: 12, fontSize: 15, fontFamily: 'Manrope-Regular', textAlign: 'center', borderWidth: 1 },
  rangeSep: { flexShrink: 0, fontFamily: 'Manrope-Regular', fontSize: 16, marginHorizontal: 2 },
  addRangeBtn: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4, alignSelf: 'flex-start' },
  addRangeText: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  // Modal
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', paddingHorizontal: 24 },
  modalCard: { borderRadius: 20, borderWidth: 1, padding: 20, maxHeight: '80%' },
  modalTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 17, fontWeight: '600', marginBottom: 12 },
  modalItem: { paddingVertical: 14, borderBottomWidth: 1 },
  modalItemText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  modalItemSub: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  modalCancelBtn: { marginTop: 12, paddingVertical: 12, borderRadius: 999, borderWidth: 1, alignItems: 'center' },
  modalCancelText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
