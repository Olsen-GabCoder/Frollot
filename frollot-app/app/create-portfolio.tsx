import { useEffect, useState } from 'react';
import {
  View, Text, TextInput, ScrollView, TouchableOpacity, Switch,
  StyleSheet, ActivityIndicator, Alert, I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../src/theme';
import { portfoliosApi } from '../src/api/portfolios';
import { salonsApi } from '../src/api/salons';
import { mediaApi } from '../src/api/media';
import { Salon } from '../src/types';

export default function CreatePortfolioScreen() {
  const { ownerId, ownerType } = useLocalSearchParams<{ ownerId: string; ownerType: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [isPublic, setIsPublic] = useState(true);
  const [coverUri, setCoverUri] = useState<string | null>(null);
  const [salons, setSalons] = useState<Salon[]>([]);
  const [selectedSalonId, setSelectedSalonId] = useState<string | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (ownerType === 'salon' && ownerId) {
      salonsApi.getSalonsByOwner(ownerId).then(setSalons).catch(() => {});
    }
  }, [ownerId, ownerType]);

  const pickCover = async () => {
    const r = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (!r.canceled && r.assets[0]) setCoverUri(r.assets[0].uri);
  };

  const handleCreate = async () => {
    if (!name.trim() || !ownerId) return;
    setIsCreating(true);
    setError(null);
    try {
      let coverImageUrl: string | undefined;
      if (coverUri) coverImageUrl = await mediaApi.uploadImage(coverUri, `portfolio_${Date.now()}.jpg`);
      const actualOwnerId = ownerType === 'salon' && selectedSalonId ? selectedSalonId : ownerId;
      await portfoliosApi.createPortfolio({
        ownerId: actualOwnerId,
        ownerType: ownerType || 'user',
        title: name.trim(),
        description: description.trim() || undefined,
        coverImageUrl,
        isPublic,
      });
      Alert.alert(t('common.actions.done'), t('portfolio.created'), [{ text: t('common.actions.ok'), onPress: () => router.back() }]);
    } catch (e: any) {
      setError(e?.response?.data?.message || t('common.states.error'));
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}><MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} /></TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>{t('portfolio.create')}</Text>
      </View>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <TouchableOpacity style={[styles.coverPicker, { backgroundColor: colors.surfaceContainerHigh, borderColor: colors.outlineVariant }]} onPress={pickCover}>
          {coverUri ? <Image source={{ uri: coverUri }} style={styles.coverImg} contentFit="cover" /> : (
            <View style={styles.coverPlaceholder}><MaterialIcons name="add-photo-alternate" size={36} color={colors.onSurfaceVariant} /></View>
          )}
        </TouchableOpacity>
        {ownerType === 'salon' && salons.length > 0 && (
          <View style={[styles.section, { backgroundColor: colors.surface }]}>
            <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8 }]}>{t('portfolio.salonLabel')}</Text>
            {salons.map((s) => (
              <TouchableOpacity key={s.id} style={styles.radioRow} onPress={() => setSelectedSalonId(s.id)}>
                <MaterialIcons name={selectedSalonId === s.id ? 'radio-button-checked' : 'radio-button-unchecked'} size={20} color={colors.primary} />
                <Text style={[typo.bodyMedium, { color: colors.onSurface, marginStart: 8 }]}>{s.name}</Text>
              </TouchableOpacity>
            ))}
          </View>
        )}
        <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('portfolio.namePlaceholder')} placeholderTextColor={colors.onSurfaceVariant} value={name} onChangeText={setName} />
        <TextInput style={[styles.input, styles.textArea, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('portfolio.descriptionPlaceholder')} placeholderTextColor={colors.onSurfaceVariant} value={description} onChangeText={setDescription} multiline textAlignVertical="top" />
        <View style={styles.switchRow}>
          <Text style={[typo.bodyLarge, { color: colors.onSurface, flex: 1 }]}>{t('portfolio.publicLabel')}</Text>
          <Switch value={isPublic} onValueChange={setIsPublic} trackColor={{ true: colors.primary }} />
        </View>
        {error && <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}><Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text></View>}
        <TouchableOpacity style={[styles.submitBtn, { backgroundColor: name.trim() ? colors.primary : colors.surfaceContainerHigh }]} onPress={handleCreate} disabled={!name.trim() || isCreating}>
          {isCreating ? <ActivityIndicator color={colors.onPrimary} /> : <Text style={[typo.labelLarge, { color: name.trim() ? colors.onPrimary : colors.onSurfaceVariant }]}>{t('portfolio.createButton')}</Text>}
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}
const styles = StyleSheet.create({
  container: { flex: 1 }, header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scroll: { padding: 16 }, coverPicker: { height: 160, borderRadius: 16, borderWidth: 1, borderStyle: 'dashed', overflow: 'hidden', marginBottom: 16 },
  coverImg: { width: '100%', height: '100%' }, coverPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  section: { borderRadius: 16, padding: 16, marginBottom: 12 }, radioRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  input: { height: 52, borderRadius: 12, paddingHorizontal: 16, marginBottom: 12, fontSize: 16, borderWidth: 1 },
  textArea: { height: 80, paddingTop: 16 }, switchRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 16 },
  errorCard: { padding: 12, borderRadius: 12, marginBottom: 12 },
  submitBtn: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center' },
});
