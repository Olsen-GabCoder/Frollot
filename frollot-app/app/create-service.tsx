import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { ServiceCategory, SERVICE_CATEGORY_META } from '../src/types';

export default function CreateServiceScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [duration, setDuration] = useState('30');
  const [price, setPrice] = useState('');
  const [category, setCategory] = useState<ServiceCategory>(ServiceCategory.COUPE);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const durationNum = parseInt(duration) || 0;
  const priceNum = parseFloat(price) || 0;
  const isFormValid = name.trim() && durationNum >= 1 && durationNum <= 480 && priceNum >= 0 && priceNum <= 10000;

  const handleSubmit = async () => {
    if (!isFormValid || !salonId) return;
    setIsCreating(true);
    setError(null);
    try {
      await salonsApi.createSalonService({
        salonId,
        name: name.trim(),
        description: description.trim() || undefined,
        durationMinutes: durationNum,
        price: price.trim(),
        category,
      });
      Alert.alert(t('common.done'), `${name.trim()} cree avec succes`, [
        { text: 'OK', onPress: () => router.back() },
      ]);
    } catch (e: any) {
      setError(e?.response?.data?.message || t('common.error'));
    } finally {
      setIsCreating(false);
    }
  };

  const categories = Object.values(ServiceCategory).map((cat) => ({
    key: cat,
    ...SERVICE_CATEGORY_META[cat],
  }));

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>{t('service.createService')}</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('service.serviceName') + ' *'} placeholderTextColor={colors.onSurfaceVariant} value={name} onChangeText={setName} />

        <TextInput style={[styles.input, styles.textArea, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('salon.description')} placeholderTextColor={colors.onSurfaceVariant} value={description} onChangeText={setDescription} multiline textAlignVertical="top" />

        <View style={styles.row}>
          <View style={styles.half}>
            <Text style={[typo.labelMedium, { color: colors.onSurface, marginBottom: 4 }]}>{t('service.duration')} (min)</Text>
            <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              value={duration} onChangeText={(v) => setDuration(v.replace(/[^0-9]/g, ''))} keyboardType="numeric" />
          </View>
          <View style={styles.half}>
            <Text style={[typo.labelMedium, { color: colors.onSurface, marginBottom: 4 }]}>{t('service.price')} (EUR)</Text>
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
              <Text style={{ fontSize: 24 }}>{cat.emoji}</Text>
              <Text style={[typo.labelSmall, { color: category === cat.key ? colors.onPrimaryContainer : colors.onSurfaceVariant, marginTop: 4 }]}>
                {cat.label}
              </Text>
              {category === cat.key && (
                <MaterialIcons name="check-circle" size={16} color={colors.primary} style={styles.checkIcon} />
              )}
            </TouchableOpacity>
          ))}
        </View>

        {error && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
          </View>
        )}

        <TouchableOpacity
          style={[styles.submitBtn, { backgroundColor: isFormValid ? colors.primary : colors.surfaceContainerHigh }]}
          onPress={handleSubmit} disabled={!isFormValid || isCreating}
        >
          {isCreating ? <ActivityIndicator color={colors.onPrimary} /> : (
            <Text style={[typo.labelLarge, { color: isFormValid ? colors.onPrimary : colors.onSurfaceVariant }]}>
              {t('service.createService')}
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
  checkIcon: { position: 'absolute', top: 8, right: 8 },
  errorCard: { padding: 12, borderRadius: 12, marginBottom: 12 },
  submitBtn: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginTop: 8 },
});
