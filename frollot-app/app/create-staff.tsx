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
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { usersApi } from '../src/api/users';
import { ServiceCategory, SERVICE_CATEGORY_META } from '../src/types';

export default function CreateStaffScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [specialties, setSpecialties] = useState<Set<ServiceCategory>>(new Set());
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const isFormValid = firstName.trim() && lastName.trim() && emailValid && specialties.size > 0;

  const toggleSpecialty = (cat: ServiceCategory) => {
    setSpecialties((prev) => {
      const next = new Set(prev);
      if (next.has(cat)) next.delete(cat);
      else next.add(cat);
      return next;
    });
  };

  const handleSubmit = async () => {
    if (!isFormValid || !salonId) return;
    setIsCreating(true);
    setError(null);
    try {
      // Search for existing user
      const users = await usersApi.searchUsers(email.trim());
      let userId: string;

      if (users.length > 0) {
        const existing = users.find((u) => u.email === email.trim());
        if (existing) {
          if (existing.userType !== 'hairstylist') {
            setError(t('staff.notHairstylist'));
            return;
          }
          userId = existing.id;
        } else {
          setError(t('staff.userNotFound'));
          return;
        }
      } else {
        // Will be handled by backend - staff creation with new user
        userId = email.trim(); // Backend handles user creation
      }

      await salonsApi.addStaffMember({
        salonId,
        userId,
        specialties: Array.from(specialties),
        isActive: true,
      });

      Alert.alert(t('common.actions.done'), t('staff.addedSuccess', { name: `${firstName.trim()} ${lastName.trim()}` }), [
        { text: t('common.actions.ok'), onPress: () => router.back() },
      ]);
    } catch (e: any) {
      const msg = e?.response?.data?.message;
      if (msg?.includes('existe deja') || e?.response?.status === 409) {
        setError(t('staff.alreadyMember'));
      } else if (e?.response?.status === 403) {
        setError(t('staff.notAuthorized'));
      } else {
        setError(msg || t('common.states.error'));
      }
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
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginStart: 16 }]}>{t('staff.addStaff')}</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.row}>
          <TextInput style={[styles.input, styles.half, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
            placeholder={t('common.fields.firstName') + ' *'} placeholderTextColor={colors.onSurfaceVariant} value={firstName} onChangeText={setFirstName} />
          <TextInput style={[styles.input, styles.half, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
            placeholder={t('common.fields.lastName') + ' *'} placeholderTextColor={colors.onSurfaceVariant} value={lastName} onChangeText={setLastName} />
        </View>

        <TextInput style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('common.fields.email') + ' *'} placeholderTextColor={colors.onSurfaceVariant} value={email} onChangeText={setEmail}
          keyboardType="email-address" autoCapitalize="none" />

        {/* Info box */}
        <View style={[styles.infoBox, { backgroundColor: colors.infoContainer }]}>
          <MaterialIcons name="info-outline" size={18} color={colors.onInfoContainer} />
          <Text style={[typo.bodySmall, { color: colors.onInfoContainer, marginStart: 8, flex: 1 }]}>
            {t('staff.autoCreateHint')}
          </Text>
        </View>

        {/* Specialties */}
        <Text style={[typo.labelLarge, { color: colors.onSurface, marginBottom: 8, marginTop: 8 }]}>{t('staff.specialties')} *</Text>
        <View style={styles.categoryGrid}>
          {categories.map((cat) => {
            const selected = specialties.has(cat.key);
            return (
              <TouchableOpacity
                key={cat.key}
                style={[styles.categoryCard, {
                  backgroundColor: selected ? colors.primaryContainer : colors.surface,
                  borderColor: selected ? colors.primary : colors.outlineVariant,
                }]}
                onPress={() => toggleSpecialty(cat.key)}
              >
                <Text style={{ fontSize: 20 }}>{cat.emoji}</Text>
                <Text style={[typo.labelSmall, { color: selected ? colors.onPrimaryContainer : colors.onSurfaceVariant, marginTop: 4 }]}>
                  {t(cat.labelKey)}
                </Text>
                {selected && <MaterialIcons name="check-circle" size={16} color={colors.primary} style={styles.checkIcon} />}
              </TouchableOpacity>
            );
          })}
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
              {t('staff.addStaff')}
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
  row: { flexDirection: 'row', gap: 12 },
  half: { flex: 1 },
  infoBox: { flexDirection: 'row', alignItems: 'center', padding: 12, borderRadius: 12, marginBottom: 12 },
  categoryGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  categoryCard: { width: '48%', padding: 12, borderRadius: 12, borderWidth: 1, alignItems: 'center', position: 'relative' },
  checkIcon: { position: 'absolute', top: 8, end: 8 },
  errorCard: { padding: 12, borderRadius: 12, marginBottom: 12 },
  submitBtn: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginTop: 8 },
});
