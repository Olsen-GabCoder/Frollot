import { useState } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet,
  ScrollView, KeyboardAvoidingView, Platform, ActivityIndicator, I18nManager,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { LinearGradient } from 'expo-linear-gradient';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { TextField, PasswordTextField, PrimaryButton } from '../../src/components/ui';
import { useTheme } from '../../src/theme';
import { UserType } from '../../src/types';

const ACCOUNT_TYPE_KEYS = [
  { type: UserType.CLIENT, icon: 'account' as const, labelKey: 'auth.userTypes.client', subKey: 'auth.register.accountSub.client' },
  { type: UserType.HAIRSTYLIST, icon: 'content-cut' as const, labelKey: 'auth.userTypes.hairstylist', subKey: 'auth.register.accountSub.hairstylist' },
  { type: UserType.SALON_OWNER, icon: 'storefront' as const, labelKey: 'auth.userTypes.salonOwner', subKey: 'auth.register.accountSub.salonOwner' },
];

export default function RegisterScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { register, isLoading } = useAuthStore();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [userType, setUserType] = useState<UserType>(UserType.CLIENT);
  const [registerError, setRegisterError] = useState<string | null>(null);

  const isEmailValid = email.includes('@') && email.includes('.');
  const passwordsMatch = password === confirmPassword;
  const passwordStrength = password.length === 0 ? 0 : password.length < 6 ? 1 : password.length < 8 ? 2 : password.length < 12 ? 3 : 4;
  const strengthLabels = ['', t('auth.register.strength.weak'), t('auth.register.strength.medium'), t('auth.register.strength.good'), t('auth.register.strength.strong')];
  const strengthColors = ['', colors.error, colors.warning, colors.info, colors.success];
  const canSubmit = firstName.trim() && lastName.trim() && isEmailValid && password.length >= 8 && passwordsMatch;

  const handleRegister = async () => {
    setRegisterError(null);
    if (!canSubmit) return;
    try {
      await register(email.trim(), password, firstName.trim(), lastName.trim(), userType);
      router.replace({ pathname: '/(auth)/email-verification', params: { email: email.trim() } });
    } catch (error: any) {
      setRegisterError(error?.response?.data?.message || t('auth.register.error'));
    }
  };

  return (
    <KeyboardAvoidingView style={[s.container, { backgroundColor: colors.surface }]} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      {/* Hero compact */}
      <View style={s.hero}>
        <View style={[s.heroPlaceholder, { backgroundColor: colors.primary }]} />
        {/* design-fixed — editorial hero gradient */}
        <LinearGradient
          colors={['rgba(40,23,51,0.5)', 'rgba(40,23,51,0.18)', colors.surface]}
          locations={[0, 0.55, 1]}
          style={StyleSheet.absoluteFill}
        />
        <View style={s.heroActions}>
          <TouchableOpacity style={s.heroBackBtn} onPress={() => router.back()}>
            <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color="#FFFFFF" />{/* design-fixed — white arrow over hero */}
          </TouchableOpacity>
        </View>
        <View style={s.heroBrand}>
          <View style={s.brandLogo}>
            <Text style={s.brandF}>F</Text>
          </View>
          <Text style={s.brandName}>Frollot</Text>
        </View>
      </View>

      <ScrollView style={s.formScroll} contentContainerStyle={s.formContent} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        <Text style={[s.overline, { color: colors.secondary }]}>{t('auth.register.overline')}</Text>
        <Text style={[s.headline, { color: colors.onSurface }]}>{t('auth.register.headline')}</Text>
        <Text style={[s.subtitle, { color: colors.onSurfaceVariant }]}>{t('auth.register.subtitle')}</Text>

        {/* Account type cards */}
        <Text style={[s.fieldLabel, { color: colors.onSurface, marginTop: 24, marginBottom: 10 }]}>{t('auth.register.accountTypeLabel')}</Text>
        <View style={s.typeGrid}>
          {ACCOUNT_TYPE_KEYS.map((item) => {
            const on = userType === item.type;
            return (
              <TouchableOpacity
                key={item.type}
                style={[s.typeCard, {
                  backgroundColor: on ? colors.primaryContainer : colors.surface,
                  borderColor: on ? colors.primary : colors.outlineVariant,
                  borderWidth: on ? 2 : 1,
                }]}
                onPress={() => setUserType(item.type)}
              >
                {on && (
                  <View style={s.typeCheck}>
                    <MaterialCommunityIcons name="check-circle" size={16} color={colors.primary} />
                  </View>
                )}
                <View style={[s.typeIconCircle, { backgroundColor: on ? colors.primary : colors.surfaceContainerHigh }]}>
                  <MaterialCommunityIcons name={item.icon} size={22} color={on ? colors.onPrimary : colors.onSurfaceVariant} />
                </View>
                <Text style={[s.typeLabel, { color: on ? colors.onPrimaryContainer : colors.onSurface }]}>{t(item.labelKey)}</Text>
                <Text style={[s.typeSub, { color: on ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>{t(item.subKey)}</Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Fields */}
        <View style={s.fieldsSection}>
          <View style={s.nameRow}>
            <View style={{ flex: 1 }}>
              <TextField label={t('common.fields.firstName')} icon="badge-account" value={firstName} onChangeText={setFirstName} placeholder={t('auth.register.firstNamePlaceholder')} />
            </View>
            <View style={{ flex: 1 }}>
              <TextField label={t('common.fields.lastName')} value={lastName} onChangeText={setLastName} placeholder={t('auth.register.lastNamePlaceholder')} />
            </View>
          </View>

          <TextField
            label={t('common.fields.email')}
            icon="email-outline"
            value={email}
            onChangeText={setEmail}
            placeholder={t('auth.register.emailPlaceholder')}
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
            trailingIcon={email.length > 3 && isEmailValid ? 'check-circle' : undefined}
          />

          <PasswordTextField
            label={t('common.fields.password')}
            value={password}
            onChangeText={setPassword}
            placeholder={t('auth.register.passwordPlaceholder')}
            autoComplete="new-password"
          />
          {password.length > 0 && (
            <View style={s.strengthRow}>
              <View style={s.strengthBars}>
                {[0, 1, 2, 3].map(i => (
                  <View key={i} style={[s.strengthBar, { backgroundColor: i < passwordStrength ? strengthColors[passwordStrength] : colors.outlineVariant }]} />
                ))}
              </View>
              <Text style={[s.strengthLabel, { color: strengthColors[passwordStrength] }]}>{strengthLabels[passwordStrength]}</Text>
            </View>
          )}

          <PasswordTextField
            label={t('common.fields.confirmPassword')}
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            placeholder={t('auth.register.confirmPlaceholder')}
            error={confirmPassword.length > 0 && !passwordsMatch ? t('common.validation.passwordsDoNotMatch') : undefined}
          />
        </View>

        {registerError && (
          <View style={[s.errorCard, { backgroundColor: colors.errorContainer }]}>
            <MaterialCommunityIcons name="alert-circle" size={18} color={colors.onErrorContainer} />
            <Text style={[s.errorText, { color: colors.onErrorContainer }]}>{registerError}</Text>
          </View>
        )}

        <TouchableOpacity
          style={[s.submitBtn, { backgroundColor: canSubmit ? colors.primary : colors.surfaceContainerHigh }]}
          onPress={handleRegister}
          disabled={isLoading || !canSubmit}
        >
          {isLoading ? (
            <ActivityIndicator color={colors.onPrimary} />
          ) : (
            <>
              <Text style={[s.submitText, { color: canSubmit ? colors.onPrimary : colors.onSurfaceVariant }]}>{t('auth.register.signUp')}</Text>
              <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-left' : 'arrow-right'} size={20} color={canSubmit ? colors.onPrimary : colors.onSurfaceVariant} />
            </>
          )}
        </TouchableOpacity>

        <Text style={[s.legal, { color: colors.onSurfaceVariant }]}>
          {t('auth.legal.creatingAccept')}
          <Text style={{ color: colors.primary, fontWeight: '600' }}>{t('auth.legal.terms')}</Text>
          {t('auth.legal.conjunction')}
          <Text style={{ color: colors.primary, fontWeight: '600' }}>{t('auth.legal.privacy')}</Text>.
        </Text>

        <View style={s.loginRow}>
          <Text style={[s.loginText, { color: colors.onSurfaceVariant }]}>{t('auth.alreadyHaveAccount')}</Text>
          <TouchableOpacity onPress={() => router.back()}>
            <Text style={[s.loginLink, { color: colors.primary }]}>{t('auth.loginButton')}</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  // Hero compact
  hero: { position: 'relative', height: 150, flexShrink: 0 },
  heroPlaceholder: { position: 'absolute', top: 0, start: 0, end: 0, bottom: 0 },
  heroActions: { position: 'absolute', top: 12, start: 8, end: 8, flexDirection: 'row' },
  heroBackBtn: {
    width: 44, height: 44,
    alignItems: 'center', justifyContent: 'center',
  },
  heroBrand: { position: 'absolute', bottom: 20, start: 24, flexDirection: 'row', alignItems: 'center', gap: 10 },
  brandLogo: {
    width: 40, height: 40, borderRadius: 12,
    backgroundColor: 'rgba(255,255,255,0.16)', // design-fixed
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.35)', // design-fixed
    alignItems: 'center', justifyContent: 'center',
  },
  brandF: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 26, fontWeight: '600', color: '#FFFFFF' }, // design-fixed
  brandName: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', color: '#FFFFFF', letterSpacing: 0.5 }, // design-fixed
  // Form
  formScroll: { flex: 1 },
  formContent: { padding: 24, paddingTop: 18, paddingBottom: 40 },
  overline: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700', letterSpacing: 2, textTransform: 'uppercase', marginBottom: 6 },
  headline: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 40, fontWeight: '600', lineHeight: 39, letterSpacing: -0.5 },
  subtitle: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 20, marginTop: 10 },
  // Type cards
  typeGrid: { flexDirection: 'row', gap: 10 },
  typeCard: {
    flex: 1, alignItems: 'center', gap: 6,
    paddingTop: 16, paddingBottom: 12, paddingHorizontal: 6,
    borderRadius: 12, position: 'relative',
  },
  typeCheck: { position: 'absolute', top: 6, end: 6 },
  typeIconCircle: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  typeLabel: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700', textAlign: 'center' },
  typeSub: { fontFamily: 'Manrope-Regular', fontSize: 10.5, lineHeight: 13, textAlign: 'center' },
  // Field label
  fieldLabel: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  // Fields
  fieldsSection: { gap: 14, marginTop: 22 },
  nameRow: { flexDirection: 'row', gap: 12 },
  // Password strength
  strengthRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 8 },
  strengthBars: { flexDirection: 'row', gap: 4, flex: 1 },
  strengthBar: { flex: 1, height: 4, borderRadius: 2 },
  strengthLabel: { fontFamily: 'Manrope-Bold', fontSize: 12, fontWeight: '700' },
  // Error
  errorCard: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14, borderRadius: 12, marginTop: 16 },
  errorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  // Submit
  submitBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    height: 52, borderRadius: 28, marginTop: 22,
    shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.12, shadowRadius: 6, elevation: 2, // design-fixed
  },
  submitText: { fontFamily: 'Manrope-Bold', fontSize: 16, fontWeight: '700' },
  // Footer
  legal: { fontFamily: 'Manrope-Regular', fontSize: 12, lineHeight: 18, textAlign: 'center', marginTop: 16 },
  loginRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, marginTop: 20 },
  loginText: { fontFamily: 'Manrope-Regular', fontSize: 14 },
  loginLink: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
