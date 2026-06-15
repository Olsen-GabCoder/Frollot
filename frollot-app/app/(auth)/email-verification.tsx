import { View, Text, TouchableOpacity, StyleSheet, I18nManager } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';

export default function EmailVerificationScreen() {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { email } = useLocalSearchParams<{ email: string }>();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.iconCircle, { backgroundColor: colors.primaryContainer }]}>
        <MaterialCommunityIcons name="email-check-outline" size={48} color={colors.primary} />
      </View>

      <Text style={[styles.title, { color: colors.onBackground }]}>
        {t('auth.verifyEmailTitle')}
      </Text>

      <Text style={[styles.desc, { color: colors.onSurfaceVariant }]}>
        {t('auth.verifyEmailHint', { email: email || t('auth.verifyEmail.yourAddress') })}
      </Text>

      <View style={[styles.infoCard, { backgroundColor: colors.surfaceContainerHigh }]}>
        <MaterialCommunityIcons name="information-outline" size={18} color={colors.onSurfaceVariant} />
        <Text style={[styles.infoText, { color: colors.onSurfaceVariant }]}>
          {t('auth.verifyEmail.spamHint')}
        </Text>
      </View>

      <TouchableOpacity
        style={[styles.loginBtn, { borderColor: colors.primary }]}
        onPress={() => router.replace('/(auth)/login')}
      >
        <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={20} color={colors.primary} />
        <Text style={[styles.loginBtnText, { color: colors.primary }]}>{t('auth.twoFactor.backToLogin')}</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, paddingTop: 100, alignItems: 'center' },
  iconCircle: { width: 88, height: 88, borderRadius: 44, alignItems: 'center', justifyContent: 'center', marginBottom: 24 },
  title: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 28, fontWeight: '600', textAlign: 'center', marginBottom: 12 },
  desc: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 21, textAlign: 'center', marginBottom: 24, paddingHorizontal: 12 },
  infoCard: { flexDirection: 'row', alignItems: 'flex-start', gap: 10, padding: 14, borderRadius: 12, marginBottom: 32, width: '100%' },
  infoText: { fontFamily: 'Manrope-Regular', fontSize: 13, lineHeight: 18, flex: 1 },
  loginBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    height: 52, borderRadius: 28, borderWidth: 1, width: '100%',
  },
  loginBtnText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },
});
