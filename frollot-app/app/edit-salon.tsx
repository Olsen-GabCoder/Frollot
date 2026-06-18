import { useState, useCallback, useEffect } from 'react';
import {
  View, Text, ScrollView, TouchableOpacity,
  StyleSheet, ActivityIndicator, KeyboardAvoidingView, Platform, I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { mediaApi } from '../src/api/media';
import { TextField } from '../src/components/ui';
import { PrimaryButton } from '../src/components/ui';
import { useToast } from '../src/contexts/ToastContext';
import { usePermissions } from '../src/hooks/usePermissions';
import { AccessDenied } from '../src/components/common';
import { resolveMediaUrl } from '../src/utils/media';
import { Salon } from '../src/types';

export default function EditSalonScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { showToast } = useToast();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);

  const [salon, setSalon] = useState<Salon | null>(null);
  const [isLoadingSalon, setIsLoadingSalon] = useState(true);

  // Form fields
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [description, setDescription] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');
  const [websiteUrl, setWebsiteUrl] = useState('');

  const [isSaving, setIsSaving] = useState(false);
  const [isUploadingCover, setIsUploadingCover] = useState(false);
  const [coverUrl, setCoverUrl] = useState<string | null>(null);

  const loadSalon = useCallback(async () => {
    if (!salonId) return;
    setIsLoadingSalon(true);
    try {
      const data = await salonsApi.getSalonById(salonId);
      setSalon(data);
      setCoverUrl(data.coverPhotoUrl || null);
      setName(data.name || '');
      setAddress(data.address || '');
      setCity(data.city || '');
      setPostalCode(data.postalCode || '');
      setDescription(data.description || '');
      setPhoneNumber(data.phoneNumber || '');
      setEmail(data.email || '');
      setWebsiteUrl(data.websiteUrl || '');
    } catch (e: any) {
      showToast(e?.message || String(e), 'error');
    } finally {
      setIsLoadingSalon(false);
    }
  }, [salonId]);

  useEffect(() => { loadSalon(); }, [loadSalon]);

  const isFormValid = name.trim() && address.trim() && city.trim() && postalCode.trim();

  const handlePickCover = async () => {
    if (!salonId) return;
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (result.canceled || !result.assets[0]) return;
    setIsUploadingCover(true);
    try {
      const fileName = `salon_cover_${salonId}_${Date.now()}.jpg`;
      const path = await mediaApi.uploadImage(result.assets[0].uri, fileName);
      await salonsApi.updateSalonCoverPhoto(salonId, path);
      setCoverUrl(path);
      showToast(t('salon.edit.coverSuccess'), 'success');
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || String(e), 'error');
    } finally {
      setIsUploadingCover(false);
    }
  };

  const handleSubmit = async () => {
    if (!isFormValid || !salonId) return;
    setIsSaving(true);
    try {
      await salonsApi.updateSalon(salonId, {
        name: name.trim(),
        address: address.trim(),
        city: city.trim(),
        postalCode: postalCode.trim(),
        description: description.trim() || undefined,
        phoneNumber: phoneNumber.trim() || undefined,
        email: email.trim() || undefined,
        websiteUrl: websiteUrl.trim() || undefined,
      });
      showToast(t('salon.edit.success'), 'success');
      if (router.canGoBack()) router.back();
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.response?.data?.error || e?.message || String(e), 'error');
    } finally {
      setIsSaving(false);
    }
  };

  if (permLoading || isLoadingSalon) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (role === 'none' || !can('salon.update_info')) {
    return <AccessDenied />;
  }

  return (
    <KeyboardAvoidingView style={[styles.container, { backgroundColor: colors.background }]} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity style={styles.backBtn} onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <View style={{ flex: 1 }}>
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>{t('salon.edit.title')}</Text>
          <Text style={[styles.headerSub, { color: colors.onSurfaceVariant }]}>{salon?.name || ''}</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        {/* Cover photo */}
        <TouchableOpacity style={[styles.coverPicker, { borderColor: colors.outlineVariant }]} onPress={handlePickCover} activeOpacity={0.8} disabled={isUploadingCover}>
          {coverUrl ? (
            <Image source={{ uri: resolveMediaUrl(coverUrl) }} style={styles.coverImage} contentFit="cover" />
          ) : (
            <View style={[styles.coverPlaceholder, { backgroundColor: colors.surfaceContainerHigh }]}>
              <MaterialCommunityIcons name="image-plus" size={40} color={colors.onSurfaceVariant} />
              <Text style={[styles.coverLabel, { color: colors.onSurfaceVariant }]}>{t('salon.edit.coverLabel')}</Text>
            </View>
          )}
          <View style={[styles.coverEditBadge, { backgroundColor: colors.primary }]}>
            {isUploadingCover ? (
              <ActivityIndicator size="small" color={colors.onPrimary} />
            ) : (
              <MaterialCommunityIcons name="camera" size={18} color={colors.onPrimary} />
            )}
          </View>
        </TouchableOpacity>

        {/* Identity fields */}
        <Text style={[styles.sectionLabel, { color: colors.onSurfaceVariant }]}>{t('salon.edit.sectionIdentity')}</Text>
        <View style={styles.fields}>
          <TextField
            label={t('salon.salonName') + ' *'}
            icon="storefront"
            value={name}
            onChangeText={setName}
          />
          <TextField
            label={t('salon.description')}
            icon="text-box-outline"
            value={description}
            onChangeText={setDescription}
            multiline
            numberOfLines={4}
          />
        </View>

        {/* Location fields */}
        <Text style={[styles.sectionLabel, { color: colors.onSurfaceVariant }]}>{t('salon.edit.sectionLocation')}</Text>
        <View style={styles.fields}>
          <TextField
            label={t('salon.address') + ' *'}
            icon="map-marker-outline"
            value={address}
            onChangeText={setAddress}
          />
          <View style={styles.row}>
            <View style={{ flex: 1 }}>
              <TextField
                label={t('salon.city') + ' *'}
                value={city}
                onChangeText={setCity}
              />
            </View>
            <View style={{ flex: 1 }}>
              <TextField
                label={t('salon.postalCode') + ' *'}
                value={postalCode}
                onChangeText={setPostalCode}
                keyboardType="numeric"
              />
            </View>
          </View>
        </View>

        {/* Contact fields */}
        <Text style={[styles.sectionLabel, { color: colors.onSurfaceVariant }]}>{t('salon.edit.sectionContact')}</Text>
        <View style={styles.fields}>
          <TextField
            label={t('salon.edit.phoneNumber')}
            icon="phone-outline"
            value={phoneNumber}
            onChangeText={setPhoneNumber}
            keyboardType="phone-pad"
          />
          <TextField
            label={t('salon.edit.email')}
            icon="email-outline"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <TextField
            label={t('salon.edit.websiteUrl')}
            icon="web"
            value={websiteUrl}
            onChangeText={setWebsiteUrl}
            keyboardType="url"
            autoCapitalize="none"
          />
        </View>

        {/* Submit */}
        <PrimaryButton
          icon="check"
          full
          onPress={handleSubmit}
          loading={isSaving}
          disabled={!isFormValid}
          style={styles.submitBtn}
        >
          {t('salon.edit.save')}
        </PrimaryButton>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 14, paddingHorizontal: 16, gap: 12 },
  backBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 22, fontWeight: '600', lineHeight: 24 },
  headerSub: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  scroll: { padding: 20, paddingBottom: 40 },
  coverPicker: { height: 180, borderRadius: 16, borderWidth: 1, borderStyle: 'dashed', overflow: 'hidden', marginBottom: 8 },
  coverImage: { width: '100%', height: '100%' },
  coverPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center', borderRadius: 16, gap: 6 },
  coverLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  coverEditBadge: { position: 'absolute', bottom: 10, end: 10, width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  sectionLabel: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 18, fontWeight: '600', marginTop: 20, marginBottom: 8 },
  fields: { gap: 14 },
  row: { flexDirection: 'row', gap: 12 },
  submitBtn: {
    marginTop: 24,
    shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.12, shadowRadius: 6, elevation: 2,
  },
});
