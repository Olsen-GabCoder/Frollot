import { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  I18nManager,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { authApi } from '../../src/api/auth';
import { TextField, PasswordTextField } from '../../src/components/ui/TextField';
import { PrimaryButton, TextButton } from '../../src/components/ui/Button';

type Step = 'request' | 'confirm' | 'success';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function ChangeEmailScreen() {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { user, setUser } = useAuthStore();

  const [step, setStep] = useState<Step>('request');
  const [newEmail, setNewEmail] = useState('');
  // Gardé en state LOCAL le temps du flux uniquement (jamais persisté, jamais dans un store)
  // pour permettre « Renvoyer le code » = rappel de changeEmail (le backend écrase l'ancien token).
  const [password, setPassword] = useState('');
  const [token, setToken] = useState('');
  const [finalEmail, setFinalEmail] = useState('');

  const [emailFieldError, setEmailFieldError] = useState('');
  const [serverError, setServerError] = useState('');
  const [resendInfo, setResendInfo] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);

  const currentEmail = user?.email ?? '';

  const leaveScreen = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  // Retour contextuel : à l'étape OTP, revenir au formulaire (sans perdre la saisie) ; sinon quitter.
  const goBack = () => {
    if (step === 'confirm') {
      setServerError('');
      setResendInfo('');
      setStep('request');
    } else {
      leaveScreen();
    }
  };

  const validateRequest = (): boolean => {
    const candidate = newEmail.trim();
    if (!candidate) {
      setEmailFieldError(t('settings.email.newRequiredError'));
      return false;
    }
    if (!EMAIL_REGEX.test(candidate)) {
      setEmailFieldError(t('common.validation.emailInvalid'));
      return false;
    }
    if (candidate.toLowerCase() === currentEmail.toLowerCase()) {
      setEmailFieldError(t('settings.email.sameAsCurrentError'));
      return false;
    }
    setEmailFieldError('');
    return true;
  };

  const handleRequest = async () => {
    setServerError('');
    if (!validateRequest() || !password) {
      if (!password) setServerError(t('common.validation.passwordRequired'));
      return;
    }
    setSubmitting(true);
    try {
      await authApi.changeEmail({ newEmail: newEmail.trim(), password });
      setToken('');
      setResendInfo('');
      setStep('confirm');
    } catch (error: any) {
      // 400 mot de passe incorrect / email invalide, 409 email déjà utilisé — message backend tel quel
      setServerError(error?.response?.data?.message || t('settings.email.sendError'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async () => {
    setServerError('');
    setResendInfo('');
    setResending(true);
    try {
      // Rappelle la demande : le backend écrase pendingEmail + token (testé S4-backend).
      await authApi.changeEmail({ newEmail: newEmail.trim(), password });
      setToken('');
      setResendInfo(t('settings.email.resendSuccess', { email: newEmail.trim() }));
    } catch (error: any) {
      setServerError(error?.response?.data?.message || t('settings.email.resendError'));
    } finally {
      setResending(false);
    }
  };

  const handleConfirm = async () => {
    setServerError('');
    setResendInfo('');
    const code = token.trim();
    if (code.length !== 6) {
      setServerError(t('settings.email.codeLengthError'));
      return;
    }
    setSubmitting(true);
    try {
      const resp = await authApi.confirmEmailChange(code);
      const updatedEmail = resp.newEmail ?? newEmail.trim();
      setFinalEmail(updatedEmail);
      // L'app ne doit plus afficher l'ancien email nulle part.
      if (user) setUser({ ...user, email: updatedEmail });
      setPassword(''); // hygiène : le mot de passe n'a plus de raison de vivre en state
      setToken('');
      setStep('success');
    } catch (error: any) {
      // 400 code invalide / expiré — message backend tel quel
      setServerError(error?.response?.data?.message || t('settings.email.codeInvalidError'));
    } finally {
      setSubmitting(false);
    }
  };

  const ErrorCard = serverError ? (
    <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
      <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
      <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
        {serverError}
      </Text>
    </View>
  ) : null;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={goBack}
        >
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {step === 'request' && (
            <>
              <Text style={[typo.overline, { color: colors.secondary }]}>{t('settings.sections.account')}</Text>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
                {t('settings.changeEmail')}
              </Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
                {t('settings.email.intro')}
              </Text>

              <View style={[styles.formCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
                {/* Email actuel — lecture seule */}
                <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant }]}>{t('settings.email.currentLabel')}</Text>
                <View style={[styles.currentEmailBox, { backgroundColor: colors.surfaceContainerHigh }]}>
                  <MaterialIcons name="alternate-email" size={18} color={colors.onSurfaceVariant} />
                  <Text style={[typo.bodyMedium, styles.currentEmailText, { color: colors.onSurface }]} numberOfLines={1}>
                    {currentEmail}
                  </Text>
                  <MaterialIcons name="lock-outline" size={16} color={colors.onSurfaceVariant} />
                </View>

                <View style={styles.fieldGap}>
                  <TextField
                    label={t('settings.email.newLabel')}
                    icon="email-outline"
                    value={newEmail}
                    onChangeText={(v) => { setNewEmail(v); if (emailFieldError) setEmailFieldError(''); }}
                    error={emailFieldError || undefined}
                    keyboardType="email-address"
                    autoCapitalize="none"
                    autoCorrect={false}
                    placeholder={t('settings.email.newPlaceholder')}
                  />
                </View>

                <View style={styles.fieldGap}>
                  <PasswordTextField
                    label={t('common.fields.password')}
                    value={password}
                    onChangeText={setPassword}
                    placeholder={t('settings.passwordConfirmPlaceholder')}
                  />
                </View>

                {ErrorCard}

                <View style={styles.fieldGap}>
                  <PrimaryButton
                    full
                    icon="send"
                    loading={submitting}
                    disabled={!newEmail.trim() || !password}
                    onPress={handleRequest}
                  >
                    {t('settings.email.sendCodeButton')}
                  </PrimaryButton>
                </View>
              </View>
            </>
          )}

          {step === 'confirm' && (
            <>
              <Text style={[typo.overline, { color: colors.secondary }]}>{t('settings.sections.account')}</Text>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
                {t('settings.email.codeTitle')}
              </Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
                {t('settings.email.codeSentPrefix')}{' '}
                <Text style={{ color: colors.onBackground, fontFamily: 'Manrope-Bold' }}>
                  {newEmail.trim()}
                </Text>
                .
              </Text>

              {/* Honnêteté sur l'état : rien n'a encore changé */}
              <View style={[styles.messageCard, { backgroundColor: colors.tertiaryContainer }]}>
                <MaterialIcons name="info-outline" size={20} color={colors.onTertiaryContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onTertiaryContainer }]}>
                  {t('settings.email.pendingHint', { email: currentEmail })}
                </Text>
              </View>

              <View style={[styles.formCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
                <Text style={[typo.labelMedium, { color: colors.onSurfaceVariant, textAlign: 'center' }]}>
                  {t('settings.email.codeLabel')}
                </Text>
                <TextInput
                  style={[styles.otpInput, {
                    color: colors.onSurface,
                    borderColor: serverError ? colors.error : colors.outline,
                    backgroundColor: colors.surfaceContainerHigh,
                  }]}
                  value={token}
                  onChangeText={(v) => { setToken(v.replace(/[^0-9]/g, '')); if (serverError) setServerError(''); }}
                  keyboardType="number-pad"
                  maxLength={6}
                  placeholder="000000"
                  placeholderTextColor={colors.onSurfaceVariant}
                  autoFocus
                />

                {ErrorCard}

                {resendInfo ? (
                  <View style={[styles.messageCard, { backgroundColor: colors.secondaryContainer }]}>
                    <MaterialIcons name="mark-email-read" size={20} color={colors.onSecondaryContainer} />
                    <Text style={[typo.bodySmall, styles.messageText, { color: colors.onSecondaryContainer }]}>
                      {resendInfo}
                    </Text>
                  </View>
                ) : null}

                <View style={styles.fieldGap}>
                  <PrimaryButton
                    full
                    icon="check"
                    loading={submitting}
                    disabled={token.trim().length !== 6}
                    onPress={handleConfirm}
                  >
                    {t('common.actions.confirm')}
                  </PrimaryButton>
                </View>

                <View style={styles.confirmActions}>
                  <TextButton loading={resending} onPress={handleResend}>
                    {t('settings.email.resendButton')}
                  </TextButton>
                  <TextButton onPress={goBack}>
                    {t('settings.email.editAddressButton')}
                  </TextButton>
                </View>
              </View>
            </>
          )}

          {step === 'success' && (
            <View style={styles.successWrap}>
              <View style={[styles.successCircle, { backgroundColor: colors.primaryContainer }]}>
                <MaterialIcons name="check-circle" size={48} color={colors.primary} />
              </View>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, textAlign: 'center', marginTop: 20 }]}>
                {t('settings.email.successTitle')}
              </Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 8 }]}>
                {t('settings.email.successPrefix')}{' '}
                <Text style={{ color: colors.onBackground, fontFamily: 'Manrope-Bold' }}>{finalEmail}</Text>{' '}
                {t('settings.email.successSuffix')}
              </Text>
              <View style={styles.successBtn}>
                <PrimaryButton full icon="arrow-left" onPress={leaveScreen}>
                  {t('settings.backToSettings')}
                </PrimaryButton>
              </View>
            </View>
          )}
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  flex: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 48 },
  formCard: { borderRadius: 20, borderWidth: 1, padding: 20, marginTop: 24 },
  currentEmailBox: {
    flexDirection: 'row', alignItems: 'center', gap: 10,
    borderRadius: 8, paddingHorizontal: 16, height: 52, marginTop: 6,
  },
  currentEmailText: { flex: 1 },
  fieldGap: { marginTop: 18 },
  messageCard: {
    flexDirection: 'row', alignItems: 'flex-start', gap: 10,
    borderRadius: 14, padding: 14, marginTop: 18,
  },
  messageText: { flex: 1 },
  otpInput: {
    fontFamily: 'Manrope-Bold',
    fontSize: 30,
    letterSpacing: 12,
    textAlign: 'center',
    borderWidth: 1,
    borderRadius: 14,
    height: 64,
    marginTop: 10,
    ...Platform.select({
      web: { outlineStyle: 'none', outlineWidth: 0 } as any,
    }),
  },
  confirmActions: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 8,
  },
  successWrap: { alignItems: 'center', paddingTop: 64, paddingHorizontal: 8 },
  successCircle: { width: 96, height: 96, borderRadius: 48, alignItems: 'center', justifyContent: 'center' },
  successBtn: { width: '100%', marginTop: 32 },
});
