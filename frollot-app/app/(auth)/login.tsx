import { useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { TextField, PasswordTextField, PrimaryButton, OutlineButton } from '../../src/components/ui';
import { useTheme } from '../../src/theme';

export default function LoginScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { login, isLoading } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const [loginError, setLoginError] = useState<string | null>(null);
  const [loginSuccess, setLoginSuccess] = useState(false);

  const handleLogin = async () => {
    setLoginError(null);
    if (!email.trim()) { setLoginError(t('common.validation.emailRequired')); return; }
    if (!password.trim()) { setLoginError(t('common.validation.passwordRequired')); return; }
    try {
      const response = await login(email.trim(), password);
      // S9d-2 — compte 2FA-actif : pas de session (le store n'a RIEN stocké), le jeton
      // de défi vit en mémoire dans authStore. On passe à la saisie du code.
      if (response.requiresTwoFactor) {
        router.push('/(auth)/two-factor');
        return;
      }
      setLoginSuccess(true);
      setTimeout(() => router.replace('/(tabs)'), 2000);
    } catch (error: any) {
      const status = error?.response?.status;
      const message = error?.response?.data?.message || '';
      if (status === 403 && message.includes('vérifiée')) {
        router.push({ pathname: '/(auth)/email-verification', params: { email: email.trim() } });
      } else if (status === 403) {
        setLoginError(t('auth.accountDisabled'));
      } else if (status === 429) {
        setLoginError(t('auth.tooManyAttempts'));
      } else {
        setLoginError(t('auth.invalidCredentialsError'));
      }
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.surface }]}>
      {/* Hero editorial */}
      <View style={styles.hero}>
        <View style={[styles.heroPlaceholder, { backgroundColor: colors.primary }]} />
        <LinearGradient
          // design-fixed — editorial hero gradient overlay, not theme-dependent
          colors={['rgba(40,23,51,0.42)', 'rgba(40,23,51,0.08)', 'rgba(251,247,249,0)', '#FFFFFF']}
          locations={[0, 0.45, 0.7, 1]}
          style={StyleSheet.absoluteFill}
        />
        {/* Brand mark */}
        <View style={styles.brandContainer}>
          <View style={styles.brandLogo}>
            <Text style={styles.brandF}>F</Text>
          </View>
          <Text style={styles.brandName}>Frollot</Text>
        </View>
      </View>

      {/* Form */}
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.formWrapper}
      >
        <ScrollView
          style={styles.formScroll}
          contentContainerStyle={styles.formContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <Text style={[styles.overline, { color: colors.secondary }]}>{t('auth.welcomeBack')}</Text>
          <Text style={[styles.headline, { color: colors.onBackground }]}>{t('auth.welcomeTitle')}</Text>
          <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>
            {t('auth.loginSubtitle')}
          </Text>

          <View style={styles.fields}>
            <TextField
              label={t('common.fields.email')}
              icon="email-outline"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              autoComplete="email"
            />
            <PasswordTextField
              label={t('common.fields.password')}
              value={password}
              onChangeText={setPassword}
              autoComplete="password"
            />
          </View>

          <TouchableOpacity
            onPress={() => router.push('/(auth)/forgot-password')}
            style={styles.forgotLink}
          >
            <Text style={[styles.forgotText, { color: colors.primary }]}>{t('auth.forgotPassword')}</Text>
          </TouchableOpacity>

          {loginError && (
            <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
              <MaterialCommunityIcons name="alert-circle" size={18} color={colors.onErrorContainer} />
              <Text style={[styles.errorText, { color: colors.onErrorContainer }]}>{loginError}</Text>
            </View>
          )}

          {loginSuccess && (
            <View style={[styles.successCard, { backgroundColor: colors.successContainer }]}>
              <MaterialCommunityIcons name="check-circle" size={18} color={colors.onSuccessContainer} />
              <Text style={[styles.errorText, { color: colors.onSuccessContainer }]}>{t('auth.loginSuccessRedirect')}</Text>
            </View>
          )}

          <PrimaryButton
            icon="login"
            full
            onPress={handleLogin}
            loading={isLoading}
            style={styles.loginBtn}
          >
            {t('auth.loginButton')}
          </PrimaryButton>

          {/* Separator */}
          <View style={styles.divider}>
            <View style={[styles.dividerLine, { backgroundColor: colors.outlineVariant }]} />
            <Text style={[styles.dividerText, { color: colors.onSurfaceVariant }]}>{t('common.words.or')}</Text>
            <View style={[styles.dividerLine, { backgroundColor: colors.outlineVariant }]} />
          </View>

          <OutlineButton full onPress={() => router.push('/(auth)/register')}>
            {t('auth.registerButton')}
          </OutlineButton>

          <Text style={[styles.legal, { color: colors.onSurfaceVariant }]}>
            {t('auth.legal.continuingAccept')}
            <Text style={[styles.legalLink, { color: colors.primary }]}>{t('auth.legal.terms')}</Text>
            {t('auth.legal.conjunction')}
            <Text style={[styles.legalLink, { color: colors.primary }]}>{t('auth.legal.privacy')}</Text>.
          </Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  // --- Hero ---
  hero: {
    height: 320,
    position: 'relative',
  },
  heroPlaceholder: {
    position: 'absolute', top: 0, start: 0, end: 0, bottom: 0,
  },
  brandContainer: {
    position: 'absolute',
    top: 28,
    start: 24,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  brandLogo: {
    width: 48,
    height: 48,
    borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.16)', // design-fixed
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.35)', // design-fixed
    alignItems: 'center',
    justifyContent: 'center',
  },
  brandF: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 32,
    fontWeight: '600',
    color: '#FFFFFF', // design-fixed — white text over hero photo
  },
  brandName: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 26,
    fontWeight: '600',
    color: '#FFFFFF', // design-fixed — white text over hero photo
    letterSpacing: 0.5,
  },
  // --- Form ---
  formWrapper: {
    flex: 1,
  },
  formScroll: {
    flex: 1,
  },
  formContent: {
    paddingTop: 4,
    paddingHorizontal: 24,
    paddingBottom: 24,
  },
  overline: {
    fontFamily: 'Manrope-Bold',
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 2,
    textTransform: 'uppercase',
    marginBottom: 6,
  },
  headline: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 44,
    fontWeight: '600',
    lineHeight: 43,
    letterSpacing: -0.5,
  },
  subtitle: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    lineHeight: 20,
    letterSpacing: 0.2,
    marginTop: 10,
  },
  fields: {
    gap: 14,
    marginTop: 24,
  },
  forgotLink: {
    alignSelf: 'flex-end',
    marginTop: 12,
    padding: 4,
  },
  forgotText: {
    fontFamily: 'Manrope-Bold',
    fontSize: 13.5,
    fontWeight: '700',
  },
  errorCard: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14, borderRadius: 12, marginBottom: 12 },
  successCard: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14, borderRadius: 12, marginBottom: 12 },
  errorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  loginBtn: {
    marginTop: 14,
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.12,
    shadowRadius: 6,
    elevation: 2,
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    marginVertical: 22,
  },
  dividerLine: {
    flex: 1,
    height: 1,
  },
  dividerText: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 12,
    fontWeight: '600',
    letterSpacing: 1,
  },
  legal: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    lineHeight: 18,
    textAlign: 'center',
    marginTop: 'auto' as any,
    paddingTop: 20,
  },
  legalLink: {
    fontWeight: '600',
  },
});
