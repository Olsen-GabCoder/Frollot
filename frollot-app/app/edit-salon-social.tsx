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
import { profilesApi } from '../src/api/profiles';
import { mediaApi } from '../src/api/media';
import { TextField } from '../src/components/ui';
import { PrimaryButton } from '../src/components/ui';
import { useToast } from '../src/contexts/ToastContext';
import { usePermissions } from '../src/hooks/usePermissions';
import { AccessDenied } from '../src/components/common';
import { resolveMediaUrl } from '../src/utils/media';

export default function EditSalonSocialScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { showToast } = useToast();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);

  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [socialDescription, setSocialDescription] = useState('');
  const [socialCoverImage, setSocialCoverImage] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploadingCover, setIsUploadingCover] = useState(false);

  const loadProfile = useCallback(async () => {
    if (!salonId) return;
    setIsLoadingProfile(true);
    try {
      const data = await profilesApi.getSalonSocialProfile(salonId);
      setSocialDescription(data.socialDescription || '');
      setSocialCoverImage(data.socialCoverImage || null);
    } catch (e: any) {
      showToast(e?.message || String(e), 'error');
    } finally {
      setIsLoadingProfile(false);
    }
  }, [salonId]);

  useEffect(() => { loadProfile(); }, [loadProfile]);

  const handlePickCover = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes: ['images'], quality: 0.8 });
    if (result.canceled || !result.assets[0]) return;
    setIsUploadingCover(true);
    try {
      const fileName = `salon_social_cover_${salonId}_${Date.now()}.jpg`;
      const url = await mediaApi.uploadImage(result.assets[0].uri, fileName);
      setSocialCoverImage(url);
      showToast(t('salon.social.coverUploaded'), 'success');
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || String(e), 'error');
    } finally {
      setIsUploadingCover(false);
    }
  };

  const handleSubmit = async () => {
    if (!salonId) return;
    setIsSaving(true);
    try {
      await profilesApi.updateSalonSocialProfile(salonId, {
        socialDescription: socialDescription.trim() || undefined,
        socialCoverImage: socialCoverImage || undefined,
      });
      showToast(t('salon.social.saveSuccess'), 'success');
      if (router.canGoBack()) router.back();
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.response?.data?.error || e?.message || String(e), 'error');
    } finally {
      setIsSaving(false);
    }
  };

  if (permLoading || isLoadingProfile) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (role === 'none' || !can('social.update_profile')) {
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
          <Text style={[styles.headerTitle, { color: colors.onSurface }]}>{t('salon.social.editTitle')}</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        {/* Social cover image */}
        <Text style={[styles.sectionLabel, { color: colors.onSurfaceVariant }]}>{t('salon.social.coverLabel')}</Text>
        <TouchableOpacity style={[styles.coverPicker, { borderColor: colors.outlineVariant }]} onPress={handlePickCover} activeOpacity={0.8} disabled={isUploadingCover}>
          {socialCoverImage ? (
            <Image source={{ uri: resolveMediaUrl(socialCoverImage) }} style={styles.coverImage} contentFit="cover" />
          ) : (
            <View style={[styles.coverPlaceholder, { backgroundColor: colors.surfaceContainerHigh }]}>
              <MaterialCommunityIcons name="image-plus" size={40} color={colors.onSurfaceVariant} />
              <Text style={[styles.coverHint, { color: colors.onSurfaceVariant }]}>{t('salon.social.coverHint')}</Text>
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

        {/* Social description */}
        <Text style={[styles.sectionLabel, { color: colors.onSurfaceVariant }]}>{t('salon.social.descriptionLabel')}</Text>
        <TextField
          label={t('salon.social.descriptionLabel')}
          icon="text-box-outline"
          value={socialDescription}
          onChangeText={setSocialDescription}
          multiline
          numberOfLines={6}
          maxLength={2000}
        />

        {/* Submit */}
        <PrimaryButton
          icon="check"
          full
          onPress={handleSubmit}
          loading={isSaving}
          style={styles.submitBtn}
        >
          {t('salon.social.save')}
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
  scroll: { padding: 20, paddingBottom: 40 },
  sectionLabel: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 18, fontWeight: '600', marginTop: 12, marginBottom: 8 },
  coverPicker: { height: 180, borderRadius: 16, borderWidth: 1, borderStyle: 'dashed', overflow: 'hidden', marginBottom: 16 },
  coverImage: { width: '100%', height: '100%' },
  coverPlaceholder: { flex: 1, justifyContent: 'center', alignItems: 'center', borderRadius: 16, gap: 6 },
  coverHint: { fontFamily: 'Manrope-Regular', fontSize: 13 },
  coverEditBadge: { position: 'absolute', bottom: 10, end: 10, width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  submitBtn: {
    marginTop: 24,
    shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.12, shadowRadius: 6, elevation: 2,
  },
});
