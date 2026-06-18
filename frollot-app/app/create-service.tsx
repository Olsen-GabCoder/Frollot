import { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons, MaterialCommunityIcons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { Image } from 'expo-image';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { mediaApi } from '../src/api/media';
import { ServiceCategory, SERVICE_CATEGORY_META } from '../src/types';
import { resolveMediaUrl } from '../src/utils/media';
import { AccessDenied } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';

const MAX_IMAGES = 5;

// An image is either already-uploaded (path string) or a local pick (uri string, needs upload)
interface ImageSlot {
  /** Resolved display URI (for <Image>) */
  displayUri: string;
  /** If already on server: the path to send back. If local: null (needs upload). */
  serverPath: string | null;
  /** If local pick: the raw URI for upload. */
  localUri: string | null;
}

export default function CreateServiceScreen() {
  const { salonId, serviceId } = useLocalSearchParams<{ salonId: string; serviceId?: string }>();
  const isEdit = !!serviceId;
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;
  const { role, isLoading: permLoading } = usePermissions(salonId);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [duration, setDuration] = useState('30');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState<ServiceCategory>(ServiceCategory.COUPE);
  const [images, setImages] = useState<ImageSlot[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingService, setIsLoadingService] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // --- Prefill in edit mode ---
  useEffect(() => {
    if (!isEdit || !salonId || !serviceId) return;
    setIsLoadingService(true);
    salonsApi.getServiceById(salonId, serviceId)
      .then((svc) => {
        setName(svc.name);
        setDescription(svc.description || '');
        setDuration(String(svc.durationMinutes));
        setPrice(String(svc.price));
        setCategory(svc.category);
        if (svc.imageUrls && svc.imageUrls.length > 0) {
          setImages(svc.imageUrls.map((path) => ({
            displayUri: resolveMediaUrl(path) || path,
            serverPath: path,
            localUri: null,
          })));
        }
      })
      .catch((e: any) => {
        Alert.alert(t('common.states.error'), e?.message || String(e));
      })
      .finally(() => setIsLoadingService(false));
  }, [isEdit, salonId, serviceId, t]);

  const durationNum = parseInt(duration) || 0;
  const priceNum = parseFloat(price) || 0;
  const isFormValid = name.trim() && durationNum >= 1 && durationNum <= 480 && priceNum >= 0 && priceNum <= 10000;

  // --- Image picking ---
  const pickImages = useCallback(async () => {
    const remaining = MAX_IMAGES - images.length;
    if (remaining <= 0) return;
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsMultipleSelection: remaining > 1,
      selectionLimit: remaining,
      quality: 0.8,
    });
    if (result.canceled || !result.assets?.length) return;
    const newSlots: ImageSlot[] = result.assets.slice(0, remaining).map((a) => ({
      displayUri: a.uri,
      serverPath: null,
      localUri: a.uri,
    }));
    setImages((prev) => [...prev, ...newSlots]);
  }, [images.length]);

  const removeImage = useCallback((index: number) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
  }, []);

  // --- Submit (create or update) ---
  const handleSubmit = async () => {
    if (!isFormValid || !salonId) return;
    setIsSubmitting(true);
    setError(null);

    try {
      // 1. Upload new (local) images
      const finalPaths: string[] = [];
      for (const img of images) {
        if (img.serverPath) {
          finalPaths.push(img.serverPath);
        } else if (img.localUri) {
          const fileName = `svc_${salonId}_${Date.now()}_${finalPaths.length}.jpg`;
          const path = await mediaApi.uploadImage(img.localUri, fileName);
          finalPaths.push(path);
        }
      }

      // 2. Build payload
      const imageUrls = finalPaths.length > 0 ? finalPaths : undefined;

      if (isEdit && serviceId) {
        await salonsApi.updateSalonService(salonId, serviceId, {
          name: name.trim(),
          description: description.trim() || undefined,
          durationMinutes: durationNum,
          price: price.trim(),
          category,
          imageUrls,
        });
        Alert.alert(t('common.actions.done'), t('service.updatedSuccess', { name: name.trim() }), [
          { text: t('common.actions.ok'), onPress: () => router.canGoBack() ? router.back() : router.replace('/(tabs)') },
        ]);
      } else {
        await salonsApi.createSalonService({
          salonId,
          name: name.trim(),
          description: description.trim() || undefined,
          durationMinutes: durationNum,
          price: price.trim(),
          category,
          imageUrls,
        });
        Alert.alert(t('common.actions.done'), t('service.createdSuccess', { name: name.trim() }), [
          { text: t('common.actions.ok'), onPress: () => router.canGoBack() ? router.back() : router.replace('/(tabs)') },
        ]);
      }
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const categories = Object.values(ServiceCategory).map((cat) => ({
    key: cat,
    ...SERVICE_CATEGORY_META[cat],
  }));

  if (permLoading) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }
  if (role === 'none') {
    return <AccessDenied />;
  }

  if (isLoadingService) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>
          {isEdit ? t('service.editService') : t('service.createService')}
        </Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('service.serviceName') + ' *'} placeholderTextColor={colors.onSurfaceVariant} value={name} onChangeText={setName} />

        <TextInput style={[styles.input, styles.textArea, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('salon.description')} placeholderTextColor={colors.onSurfaceVariant} value={description} onChangeText={setDescription} multiline textAlignVertical="top" />

        <View style={styles.row}>
          <View style={styles.half}>
            <Text style={[typo.labelMedium, { color: colors.onSurface, marginBottom: 4 }]}>{t('service.duration')} ({t('service.durationUnit')})</Text>
            <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              value={duration} onChangeText={(v) => setDuration(v.replace(/[^0-9]/g, ''))} keyboardType="numeric" />
          </View>
          <View style={styles.half}>
            <Text style={[typo.labelMedium, { color: colors.onSurface, marginBottom: 4 }]}>{t('service.price')} ({t('service.priceUnit')})</Text>
            <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              value={price} onChangeText={(v) => setPrice(v.replace(/[^0-9.]/g, ''))} keyboardType="decimal-pad" placeholder="0.00" placeholderTextColor={colors.onSurfaceVariant} />
          </View>
        </View>

        {/* Category selector */}
        <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8 }]}>{t('service.category')}</Text>
        <View style={styles.categoryGrid}>
          {categories.map((cat) => (
            <TouchableOpacity
              key={cat.key}
              style={[styles.categoryCard, {
                backgroundColor: category === cat.key ? colors.primaryContainer : colors.surface,
                borderColor: category === cat.key ? colors.primary : colors.outlineVariant,
              }]}
              onPress={() => setCategory(cat.key)}
            >
              <MaterialCommunityIcons name={cat.icon} size={24} color={category === cat.key ? colors.onPrimaryContainer : colors.onSurfaceVariant} />
              <Text style={[typo.labelSmall, { color: category === cat.key ? colors.onPrimaryContainer : colors.onSurfaceVariant, marginTop: 4 }]}>
                {t(cat.labelKey)}
              </Text>
              {category === cat.key && (
                <MaterialIcons name="check-circle" size={16} color={colors.primary} style={styles.checkIcon} />
              )}
            </TouchableOpacity>
          ))}
        </View>

        {/* Image gallery */}
        <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8 }]}>
          {t('service.addImages')} ({images.length}/{MAX_IMAGES})
        </Text>
        <View style={styles.galleryRow}>
          {images.map((img, i) => (
            <View key={i} style={styles.galleryThumb}>
              <Image source={{ uri: img.displayUri }} style={styles.galleryImg} contentFit="cover" />
              <TouchableOpacity style={[styles.galleryRemove, { backgroundColor: colors.error }]} onPress={() => removeImage(i)}>
                <MaterialIcons name="close" size={14} color={colors.onError} />
              </TouchableOpacity>
            </View>
          ))}
          {images.length < MAX_IMAGES && (
            <TouchableOpacity style={[styles.galleryAdd, { borderColor: colors.outlineVariant }]} onPress={pickImages}>
              <MaterialCommunityIcons name="camera-plus-outline" size={24} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          )}
        </View>

        {error && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
          </View>
        )}

        <TouchableOpacity
          style={[styles.submitBtn, { backgroundColor: isFormValid ? colors.primary : colors.surfaceContainerHigh }]}
          onPress={handleSubmit} disabled={!isFormValid || isSubmitting}
        >
          {isSubmitting ? <ActivityIndicator color={colors.onPrimary} /> : (
            <Text style={[typo.labelLarge, { color: isFormValid ? colors.onPrimary : colors.onSurfaceVariant }]}>
              {isEdit ? t('service.saveChanges') : t('service.createService')}
            </Text>
          )}
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scroll: { padding: 16 },
  input: { height: 52, borderRadius: 12, paddingHorizontal: 16, marginBottom: 12, fontSize: 16, borderWidth: 1 },
  textArea: { height: 80, paddingTop: 16 },
  row: { flexDirection: 'row', gap: 12 },
  half: { flex: 1 },
  categoryGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  categoryCard: {
    width: '48%', padding: 16, borderRadius: 12, borderWidth: 1,
    alignItems: 'center', position: 'relative',
  },
  checkIcon: { position: 'absolute', top: 8, end: 8 },
  // Image gallery
  galleryRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  galleryThumb: { width: 72, height: 72, borderRadius: 10, overflow: 'hidden' },
  galleryImg: { width: '100%', height: '100%' },
  galleryRemove: { position: 'absolute', top: 2, end: 2, width: 20, height: 20, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  galleryAdd: { width: 72, height: 72, borderRadius: 10, borderWidth: 1.5, borderStyle: 'dashed', alignItems: 'center', justifyContent: 'center' },
  errorCard: { padding: 12, borderRadius: 12, marginBottom: 12 },
  submitBtn: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginTop: 8 },
});
