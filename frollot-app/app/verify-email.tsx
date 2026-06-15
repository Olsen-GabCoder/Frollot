import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { authApi } from '../src/api/auth';
import { PrimaryButton, OutlineButton } from '../src/components/ui';

export default function VerifyEmailScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { token } = useLocalSearchParams<{ token: string }>();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (!token) { setStatus('error'); setErrorMessage(t('auth.verifyEmail.invalidLink')); return; }
    let ignore = false;
    const verify = async () => {
      try {
        await authApi.completeRegistration(token);
        if (!ignore) setStatus('success');
      } catch (e: any) {
        if (!ignore) {
          setStatus('error');
          setErrorMessage(e?.response?.data?.message || t('auth.verifyEmail.expiredLink'));
        }
      }
    };
    verify();
    return () => { ignore = true; };
  }, [token]);

  if (status === 'loading') {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <ActivityIndicator size="large" color={colors.primary} />
        <Text style={[styles.loadingText, { color: colors.onSurfaceVariant }]}>{t('auth.verifyEmail.activating')}</Text>
      </View>
    );
  }

  if (status === 'error') {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <View style={[styles.iconCircle, { backgroundColor: colors.errorContainer }]}>
          <MaterialCommunityIcons name="alert-circle-outline" size={52} color={colors.error} />
        </View>
        <Text style={[styles.title, { color: colors.onBackground }]}>{t('auth.verifyEmail.failedTitle')}</Text>
        <Text style={[styles.desc, { color: colors.onSurfaceVariant }]}>{errorMessage}</Text>
        <OutlineButton full onPress={() => router.replace('/(auth)/login')} style={styles.btn}>
          {t('auth.twoFactor.backToLogin')}
        </OutlineButton>
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.iconCircle, { backgroundColor: colors.successContainer }]}>
        <MaterialCommunityIcons name="check-circle-outline" size={52} color={colors.success} />
      </View>
      <Text style={[styles.title, { color: colors.onBackground }]}>{t('auth.verifyEmail.successTitle')}</Text>
      <Text style={[styles.desc, { color: colors.onSurfaceVariant }]}>
        {t('auth.verifyEmail.successDesc')}
      </Text>
      <PrimaryButton icon="login" full onPress={() => router.replace('/(auth)/login')} style={styles.btn}>
        {t('auth.verifyEmail.loginButton')}
      </PrimaryButton>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  iconCircle: { width: 96, height: 96, borderRadius: 48, alignItems: 'center', justifyContent: 'center', marginBottom: 24 },
  title: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 30, fontWeight: '600', textAlign: 'center', marginBottom: 12 },
  desc: { fontFamily: 'Manrope-Regular', fontSize: 15, lineHeight: 22, textAlign: 'center', marginBottom: 32, paddingHorizontal: 16 },
  loadingText: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600', marginTop: 20 },
  btn: { width: '100%' },
});
