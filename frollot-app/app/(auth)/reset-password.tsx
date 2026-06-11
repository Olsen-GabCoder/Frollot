import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../src/theme';
import { authApi } from '../../src/api/auth';

export default function ResetPasswordScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { token } = useLocalSearchParams<{ token: string }>();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async () => {
    if (newPassword.length < 8) {
      Alert.alert(t('common.error'), t('auth.passwordTooShort'));
      return;
    }
    if (newPassword !== confirmPassword) {
      Alert.alert(t('common.error'), t('auth.passwordsDoNotMatch'));
      return;
    }
    setIsLoading(true);
    try {
      await authApi.resetPassword({ token: token || '', newPassword });
      Alert.alert(t('common.done'), t('auth.resetPasswordSuccess'));
      router.replace('/(auth)/login');
    } catch (error: any) {
      Alert.alert(t('common.error'), error?.response?.data?.message || t('common.error'));
    } finally {
      setIsLoading(false);
    }
  };

  const { colors, typography: typo } = theme;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <Text style={[typo.headlineMedium, { color: colors.onBackground, marginBottom: 32 }]}>
        {t('auth.resetPassword')}
      </Text>

      <TextInput
        style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
        placeholder={t('auth.newPassword')}
        placeholderTextColor={colors.onSurfaceVariant}
        value={newPassword}
        onChangeText={setNewPassword}
        secureTextEntry
      />
      <TextInput
        style={[styles.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
        placeholder={t('auth.confirmPassword')}
        placeholderTextColor={colors.onSurfaceVariant}
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        secureTextEntry
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
  input: { height: 52, borderRadius: 12, paddingHorizontal: 16, marginBottom: 12, fontSize: 16, borderWidth: 1 },
  button: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginTop: 8 },
});
