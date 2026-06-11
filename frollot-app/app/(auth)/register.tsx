import { useState } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet,
  ScrollView, KeyboardAvoidingView, Platform, ActivityIndicator,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { LinearGradient } from 'expo-linear-gradient';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { TextField, PasswordTextField, PrimaryButton } from '../../src/components/ui';
import { useTheme } from '../../src/theme';
import { UserType } from '../../src/types';

const ACCOUNT_TYPES = [
  { type: UserType.CLIENT, icon: 'account' as const, label: 'Client', sub: 'Réserver & suivre' },
  { type: UserType.HAIRSTYLIST, icon: 'content-cut' as const, label: 'Coiffeur', sub: 'Portfolio & agenda' },
  { type: UserType.SALON_OWNER, icon: 'storefront' as const, label: 'Salon', sub: 'Gérer mon salon' },
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
  const strengthLabels = ['', 'Faible', 'Moyen', 'Bon', 'Robuste'];
  const strengthColors = ['', colors.error, colors.warning, colors.info, colors.success];
  const canSubmit = firstName.trim() && lastName.trim() && isEmailValid && password.length >= 8 && passwordsMatch;

  const handleRegister = async () => {
    setRegisterError(null);
    if (!canSubmit) return;
    try {
      await register(email.trim(), password, firstName.trim(), lastName.trim(), userType);
      router.replace({ pathname: '/(auth)/email-verification', params: { email: email.trim() } });
    } catch (error: any) {
      setRegisterError(error?.response?.data?.message || 'Erreur lors de l\'inscription.');
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
            <MaterialCommunityIcons name="arrow-left" size={24} color="#FFFFFF" />{/* design-fixed — white arrow over hero */}
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
        <Text style={[s.overline, { color: colors.secondary }]}>Créez votre compte</Text>
        <Text style={[s.headline, { color: colors.onSurface }]}>Rejoignez-nous</Text>
        <Text style={[s.subtitle, { color: colors.onSurfaceVariant }]}>Quelques instants suffisent pour faire partie de la communauté beauté.</Text>

        {/* Account type cards */}
        <Text style={[s.fieldLabel, { color: colors.onSurface, marginTop: 24, marginBottom: 10 }]}>Je m'inscris en tant que</Text>
        <View style={s.typeGrid}>
          {ACCOUNT_TYPES.map((t) => {
            const on = userType === t.type;
            return (
              <TouchableOpacity
                key={t.type}
                style={[s.typeCard, {
                  backgroundColor: on ? colors.primaryContainer : colors.surface,
                  borderColor: on ? colors.primary : colors.outlineVariant,
                  borderWidth: on ? 2 : 1,
                }]}
                onPress={() => setUserType(t.type)}
              >
                {on && (
                  <View style={s.typeCheck}>
                    <MaterialCommunityIcons name="check-circle" size={16} color={colors.primary} />
                  </View>
                )}
                <View style={[s.typeIconCircle, { backgroundColor: on ? colors.primary : colors.surfaceContainerHigh }]}>
                  <MaterialCommunityIcons name={t.icon} size={22} color={on ? colors.onPrimary : colors.onSurfaceVariant} />
                </View>
                <Text style={[s.typeLabel, { color: on ? colors.onPrimaryContainer : colors.onSurface }]}>{t.label}</Text>
                <Text style={[s.typeSub, { color: on ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>{t.sub}</Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Fields */}
        <View style={s.fieldsSection}>
          <View style={s.nameRow}>
            <View style={{ flex: 1 }}>
              <TextField label="Prénom" icon="badge-account" value={firstName} onChangeText={setFirstName} placeholder="Camille" />
            </View>
            <View style={{ flex: 1 }}>
              <TextField label="Nom" value={lastName} onChangeText={setLastName} placeholder="Roussel" />
            </View>
          </View>

          <TextField
            label="Email"
            icon="email-outline"
            value={email}
            onChangeText={setEmail}
            placeholder="camille@email.com"
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
            trailingIcon={email.length > 3 && isEmailValid ? 'check-circle' : undefined}
          />

          <PasswordTextField
            label="Mot de passe"
            value={password}
            onChangeText={setPassword}
            placeholder="Minimum 8 caractères"
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
            label="Confirmer le mot de passe"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            placeholder="Retapez le mot de passe"
            error={confirmPassword.length > 0 && !passwordsMatch ? 'Les mots de passe ne correspondent pas' : undefined}
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
              <Text style={[s.submitText, { color: canSubmit ? colors.onPrimary : colors.onSurfaceVariant }]}>S'inscrire</Text>
              <MaterialCommunityIcons name="arrow-right" size={20} color={canSubmit ? colors.onPrimary : colors.onSurfaceVariant} />
            </>
          )}
        </TouchableOpacity>

        <Text style={[s.legal, { color: colors.onSurfaceVariant }]}>
          En créant un compte, vous acceptez nos{' '}
          <Text style={{ color: colors.primary, fontWeight: '600' }}>Conditions</Text> et notre{' '}
          <Text style={{ color: colors.primary, fontWeight: '600' }}>Politique de confidentialité</Text>.
        </Text>

        <View style={s.loginRow}>
          <Text style={[s.loginText, { color: colors.onSurfaceVariant }]}>Déjà un compte ?</Text>
          <TouchableOpacity onPress={() => router.back()}>
            <Text style={[s.loginLink, { color: colors.primary }]}>Se connecter</Text>
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
  heroPlaceholder: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 },
  heroActions: { position: 'absolute', top: 12, left: 8, right: 8, flexDirection: 'row' },
  heroBackBtn: {
    width: 44, height: 44,
    alignItems: 'center', justifyContent: 'center',
  },
  heroBrand: { position: 'absolute', bottom: 20, left: 24, flexDirection: 'row', alignItems: 'center', gap: 10 },
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
  typeCheck: { position: 'absolute', top: 6, right: 6 },
  typeIconCircle: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  typeLabel: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700', textAlign: 'center' },
  typeSub: { fontFamily: 'Manrope-Regular', fontSize: 10.5, lineHeight: 13, textAlign: 'center' },
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
