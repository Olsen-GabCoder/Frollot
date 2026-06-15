import { useState } from 'react';
import {
  View, Text, ScrollView, TouchableOpacity,
  StyleSheet, ActivityIndicator, KeyboardAvoidingView, Platform, I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { mediaApi } from '../src/api/media';
import { TextField } from '../src/components/ui';
import { PrimaryButton } from '../src/components/ui';

export default function CreateSalonScreen() {
  const { ownerId } = useLocalSearchParams<{ ownerId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();

  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [description, setDescription] = useState('');
  const [coverUri, setCoverUri] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const isFormValid = name.trim() && address.trim() && city.trim() && postalCode.trim();

  const pickCover = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (!result.canceled && result.assets[0]) setCoverUri(result.assets[0].uri);
  };

  const handleSubmit = async () => {
    if (!isFormValid || !ownerId) return;
    setIsLoading(true);
    setError(null);
    try {
      let coverPhotoUrl: string | undefined;
      if (coverUri) {
        coverPhotoUrl = await mediaApi.uploadImage(coverUri, `salon_${Date.now()}.jpg`);
      }
      await salonsApi.createSalon({
        id: crypto.randomUUID?.() || `${Date.now()}`,
        name: name.trim(),
        address: address.trim(),
        city: city.trim(),
        postalCode: postalCode.trim(),
        description: description.trim() || undefined,
        ownerId,
        createdAt: new Date().toISOString(),
        coverPhotoUrl,
      });
      setSuccess(true);
      setTimeout(() => router.back(), 2000);
    } catch (e: any) {
      setError(e?.response?.data?.message || t('salon.create.error'));
    } finally {
      setIsLoading(false);
    }
  };

  if (success) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center', padding: 32 }]}>
        <View style={[styles.successIcon, { backgroundColor: colors.successContainer }]}>
          <MaterialCommunityIcons name="check-circle" size={56} color={colors.success} />
        </View>
        <Text style={[styles.successTitle, { color: colors.onSurface }]}>{t('salon.create.successTitle')}</Text>
        <Text style={[styles.successDesc, { color: colors.onSurfaceVariant }]}>{t('salon.create.successDesc')}</Text>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView style={[styles.container, { backgroundColor: colors.background }]} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity style={styles.backBtn} onPress={() => router.back()}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? "arrow-right" : "arrow-left"} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <View style={{ flex: 1 }}>
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>{t('salon.create.title')}</Text>
          <Text style={[styles.headerSub, { color: colors.onSurfaceVariant }]}>{t('salon.create.subtitle')}</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        {/* Cover photo */}
        <TouchableOpacity style={[styles.coverPicker, { borderColor: colors.outlineVariant }]} onPress={pickCover} activeOpacity={0.8}>
          {coverUri ? (
            <Image source={{ uri: coverUri }} style={styles.coverImage} contentFit="cover" />
          ) : (
            <View style={[styles.coverPlaceholder, { backgroundColor: colors.surfaceContainerHigh }]}>
              <MaterialCommunityIcons name="image-plus" size={40} color={colors.onSurfaceVariant} />
              <Text style={[styles.coverLabel, { color: colors.onSurfaceVariant }]}>{t('salon.create.coverLabel')}</Text>
              <Text style={[styles.coverHint, { color: colors.outline }]}>{t('salon.create.coverHint')}</Text>
            </View>
          )}
        </TouchableOpacity>

        {/* Fields */}
        <View style={styles.fields}>
          <TextField
            label={t('salon.salonName') + ' *'}
            icon="storefront"
            value={name}
            onChangeText={setName}
            placeholder={t('salon.create.namePlaceholder')}
          />

          <TextField
            label={t('salon.address') + ' *'}
            icon="map-marker-outline"
            value={address}
            onChangeText={setAddress}
            placeholder={t('salon.create.addressPlaceholder')}
          />

          <View style={styles.row}>
            <View style={{ flex: 1 }}>
              <TextField
                label={t('salon.city') + ' *'}
                value={city}
                onChangeText={setCity}
                placeholder={t('salon.create.cityPlaceholder')}
              />
            </View>
            <View style={{ flex: 1 }}>
              <TextField
                label={t('salon.postalCode') + ' *'}
                value={postalCode}
                onChangeText={setPostalCode}
                placeholder={t('salon.create.postalCodePlaceholder')}
                keyboardType="numeric"
              />
            </View>
          </View>

          <TextField
            label={t('salon.description')}
            icon="text-box-outline"
            value={description}
            onChangeText={setDescription}
            placeholder={t('salon.create.descriptionPlaceholder')}
            multiline
            numberOfLines={4}
          />
        </View>

        {/* Error */}
        {error && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <MaterialCommunityIcons name="alert-circle" size={18} color={colors.onErrorContainer} />
            <Text style={[styles.errorText, { color: colors.onErrorContainer }]}>{error}</Text>
          </View>
        )}

        {/* Submit */}
        <PrimaryButton
          icon="check"
          full
          onPress={handleSubmit}
          loading={isLoading}
          disabled={!isFormValid}
          style={styles.submitBtn}
        >
          {t('salon.create.submit')}
        </PrimaryButton>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  // Header
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  backBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 22, fontWeight: '600', lineHeight: 24 },
  headerSub: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  // Content
  scroll: { padding: 20, paddingBottom: 40 },
  // Cover
  coverPicker: { height: 180, borderRadius: 16, borderWidth: 1, borderStyle: 'dashed', overflow: 'hidden', marginBottom: 24 },
  coverImage: { width: '100%', height: '100%' },
  coverPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center', borderRadius: 16, gap: 6 },
  coverLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  coverHint: { fontFamily: 'Manrope-Regular', fontSize: 11 },
  // Fields
  fields: { gap: 14 },
  row: { flexDirection: 'row', gap: 12 },
  // Error
  errorCard: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14, borderRadius: 12, marginTop: 16 },
  errorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  // Submit
  submitBtn: {
    marginTop: 24,
    shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.12, shadowRadius: 6, elevation: 2, // design-fixed
  },
  // Success
  successIcon: { width: 96, height: 96, borderRadius: 48, alignItems: 'center', justifyContent: 'center', marginBottom: 20 },
  successTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 28, fontWeight: '600', marginBottom: 8 },
  successDesc: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
});
