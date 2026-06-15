import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../src/theme';
import { authApi } from '../../src/api/auth';

export default function ForgotPasswordScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async () => {
    if (!email.trim()) {
      Alert.alert(t('common.states.error'), t('common.validation.emailRequired'));
      return;
    }
    setIsLoading(true);
    try {
      await authApi.forgotPassword({ email: email.trim() });
      Alert.alert(t('common.actions.done'), t('auth.resetPasswordSent'));
      router.back();
    } catch (error: any) {
      Alert.alert(t('common.states.error'), error?.response?.data?.message || t('common.states.error'));
    } finally {
      setIsLoading(false);
    }
  };

  const { colors, typography: typo } = theme;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <TouchableOpacity onPress={() => router.back()} style={styles.backBtn}>
        <Text style={[typo.labelLarge, { color: colors.primary }]}>{t('common.actions.back')}</Text>
      </TouchableOpacity>

      <Text style={[typo.headlineMedium, { color: colors.onBackground, marginBottom: 12 }]}>
        {t('auth.forgotPassword')}
      </Text>
      <Text style={[typo.bodyLarge, { color: colors.onSurfaceVariant, marginBottom: 32 }]}>
        {t('auth.resetPasswordSent')}
      </Text>

      <TextInput
        style={[styles.input, {
          backgroundColor: colors.surfaceContainerHigh,
          color: colors.onSurface,
          borderColor: colors.outlineVariant,
        }]}
        placeholder={t('common.fields.email')}
        placeholderTextColor={colors.onSurfaceVariant}
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
      />

      <TouchableOpacity
        style={[styles.button, { backgroundColor: colors.primary }]}
        onPress={handleSubmit}
        disabled={isLoading}
      >
        {isLoading ? (
          <ActivityIndicator color={colors.onPrimary} />
        ) : (
          <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>{t('auth.resetPassword')}</Text>
        )}
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, paddingTop: 60 },
  backBtn: { marginBottom: 24 },
  input: {
    height: 52,
    borderRadius: 12,
    paddingHorizontal: 16,
    marginBottom: 20,
    fontSize: 16,
    borderWidth: 1,
  },
  button: {
    height: 52,
    borderRadius: 28,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
