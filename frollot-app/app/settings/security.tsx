/*
 * S8 — Sécurité (famille C, écran 11) + S9d-1 — gestion 2FA TOTP
 *
 * Parité KMP (SecuritySettingsScreen) + suppression de compte (décision utilisateur,
 * reportée depuis S1). Périmètre tranché au diagnostic famille C :
 *  - Changement de téléphone : écran séparé, REPORTÉ (OTP SMS requis) — chantier futur.
 *
 * APIs réelles (B25b, vérifiées curl 2026-06-12) :
 *  - PUT    /api/users/me/password        (UserController:732) — révoque TOUS les refresh
 *    tokens au succès (:753) -> reconnexion forcée gérée ici (modal + logout + login).
 *  - GET    /api/users/me/sessions        (:780) — header X-Refresh-Token pour isCurrent.
 *  - DELETE /api/users/me/sessions/{id}   (:807).
 *  - DELETE /api/users/me/sessions        (:842) — X-Refresh-Token OBLIGATOIRE sinon la
 *    session courante est aussi révoquée (géré dans authApi.revokeAllOtherSessions).
 *  - DELETE /api/users/me                 (:1034) — exige confirmDeletion=true + mot de
 *    passe ; hard delete (vérifié SQL). Double confirmation côté UI.
 *
 * 2FA TOTP (S9d-1, endpoints TwoFactorController vérifiés curl S9a-S9c) :
 *  - GET    /api/users/me/2fa/status                          -> { enabled }
 *  - POST   /api/users/me/2fa/setup                           -> { secret, otpauthUri }
 *  - POST   /api/users/me/2fa/confirm {code}                  -> { success, message, recoveryCodes[10] }
 *  - DELETE /api/users/me/2fa {password, code}                -> { success, message }
 *  - POST   /api/users/me/2fa/recovery-codes/regenerate {password, code} -> recoveryCodes[10]
 *  >>> S9d-1 = gestion uniquement. Le DÉFI AU LOGIN n'est PAS encore branché côté RN
 *  (S9d-2) : un compte 2FA-activé ici se connecte encore sans défi pour l'instant. <<<
 */
import { useCallback, useEffect, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Modal,
  Pressable,
  ActivityIndicator,
  I18nManager,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import QRCode from 'react-native-qrcode-svg';
import * as Clipboard from 'expo-clipboard';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { authApi } from '../../src/api/auth';
import { SessionInfo, TwoFactorSetupResponse } from '../../src/types';
import { TextField, PasswordTextField } from '../../src/components/ui/TextField';
import { PrimaryButton } from '../../src/components/ui/Button';
import { formatDateTime } from '../../src/utils/formatDate';
import { LoadingState, ErrorState, EmptyState } from '../../src/components/lists';

const MIN_PASSWORD_LENGTH = 8; // règle backend : @Size(min = 8) sur newPassword (SecurityDto.kt:18)

function formatSessionDate(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return formatDateTime(d);
}

function deviceIcon(deviceType?: string): keyof typeof MaterialIcons.glyphMap {
  switch (deviceType) {
    case 'mobile': return 'smartphone';
    case 'tablet': return 'tablet';
    case 'desktop': return 'computer';
    default: return 'devices';
  }
}

function deviceLabel(s: SessionInfo, t: (key: string) => string): string {
  if (s.deviceName) return s.deviceName;
  const parts = [s.browser, s.operatingSystem].filter(Boolean);
  if (parts.length) return parts.join(' · ');
  switch (s.deviceType) {
    case 'mobile': return t('security.sessions.deviceMobile');
    case 'tablet': return t('security.sessions.deviceTablet');
    case 'desktop': return t('security.sessions.deviceDesktop');
    default: return t('security.sessions.deviceUnknown');
  }
}

/**
 * Affichage des 10 codes de récupération — UNIQUE composant, réutilisé pour la
 * confirmation d'activation ET la régénération (zéro duplication).
 *
 * GARDE NON CONTOURNABLE : les codes ne sont montrés qu'une seule fois (stockés
 * hachés côté backend). La fermeture exige le clic explicite « J'ai enregistré mes
 * codes » ; pas de fermeture par l'overlay ni par le bouton retour Android.
 */
function RecoveryCodesModal({
  codes,
  regenerated,
  onClose,
}: {
  codes: string[] | null;
  regenerated: boolean;
  onClose: () => void;
}) {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const [acknowledged, setAcknowledged] = useState(false);
  const [copied, setCopied] = useState(false);

  // Réarmer la garde à chaque nouveau lot de codes
  useEffect(() => {
    setAcknowledged(false);
    setCopied(false);
  }, [codes]);

  const handleCopy = async () => {
    if (!codes) return;
    await Clipboard.setStringAsync(codes.join('\n'));
    setCopied(true);
  };

  const handleClose = () => {
    if (!acknowledged) return; // garde : pas de fermeture silencieuse
    onClose();
  };

  return (
    <Modal visible={codes !== null} transparent animationType="fade" onRequestClose={handleClose}>
      <View style={styles.overlay}>
        <View style={[styles.modalCard, styles.recoveryCard, { backgroundColor: colors.surface }]}>
          <ScrollView showsVerticalScrollIndicator={false}>
            <MaterialIcons name="key" size={36} color={colors.primary} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.twoFactor.recoveryTitle')}
            </Text>
            {regenerated && (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer, marginTop: 0, marginBottom: 12 }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {t('security.twoFactor.recoveryOldInvalid')}
                </Text>
              </View>
            )}
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
              {t('security.twoFactor.recoveryHint')}
            </Text>

            <View style={[styles.codesGrid, { backgroundColor: colors.surfaceContainerHigh }]}>
              {(codes ?? []).map((c) => (
                <Text key={c} selectable style={[styles.codeText, { color: colors.onSurface }]}>
                  {c}
                </Text>
              ))}
            </View>

            <TouchableOpacity
              style={[styles.copyBtn, { borderColor: colors.primary }]}
              onPress={handleCopy}
            >
              <MaterialIcons name={copied ? 'check' : 'content-copy'} size={18} color={colors.primary} />
              <Text style={[typo.labelLarge, { color: colors.primary }]}>
                {copied ? t('security.twoFactor.copiedLabel') : t('security.twoFactor.copyButton')}
              </Text>
            </TouchableOpacity>

            <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
              <MaterialIcons name="warning-amber" size={20} color={colors.onErrorContainer} />
              <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                {t('security.twoFactor.recoveryWarning')}
              </Text>
            </View>

            <TouchableOpacity
              style={styles.ackRow}
              onPress={() => setAcknowledged((v) => !v)}
              accessibilityRole="checkbox"
              accessibilityState={{ checked: acknowledged }}
            >
              <MaterialIcons
                name={acknowledged ? 'check-box' : 'check-box-outline-blank'}
                size={22}
                color={acknowledged ? colors.primary : colors.onSurfaceVariant}
              />
              <Text style={[typo.bodyMedium, { color: colors.onSurface, flex: 1 }]}>
                {t('security.twoFactor.recoveryAck')}
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[
                styles.modalBtnFull,
                { backgroundColor: acknowledged ? colors.primary : colors.surfaceContainerHigh },
              ]}
              onPress={handleClose}
              disabled={!acknowledged}
            >
              <Text style={[styles.modalBtnText, { color: acknowledged ? colors.onPrimary : colors.onSurfaceVariant }]}>
                {t('common.actions.done')}
              </Text>
            </TouchableOpacity>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

export default function SecurityScreen() {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { logout } = useAuthStore();

  // --- Changement de mot de passe ---
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [pwdError, setPwdError] = useState('');
  const [pwdSubmitting, setPwdSubmitting] = useState(false);
  const [pwdSuccessVisible, setPwdSuccessVisible] = useState(false);

  // --- Sessions actives ---
  const [sessions, setSessions] = useState<SessionInfo[]>([]);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [sessionsError, setSessionsError] = useState('');
  const [sessionActionError, setSessionActionError] = useState('');
  const [revokingId, setRevokingId] = useState<number | null>(null);
  const [revokeAllVisible, setRevokeAllVisible] = useState(false);
  const [revokingAll, setRevokingAll] = useState(false);

  // --- Suppression de compte (double confirmation) ---
  const [deleteStep, setDeleteStep] = useState<0 | 1 | 2>(0);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  // --- 2FA TOTP (S9d-1) ---
  const [tfaLoading, setTfaLoading] = useState(true);
  const [tfaError, setTfaError] = useState('');
  const [tfaEnabled, setTfaEnabled] = useState(false);
  // Wizard d'activation (setup -> QR + code -> confirm)
  const [wizardVisible, setWizardVisible] = useState(false);
  const [wizardSetup, setWizardSetup] = useState<TwoFactorSetupResponse | null>(null);
  const [wizardSetupError, setWizardSetupError] = useState('');
  const [wizardCode, setWizardCode] = useState('');
  const [wizardError, setWizardError] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [secretCopied, setSecretCopied] = useState(false);
  // Codes de récupération (UNIQUE modal partagée : confirm + regenerate)
  const [recoveryCodes, setRecoveryCodes] = useState<string[] | null>(null);
  const [recoveryRegenerated, setRecoveryRegenerated] = useState(false);
  // Désactivation (modal B22 : password + code)
  const [disableVisible, setDisableVisible] = useState(false);
  const [disablePassword, setDisablePassword] = useState('');
  const [disableCode, setDisableCode] = useState('');
  const [disableError, setDisableError] = useState('');
  const [disabling, setDisabling] = useState(false);
  // Régénération des codes (modal B22 : password + code)
  const [regenVisible, setRegenVisible] = useState(false);
  const [regenPassword, setRegenPassword] = useState('');
  const [regenCode, setRegenCode] = useState('');
  const [regenError, setRegenError] = useState('');
  const [regenerating, setRegenerating] = useState(false);

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  const loadSessions = useCallback(async () => {
    setSessionsLoading(true);
    setSessionsError('');
    try {
      const resp = await authApi.getActiveSessions();
      setSessions(resp.sessions);
    } catch (error: any) {
      setSessionsError(error?.response?.data?.message || t('security.sessions.loadError'));
    } finally {
      setSessionsLoading(false);
    }
  }, []);

  const loadTwoFactorStatus = useCallback(async () => {
    setTfaLoading(true);
    setTfaError('');
    try {
      const resp = await authApi.getTwoFactorStatus();
      setTfaEnabled(resp.enabled);
    } catch (error: any) {
      setTfaError(error?.response?.data?.message || t('security.twoFactor.statusError'));
    } finally {
      setTfaLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSessions();
    loadTwoFactorStatus();
  }, [loadSessions, loadTwoFactorStatus]);

  // ===== Handlers 2FA (S9d-1) =====

  const openWizard = async () => {
    setWizardVisible(true);
    setWizardSetup(null);
    setWizardSetupError('');
    setWizardCode('');
    setWizardError('');
    setSecretCopied(false);
    try {
      const resp = await authApi.setupTwoFactor();
      setWizardSetup(resp);
    } catch (error: any) {
      // 400 « déjà activée » ou erreur réseau — message backend tel quel
      setWizardSetupError(error?.response?.data?.message || t('security.twoFactor.setupError'));
    }
  };

  // Annuler le wizard est sans danger : le secret non confirmé reste enabled=false
  // côté backend et sera écrasé au prochain setup (sémantique S9a).
  const closeWizard = () => {
    if (confirming) return;
    setWizardVisible(false);
  };

  const handleCopySecret = async () => {
    if (!wizardSetup) return;
    await Clipboard.setStringAsync(wizardSetup.secret);
    setSecretCopied(true);
  };

  const handleConfirmTwoFactor = async () => {
    setWizardError('');
    setConfirming(true);
    try {
      const resp = await authApi.confirmTwoFactor(wizardCode.trim());
      setWizardVisible(false);
      setTfaEnabled(true);
      // Moment critique : les codes ne seront plus jamais affichés
      setRecoveryRegenerated(false);
      setRecoveryCodes(resp.recoveryCodes);
    } catch (error: any) {
      // 400 code invalide — message backend tel quel
      setWizardError(error?.response?.data?.message || t('security.twoFactor.codeInvalidError'));
    } finally {
      setConfirming(false);
    }
  };

  const closeDisableModal = () => {
    if (disabling) return;
    setDisableVisible(false);
    setDisablePassword('');
    setDisableCode('');
    setDisableError('');
  };

  const handleDisableTwoFactor = async () => {
    setDisableError('');
    setDisabling(true);
    try {
      await authApi.disableTwoFactor({ password: disablePassword, code: disableCode.trim() });
      setDisableVisible(false);
      setDisablePassword('');
      setDisableCode('');
      setTfaEnabled(false);
    } catch (error: any) {
      // 400 mot de passe incorrect / code incorrect — message backend tel quel, 2FA intacte
      setDisableError(error?.response?.data?.message || t('security.twoFactor.disableError'));
    } finally {
      setDisabling(false);
    }
  };

  const closeRegenModal = () => {
    if (regenerating) return;
    setRegenVisible(false);
    setRegenPassword('');
    setRegenCode('');
    setRegenError('');
  };

  const handleRegenerateCodes = async () => {
    setRegenError('');
    setRegenerating(true);
    try {
      const resp = await authApi.regenerateRecoveryCodes({ password: regenPassword, code: regenCode.trim() });
      setRegenVisible(false);
      setRegenPassword('');
      setRegenCode('');
      // Même écran codes que l'activation, avec l'avertissement « anciens codes invalides »
      setRecoveryRegenerated(true);
      setRecoveryCodes(resp.recoveryCodes);
    } catch (error: any) {
      setRegenError(error?.response?.data?.message || t('security.twoFactor.regenError'));
    } finally {
      setRegenerating(false);
    }
  };

  // Règles de force affichées en direct (la seule règle backend vérifiée est min 8 caractères)
  const ruleLength = newPassword.length >= MIN_PASSWORD_LENGTH;
  const ruleDifferent = newPassword.length > 0 && newPassword !== currentPassword;
  const ruleMatch = confirmPassword.length > 0 && newPassword === confirmPassword;
  const canSubmitPwd = !!currentPassword && ruleLength && ruleDifferent && newPassword === confirmPassword;

  const handleChangePassword = async () => {
    setPwdError('');
    setPwdSubmitting(true);
    try {
      await authApi.changePassword({ currentPassword, newPassword, confirmPassword });
      // Le backend a révoqué tous les refresh tokens (UserController:753) :
      // la session ne survivra pas au prochain refresh -> reconnexion forcée propre.
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setPwdSuccessVisible(true);
    } catch (error: any) {
      // 400 mot de passe actuel incorrect / nouveau invalide — message backend tel quel
      setPwdError(error?.response?.data?.message || t('security.password.changeError'));
    } finally {
      setPwdSubmitting(false);
    }
  };

  const handlePwdSuccessAck = async () => {
    setPwdSuccessVisible(false);
    await logout();
    router.replace('/(auth)/login');
  };

  const handleRevokeSession = async (sessionId: number) => {
    setSessionActionError('');
    setRevokingId(sessionId);
    try {
      await authApi.revokeSession(sessionId);
      setSessions((prev) => prev.filter((s) => s.id !== sessionId));
    } catch (error: any) {
      setSessionActionError(error?.response?.data?.message || t('security.sessions.revokeError'));
    } finally {
      setRevokingId(null);
    }
  };

  const handleRevokeAll = async () => {
    setSessionActionError('');
    setRevokingAll(true);
    try {
      await authApi.revokeAllOtherSessions();
      setRevokeAllVisible(false);
      await loadSessions(); // source de vérité serveur : seule la session courante doit rester
    } catch (error: any) {
      setRevokeAllVisible(false);
      setSessionActionError(
        error?.response?.data?.message || error?.message || t('security.sessions.revokeAllError')
      );
    } finally {
      setRevokingAll(false);
    }
  };

  const closeDeleteModal = () => {
    if (deleting) return;
    setDeleteStep(0);
    setDeletePassword('');
    setDeleteError('');
  };

  const handleDeleteAccount = async () => {
    setDeleteError('');
    setDeleting(true);
    try {
      // confirmDeletion:true envoyé UNIQUEMENT ici, après la 2e confirmation explicite
      await authApi.deleteAccount({ password: deletePassword, confirmDeletion: true });
      setDeleteStep(0);
      await logout();
      router.replace('/(auth)/login');
    } catch (error: any) {
      // 400 mot de passe incorrect — message backend tel quel ; on revient à l'étape mot de passe
      setDeleteError(error?.response?.data?.message || t('security.dangerZone.deleteError'));
      setDeleteStep(1);
    } finally {
      setDeleting(false);
    }
  };

  const otherSessions = sessions.filter((s) => !s.isCurrent);

  const renderRule = (ok: boolean, label: string) => (
    <View style={styles.ruleRow}>
      <MaterialIcons
        name={ok ? 'check-circle' : 'radio-button-unchecked'}
        size={16}
        color={ok ? colors.primary : colors.onSurfaceVariant}
      />
      <Text style={[typo.bodySmall, { color: ok ? colors.onSurface : colors.onSurfaceVariant }]}>{label}</Text>
    </View>
  );

  const renderSession = (s: SessionInfo) => (
    <View
      key={s.id}
      style={[styles.sessionRow, { borderColor: colors.outlineVariant, backgroundColor: colors.surface }]}
    >
      <View style={[styles.sessionIcon, { backgroundColor: colors.surfaceContainerHigh }]}>
        <MaterialIcons name={deviceIcon(s.deviceType)} size={20} color={colors.primary} />
      </View>
      <View style={styles.sessionInfo}>
        <View style={styles.sessionTitleRow}>
          <Text style={[typo.titleSmall, { color: colors.onSurface, flexShrink: 1 }]} numberOfLines={1}>
            {deviceLabel(s, t)}
          </Text>
          {s.isCurrent && (
            <View style={[styles.currentBadge, { backgroundColor: colors.primaryContainer }]}>
              <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>{t('security.sessions.currentBadge')}</Text>
            </View>
          )}
        </View>
        {!!s.ipAddress && (
          <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
            IP {s.ipAddress}{s.location ? ` · ${s.location}` : ''}
          </Text>
        )}
        <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
          {s.lastUsedAt
            ? t('security.sessions.lastActivity', { date: formatSessionDate(s.lastUsedAt) })
            : t('security.sessions.createdAt', { date: formatSessionDate(s.createdAt) })}
        </Text>
      </View>
      {!s.isCurrent && (
        <TouchableOpacity
          style={[styles.revokeBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={() => handleRevokeSession(s.id)}
          disabled={revokingId !== null}
        >
          {revokingId === s.id ? (
            <ActivityIndicator size="small" color={colors.error} />
          ) : (
            <MaterialIcons name="logout" size={18} color={colors.error} />
          )}
        </TouchableOpacity>
      )}
    </View>
  );

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity
          style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={goBack}
        >
          <MaterialIcons name={I18nManager.isRTL ? 'arrow-forward' : 'arrow-back'} size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          <Text style={[typo.overline, { color: colors.secondary }]}>{t('settings.sections.privacySecurity')}</Text>
          <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>{t('settings.security')}</Text>

          {/* ===== Changer le mot de passe ===== */}
          <Text style={[typo.titleMedium, { color: colors.onSurface, marginTop: 24 }]}>
            {t('settings.changePassword')}
          </Text>
          <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
            <PasswordTextField
              label={t('security.password.currentLabel')}
              value={currentPassword}
              onChangeText={(v) => { setCurrentPassword(v); if (pwdError) setPwdError(''); }}
              placeholder={t('security.password.currentPlaceholder')}
            />
            <View style={styles.fieldGap}>
              <PasswordTextField
                label={t('common.fields.newPassword')}
                value={newPassword}
                onChangeText={(v) => { setNewPassword(v); if (pwdError) setPwdError(''); }}
                placeholder={t('security.password.minLengthRule', { count: MIN_PASSWORD_LENGTH })}
              />
            </View>
            <View style={styles.fieldGap}>
              <PasswordTextField
                label={t('common.fields.confirmPassword')}
                value={confirmPassword}
                onChangeText={(v) => { setConfirmPassword(v); if (pwdError) setPwdError(''); }}
                placeholder={t('security.password.confirmPlaceholder')}
                error={
                  confirmPassword.length > 0 && newPassword !== confirmPassword
                    ? t('common.validation.passwordsDoNotMatch')
                    : undefined
                }
              />
            </View>

            <View style={styles.rules}>
              {renderRule(ruleLength, t('security.password.minLengthRule', { count: MIN_PASSWORD_LENGTH }))}
              {renderRule(ruleDifferent, t('security.password.differentRule'))}
              {renderRule(ruleMatch, t('security.password.matchRule'))}
            </View>

            {!!pwdError && (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {pwdError}
                </Text>
              </View>
            )}

            <View style={[styles.messageCard, { backgroundColor: colors.tertiaryContainer }]}>
              <MaterialIcons name="info-outline" size={20} color={colors.onTertiaryContainer} />
              <Text style={[typo.bodySmall, styles.messageText, { color: colors.onTertiaryContainer }]}>
                {t('security.password.revokeAllHint')}
              </Text>
            </View>

            <View style={styles.fieldGap}>
              <PrimaryButton
                full
                icon="lock-reset"
                loading={pwdSubmitting}
                disabled={!canSubmitPwd}
                onPress={handleChangePassword}
              >
                {t('settings.changePassword')}
              </PrimaryButton>
            </View>
          </View>

          {/* ===== Double authentification (S9d-1) =====
              NB : le défi au login n'est PAS encore branché (S9d-2) — un compte
              activé ici se connecte encore sans code pour l'instant. */}
          <Text style={[typo.titleMedium, { color: colors.onSurface, marginTop: 32 }]}>
            {t('security.twoFactor.title')}
          </Text>
          <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
            {tfaLoading ? (
              <LoadingState message={t('security.twoFactor.statusLoading')} />
            ) : tfaError ? (
              <ErrorState message={tfaError} onRetry={loadTwoFactorStatus} />
            ) : tfaEnabled ? (
              <>
                <View style={styles.tfaStatusRow}>
                  <View style={[styles.tfaStatusBadge, { backgroundColor: colors.primaryContainer }]}>
                    <MaterialIcons name="verified-user" size={18} color={colors.onPrimaryContainer} />
                    <Text style={[typo.labelLarge, { color: colors.onPrimaryContainer }]}>{t('security.twoFactor.enabledBadge')}</Text>
                  </View>
                </View>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 10 }]}>
                  {t('security.twoFactor.enabledHint')}
                </Text>
                <View style={styles.fieldGap}>
                  <TouchableOpacity
                    style={[styles.tfaActionBtn, { borderColor: colors.primary }]}
                    onPress={() => setRegenVisible(true)}
                  >
                    <MaterialIcons name="autorenew" size={18} color={colors.primary} />
                    <Text style={[typo.labelLarge, { color: colors.primary }]}>
                      {t('security.twoFactor.regenButton')}
                    </Text>
                  </TouchableOpacity>
                </View>
                <TouchableOpacity
                  style={[styles.tfaActionBtn, { borderColor: colors.error, marginTop: 10 }]}
                  onPress={() => setDisableVisible(true)}
                >
                  <MaterialIcons name="gpp-bad" size={18} color={colors.error} />
                  <Text style={[typo.labelLarge, { color: colors.error }]}>{t('security.twoFactor.disableButton')}</Text>
                </TouchableOpacity>
              </>
            ) : (
              <>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
                  {t('security.twoFactor.disabledHint')}
                </Text>
                <View style={styles.fieldGap}>
                  <PrimaryButton full icon="shield-lock" onPress={openWizard}>
                    {t('security.twoFactor.enableButton')}
                  </PrimaryButton>
                </View>
              </>
            )}
          </View>

          {/* ===== Sessions actives ===== */}
          <View style={styles.sectionHeaderRow}>
            <Text style={[typo.titleMedium, { color: colors.onSurface }]}>{t('settings.activeSessions')}</Text>
            {!sessionsLoading && !sessionsError && (
              <TouchableOpacity onPress={loadSessions} style={styles.refreshBtn}>
                <MaterialIcons name="refresh" size={20} color={colors.primary} />
              </TouchableOpacity>
            )}
          </View>
          <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 4 }]}>
            {t('security.sessions.intro')}
          </Text>

          {sessionsLoading ? (
            <View style={styles.stateWrap}>
              <LoadingState message={t('security.sessions.loadingLabel')} />
            </View>
          ) : sessionsError ? (
            <View style={styles.stateWrap}>
              <ErrorState message={sessionsError} onRetry={loadSessions} />
            </View>
          ) : sessions.length === 0 ? (
            <View style={styles.stateWrap}>
              <EmptyState
                icon="cellphone-link"
                title={t('security.sessions.emptyTitle')}
                message={t('security.sessions.emptyHint')}
              />
            </View>
          ) : (
            <>
              {!!sessionActionError && (
                <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                  <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                  <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                    {sessionActionError}
                  </Text>
                </View>
              )}
              <View style={styles.sessionList}>{sessions.map(renderSession)}</View>
              <TouchableOpacity
                style={[
                  styles.revokeAllBtn,
                  { borderColor: otherSessions.length === 0 ? colors.outlineVariant : colors.error },
                ]}
                onPress={() => setRevokeAllVisible(true)}
                disabled={otherSessions.length === 0}
              >
                <MaterialIcons
                  name="phonelink-erase"
                  size={18}
                  color={otherSessions.length === 0 ? colors.onSurfaceVariant : colors.error}
                />
                <Text
                  style={[
                    typo.labelLarge,
                    { color: otherSessions.length === 0 ? colors.onSurfaceVariant : colors.error },
                  ]}
                >
                  {t('security.sessions.revokeAllButton')}
                  {otherSessions.length > 0 ? ` (${otherSessions.length})` : ''}
                </Text>
              </TouchableOpacity>
            </>
          )}

          {/* ===== Zone danger ===== */}
          <Text style={[typo.titleMedium, { color: colors.error, marginTop: 32 }]}>{t('security.dangerZone.title')}</Text>
          <View style={[styles.card, styles.dangerCard, { backgroundColor: colors.surface, borderColor: colors.error }]}>
            <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{t('security.dangerZone.deleteButton')}</Text>
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 6 }]}>
              {t('security.dangerZone.warning')}
            </Text>
            <TouchableOpacity
              style={[styles.deleteBtn, { backgroundColor: colors.error }]}
              onPress={() => setDeleteStep(1)}
            >
              <MaterialIcons name="delete-forever" size={18} color={colors.onError} />
              <Text style={[typo.labelLarge, { color: colors.onError }]}>{t('security.dangerZone.deleteButton')}</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>

      {/* Modal succès mot de passe -> reconnexion forcée */}
      <Modal visible={pwdSuccessVisible} transparent animationType="fade" onRequestClose={handlePwdSuccessAck}>
        <View style={styles.overlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="check-circle" size={36} color={colors.primary} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.password.successTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 20 }]}>
              {t('security.password.successHint')}
            </Text>
            <TouchableOpacity style={[styles.modalBtnFull, { backgroundColor: colors.primary }]} onPress={handlePwdSuccessAck}>
              <Text style={[styles.modalBtnText, { color: colors.onPrimary }]}>{t('security.password.reloginButton')}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Modal confirmation revoke-all (B22) */}
      <Modal
        visible={revokeAllVisible}
        transparent
        animationType="fade"
        onRequestClose={() => !revokingAll && setRevokeAllVisible(false)}
      >
        <Pressable style={styles.overlay} onPress={() => !revokingAll && setRevokeAllVisible(false)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="phonelink-erase" size={36} color={colors.error} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.sessions.revokeAllConfirmTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 20 }]}>
              {t('security.sessions.revokeAllConfirmHint', { count: otherSessions.length })}
            </Text>
            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={() => setRevokeAllVisible(false)}
                disabled={revokingAll}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.error }]}
                onPress={handleRevokeAll}
                disabled={revokingAll}
              >
                {revokingAll ? (
                  <ActivityIndicator size="small" color={colors.onError} />
                ) : (
                  <Text style={[styles.modalBtnText, { color: colors.onError }]}>{t('security.sessions.revokeButton')}</Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Modal suppression — étape 1 : avertissement + mot de passe */}
      <Modal visible={deleteStep === 1} transparent animationType="fade" onRequestClose={closeDeleteModal}>
        <Pressable style={styles.overlay} onPress={closeDeleteModal}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="warning-amber" size={36} color={colors.error} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.dangerZone.confirmTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
              {t('security.dangerZone.confirmHint')}
            </Text>
            <PasswordTextField
              label={t('common.fields.password')}
              value={deletePassword}
              onChangeText={(v) => { setDeletePassword(v); if (deleteError) setDeleteError(''); }}
              placeholder={t('security.passwordPlaceholder')}
            />
            {!!deleteError && (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {deleteError}
                </Text>
              </View>
            )}
            <View style={[styles.modalActions, { marginTop: 20 }]}>
              <TouchableOpacity style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={closeDeleteModal}>
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: deletePassword ? colors.error : colors.surfaceContainerHigh }]}
                onPress={() => { setDeleteError(''); setDeleteStep(2); }}
                disabled={!deletePassword}
              >
                <Text style={[styles.modalBtnText, { color: deletePassword ? colors.onError : colors.onSurfaceVariant }]}>
                  {t('security.dangerZone.continueButton')}
                </Text>
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Modal suppression — étape 2 : confirmation finale explicite */}
      <Modal visible={deleteStep === 2} transparent animationType="fade" onRequestClose={closeDeleteModal}>
        <Pressable style={styles.overlay} onPress={() => !deleting && closeDeleteModal()}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="delete-forever" size={36} color={colors.error} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.dangerZone.finalTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 20 }]}>
              {t('security.dangerZone.finalHint')}
            </Text>
            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={closeDeleteModal}
                disabled={deleting}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.error }]}
                onPress={handleDeleteAccount}
                disabled={deleting}
              >
                {deleting ? (
                  <ActivityIndicator size="small" color={colors.onError} />
                ) : (
                  <Text style={[styles.modalBtnText, { color: colors.onError }]}>{t('security.dangerZone.finalButton')}</Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* ===== Wizard activation 2FA : QR + clé + premier code (S9d-1) ===== */}
      <Modal visible={wizardVisible} transparent animationType="fade" onRequestClose={closeWizard}>
        <Pressable style={styles.overlay} onPress={closeWizard}>
          <Pressable
            onPress={(e) => e.stopPropagation()}
            style={[styles.modalCard, styles.recoveryCard, { backgroundColor: colors.surface }]}
          >
            <ScrollView showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
              <MaterialIcons name="qr-code-2" size={36} color={colors.primary} style={styles.modalIcon} />
              <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
                {t('security.twoFactor.enableButton')}
              </Text>

              {!wizardSetup && !wizardSetupError && <LoadingState message={t('security.twoFactor.setupLoading')} />}
              {!!wizardSetupError && <ErrorState message={wizardSetupError} onRetry={openWizard} />}

              {wizardSetup && (
                <>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
                    {t('security.twoFactor.scanStep')}
                  </Text>
                  <View style={styles.qrWrap}>
                    {/* Fond blanc / modules noirs imposés par la scannabilité du QR — design-fixed */}
                    <QRCode value={wizardSetup.otpauthUri} size={180} backgroundColor="#FFFFFF" color="#000000" />
                  </View>

                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 16 }]}>
                    {t('security.twoFactor.manualKeyHint')}
                  </Text>
                  <View style={[styles.secretRow, { backgroundColor: colors.surfaceContainerHigh }]}>
                    <Text selectable style={[styles.codeText, styles.secretText, { color: colors.onSurface }]}>
                      {wizardSetup.secret}
                    </Text>
                    <TouchableOpacity onPress={handleCopySecret} hitSlop={8}>
                      <MaterialIcons
                        name={secretCopied ? 'check' : 'content-copy'}
                        size={20}
                        color={colors.primary}
                      />
                    </TouchableOpacity>
                  </View>

                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 20, marginBottom: 10 }]}>
                    {t('security.twoFactor.codeStep')}
                  </Text>
                  <TextField
                    label={t('security.twoFactor.codeLabel')}
                    icon="shield-key"
                    value={wizardCode}
                    onChangeText={(v) => { setWizardCode(v); if (wizardError) setWizardError(''); }}
                    placeholder="123456"
                    keyboardType="number-pad"
                    maxLength={6}
                    error={wizardError || undefined}
                  />

                  <View style={[styles.modalActions, { marginTop: 20 }]}>
                    <TouchableOpacity
                      style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                      onPress={closeWizard}
                      disabled={confirming}
                    >
                      <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                      style={[
                        styles.modalBtn,
                        { backgroundColor: wizardCode.trim().length === 6 ? colors.primary : colors.surfaceContainerHigh },
                      ]}
                      onPress={handleConfirmTwoFactor}
                      disabled={wizardCode.trim().length !== 6 || confirming}
                    >
                      {confirming ? (
                        <ActivityIndicator size="small" color={colors.onPrimary} />
                      ) : (
                        <Text
                          style={[
                            styles.modalBtnText,
                            { color: wizardCode.trim().length === 6 ? colors.onPrimary : colors.onSurfaceVariant },
                          ]}
                        >
                          {t('common.actions.confirm')}
                        </Text>
                      )}
                    </TouchableOpacity>
                  </View>
                </>
              )}
            </ScrollView>
          </Pressable>
        </Pressable>
      </Modal>

      {/* ===== Codes de récupération — UNIQUE composant (confirm + regenerate), garde non contournable ===== */}
      <RecoveryCodesModal
        codes={recoveryCodes}
        regenerated={recoveryRegenerated}
        onClose={() => setRecoveryCodes(null)}
      />

      {/* ===== Modal désactivation 2FA (B22) : mot de passe + code ===== */}
      <Modal visible={disableVisible} transparent animationType="fade" onRequestClose={closeDisableModal}>
        <Pressable style={styles.overlay} onPress={closeDisableModal}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="gpp-bad" size={36} color={colors.error} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.twoFactor.disableConfirmTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
              {t('security.twoFactor.disableConfirmHint')}
            </Text>
            <PasswordTextField
              label={t('common.fields.password')}
              value={disablePassword}
              onChangeText={(v) => { setDisablePassword(v); if (disableError) setDisableError(''); }}
              placeholder={t('security.passwordPlaceholder')}
            />
            <View style={styles.fieldGap}>
              <TextField
                label={t('security.twoFactor.codeOrRecoveryLabel')}
                icon="shield-key"
                value={disableCode}
                onChangeText={(v) => { setDisableCode(v); if (disableError) setDisableError(''); }}
                placeholder={t('security.twoFactor.codeOrRecoveryPlaceholder')}
                autoCapitalize="characters"
                autoCorrect={false}
              />
            </View>
            {!!disableError && (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {disableError}
                </Text>
              </View>
            )}
            <View style={[styles.modalActions, { marginTop: 20 }]}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={closeDisableModal}
                disabled={disabling}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.modalBtn,
                  { backgroundColor: disablePassword && disableCode ? colors.error : colors.surfaceContainerHigh },
                ]}
                onPress={handleDisableTwoFactor}
                disabled={!disablePassword || !disableCode || disabling}
              >
                {disabling ? (
                  <ActivityIndicator size="small" color={colors.onError} />
                ) : (
                  <Text
                    style={[
                      styles.modalBtnText,
                      { color: disablePassword && disableCode ? colors.onError : colors.onSurfaceVariant },
                    ]}
                  >
                    {t('security.twoFactor.disableButton')}
                  </Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>

      {/* ===== Modal régénération des codes (B22) : mot de passe + code ===== */}
      <Modal visible={regenVisible} transparent animationType="fade" onRequestClose={closeRegenModal}>
        <Pressable style={styles.overlay} onPress={closeRegenModal}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <MaterialIcons name="autorenew" size={36} color={colors.primary} style={styles.modalIcon} />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('security.twoFactor.regenConfirmTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
              {t('security.twoFactor.regenConfirmHint')}
            </Text>
            <PasswordTextField
              label={t('common.fields.password')}
              value={regenPassword}
              onChangeText={(v) => { setRegenPassword(v); if (regenError) setRegenError(''); }}
              placeholder={t('security.passwordPlaceholder')}
            />
            <View style={styles.fieldGap}>
              <TextField
                label={t('security.twoFactor.codeOrRecoveryLabel')}
                icon="shield-key"
                value={regenCode}
                onChangeText={(v) => { setRegenCode(v); if (regenError) setRegenError(''); }}
                placeholder={t('security.twoFactor.codeOrRecoveryPlaceholder')}
                autoCapitalize="characters"
                autoCorrect={false}
              />
            </View>
            {!!regenError && (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {regenError}
                </Text>
              </View>
            )}
            <View style={[styles.modalActions, { marginTop: 20 }]}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={closeRegenModal}
                disabled={regenerating}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.modalBtn,
                  { backgroundColor: regenPassword && regenCode ? colors.primary : colors.surfaceContainerHigh },
                ]}
                onPress={handleRegenerateCodes}
                disabled={!regenPassword || !regenCode || regenerating}
              >
                {regenerating ? (
                  <ActivityIndicator size="small" color={colors.onPrimary} />
                ) : (
                  <Text
                    style={[
                      styles.modalBtnText,
                      { color: regenPassword && regenCode ? colors.onPrimary : colors.onSurfaceVariant },
                    ]}
                  >
                    {t('security.twoFactor.regenConfirmButton')}
                  </Text>
                )}
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  flex: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 64 },
  card: { borderRadius: 20, borderWidth: 1, padding: 20, marginTop: 12 },
  dangerCard: { borderWidth: 1.5 },
  fieldGap: { marginTop: 18 },
  rules: { marginTop: 16, gap: 8 },
  ruleRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  messageCard: {
    flexDirection: 'row', alignItems: 'flex-start', gap: 10,
    borderRadius: 14, padding: 14, marginTop: 16,
  },
  messageText: { flex: 1 },
  sectionHeaderRow: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 32,
  },
  refreshBtn: { padding: 4 },
  stateWrap: { paddingVertical: 12 },
  sessionList: { marginTop: 12, gap: 10 },
  sessionRow: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    borderRadius: 16, borderWidth: 1, padding: 14,
  },
  sessionIcon: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  sessionInfo: { flex: 1, gap: 2 },
  sessionTitleRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  currentBadge: { borderRadius: 999, paddingHorizontal: 8, paddingVertical: 2 },
  revokeBtn: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  revokeAllBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    borderWidth: 1, borderRadius: 999, paddingVertical: 14, marginTop: 14,
  },
  deleteBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    borderRadius: 999, paddingVertical: 14, marginTop: 16,
  },
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  modalIcon: { alignSelf: 'center', marginBottom: 12 },
  modalActions: { flexDirection: 'row', gap: 12 },
  modalBtn: { flex: 1, paddingVertical: 14, borderRadius: 999, alignItems: 'center', justifyContent: 'center' },
  modalBtnFull: { paddingVertical: 14, borderRadius: 999, alignItems: 'center' },
  modalBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  // --- 2FA (S9d-1) ---
  tfaStatusRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 4 },
  tfaStatusBadge: {
    flexDirection: 'row', alignItems: 'center', gap: 6,
    borderRadius: 999, paddingHorizontal: 12, paddingVertical: 6,
  },
  tfaActionBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    borderWidth: 1, borderRadius: 999, paddingVertical: 14, marginTop: 16,
  },
  recoveryCard: { maxHeight: '85%' },
  codesGrid: {
    flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'space-between',
    borderRadius: 14, paddingHorizontal: 18, paddingVertical: 14, rowGap: 8,
  },
  codeText: {
    width: '48%',
    fontFamily: Platform.select({ ios: 'Menlo', android: 'monospace', default: 'monospace' }),
    fontSize: 14,
    textAlign: 'center',
  },
  copyBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    borderWidth: 1, borderRadius: 999, paddingVertical: 12, marginTop: 14,
  },
  ackRow: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 16, marginBottom: 16 },
  qrWrap: {
    alignSelf: 'center',
    backgroundColor: '#FFFFFF', // design-fixed : un QR code exige un fond blanc, mode sombre inclus
    borderRadius: 12,
    padding: 12,
    marginBottom: 16,
  },
  secretRow: {
    flexDirection: 'row', alignItems: 'center', gap: 10,
    borderRadius: 12, paddingHorizontal: 14, paddingVertical: 10, marginBottom: 16,
  },
  secretText: { flex: 1, width: undefined, textAlign: 'left', fontSize: 13 },
});
