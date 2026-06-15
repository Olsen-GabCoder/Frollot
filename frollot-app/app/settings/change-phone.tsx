import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  I18nManager,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { usersApi } from '../../src/api/users';
import { authApi } from '../../src/api/auth';
import { PasswordTextField } from '../../src/components/ui/TextField';
import { PrimaryButton } from '../../src/components/ui/Button';
import { PhoneNumberField } from '../../src/components/phone';

type Step = 'form' | 'success';

/**
 * Écran « Numéro de téléphone » (incrément 3 — clôture du chantier téléphone).
 *
 * Numéro DÉCLARATIF, sans vérification OTP/SMS (couche future). PREFILL via /me
 * (PAS le store : l'AuthResponse du login ne porte ni phoneNumber ni phonePublic,
 * le store peut être vide après un login frais). Enregistrer et Supprimer sont
 * deux intentions SÉPARÉES (boutons distincts, suppression confirmée en modal B22).
 */
export default function ChangePhoneScreen() {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { user, setUser } = useAuthStore();

  // Chargement initial (/me)
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  // État réel serveur (sert au prefill + à montrer/cacher « Supprimer »)
  const [existingPhone, setExistingPhone] = useState<string | null>(null);

  // Saisie
  const [e164, setE164] = useState<string | null>(null);
  const [phoneValid, setPhoneValid] = useState(true);
  const [phonePublic, setPhonePublic] = useState(false);
  const [password, setPassword] = useState('');

  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [step, setStep] = useState<Step>('form');
  const [successMessage, setSuccessMessage] = useState('');

  // Suppression (modal B22, mot de passe propre à la modale)
  const [deleteVisible, setDeleteVisible] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  const leaveScreen = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  const loadCurrentPhone = async () => {
    setLoading(true);
    setLoadError('');
    try {
      const fresh = await usersApi.getCurrentUser();
      setExistingPhone(fresh.phoneNumber ?? null);
      setE164(fresh.phoneNumber ?? null);
      setPhonePublic(fresh.phonePublic ?? false);
      // Le store profite du refetch (vue propriétaire complète)
      setUser(fresh);
    } catch (error: any) {
      setLoadError(
        error?.response?.data?.message || t('phone.loadError')
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCurrentPhone();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSave = async () => {
    if (!e164 || !phoneValid || !password) return;
    setServerError('');
    setSubmitting(true);
    try {
      const resp = await authApi.changePhone({ newPhone: e164, phonePublic, password });
      // Mise à jour locale façon S4 : l'app ne doit plus montrer l'ancien numéro
      if (user) setUser({ ...user, phoneNumber: resp.newPhone ?? e164, phonePublic: resp.phonePublic });
      setExistingPhone(resp.newPhone ?? e164);
      setPassword(''); // hygiène : le mot de passe n'a plus de raison de vivre en state
      setSuccessMessage(
        resp.phonePublic
          ? t('phone.savedPublicSuccess')
          : t('phone.savedPrivateSuccess')
      );
      setStep('success');
    } catch (error: any) {
      // 400 format invalide / « déjà utilisé » / mot de passe incorrect — message backend tel quel
      setServerError(error?.response?.data?.message || t('phone.saveError'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deletePassword) return;
    setDeleteError('');
    setDeleting(true);
    try {
      // newPhone vide = suppression : backend met NULL + phonePublic=false (prouvé incrément 1)
      await authApi.changePhone({ newPhone: '', phonePublic: false, password: deletePassword });
      if (user) setUser({ ...user, phoneNumber: undefined, phonePublic: false });
      setExistingPhone(null);
      setE164(null);
      setPhonePublic(false);
      setDeleteVisible(false);
      setDeletePassword('');
      setPassword('');
      setSuccessMessage(t('phone.deletedSuccess'));
      setStep('success');
    } catch (error: any) {
      setDeleteError(error?.response?.data?.message || t('phone.deleteError'));
    } finally {
      setDeleting(false);
    }
  };

  const closeDeleteModal = () => {
    if (deleting) return;
    setDeleteVisible(false);
    setDeletePassword('');
    setDeleteError('');
  };

  const canSave = !!e164 && phoneValid && !!password;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]}
          onPress={leaveScreen}
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
          {step === 'form' && (
            <>
              <Text style={[typo.overline, { color: colors.secondary }]}>{t('settings.sections.account')}</Text>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
                {t('phone.numberLabel')}
              </Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
                {t('phone.changeIntro')}
              </Text>

              {loading ? (
                <View style={styles.loadingWrap}>
                  <ActivityIndicator size="large" color={colors.primary} />
                  <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
                    {t('phone.loadingCurrent')}
                  </Text>
                </View>
              ) : loadError ? (
                <View style={[styles.formCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
                  <View style={[styles.messageCard, styles.noTopMargin, { backgroundColor: colors.errorContainer }]}>
                    <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                    <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                      {loadError}
                    </Text>
                  </View>
                  <View style={styles.fieldGap}>
                    <PrimaryButton full icon="refresh" onPress={loadCurrentPhone}>
                      {t('common.actions.retry')}
                    </PrimaryButton>
                  </View>
                </View>
              ) : (
                <>
                  <View style={[styles.formCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
                    <PhoneNumberField
                      value={e164 ?? ''}
                      onChangeE164={setE164}
                      onValidityChange={setPhoneValid}
                      editable={!submitting}
                    />

                    {/* Visibilité — libellés honnêtes de chaque état */}
                    <View style={[styles.visibilityRow, styles.fieldGap]}>
                      <View style={[styles.visibilityIcon, { backgroundColor: colors.surfaceContainerHigh }]}>
                        <MaterialIcons
                          name={phonePublic ? 'public' : 'lock-outline'}
                          size={20}
                          color={phonePublic ? colors.primary : colors.onSurfaceVariant}
                        />
                      </View>
                      <View style={styles.visibilityTextWrap}>
                        <Text style={[typo.titleSmall, { color: colors.onSurface }]}>
                          {phonePublic ? t('phone.publicTitle') : t('phone.privateTitle')}
                        </Text>
                        <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 1 }]}>
                          {phonePublic
                            ? t('phone.publicHint')
                            : t('phone.privateHint')}
                        </Text>
                      </View>
                      <Switch
                        value={phonePublic}
                        onValueChange={setPhonePublic}
                        disabled={submitting}
                        trackColor={{ false: colors.surfaceVariant, true: colors.primaryContainer }}
                        thumbColor={phonePublic ? colors.primary : colors.outline}
                      />
                    </View>

                    {/* Honnêteté : le canal transactionnel n'est pas soumis à la visibilité */}
                    <Text style={[typo.bodySmall, styles.transactionalNote, { color: colors.onSurfaceVariant }]}>
                      {t('phone.transactionalNote')}
                    </Text>

                    <View style={styles.fieldGap}>
                      <PasswordTextField
                        label={t('common.fields.password')}
                        value={password}
                        onChangeText={setPassword}
                        placeholder={t('settings.passwordConfirmPlaceholder')}
                      />
                    </View>

                    {serverError ? (
                      <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                        <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                        <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                          {serverError}
                        </Text>
                      </View>
                    ) : null}

                    <View style={styles.fieldGap}>
                      <PrimaryButton
                        full
                        icon="check"
                        loading={submitting}
                        disabled={!canSave}
                        onPress={handleSave}
                      >
                        {t('common.actions.save')}
                      </PrimaryButton>
                    </View>
                  </View>

                  {/* Suppression — intention SÉPARÉE, visible seulement si un numéro existe */}
                  {existingPhone && (
                    <TouchableOpacity
                      style={[styles.deleteBtn, { borderColor: colors.error }]}
                      onPress={() => setDeleteVisible(true)}
                      disabled={submitting}
                    >
                      <MaterialIcons name="delete-outline" size={20} color={colors.error} />
                      <Text style={[typo.labelLarge, { color: colors.error, marginStart: 8 }]}>
                        {t('phone.deleteButton')}
                      </Text>
                    </TouchableOpacity>
                  )}
                </>
              )}
            </>
          )}

          {step === 'success' && (
            <View style={styles.successWrap}>
              <View style={[styles.successCircle, { backgroundColor: colors.primaryContainer }]}>
                <MaterialIcons name="check-circle" size={48} color={colors.primary} />
              </View>
              <Text style={[typo.headlineMedium, { color: colors.onBackground, textAlign: 'center', marginTop: 20 }]}>
                {t('phone.successTitle')}
              </Text>
              <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 8 }]}>
                {successMessage}
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

      {/* Confirmation de suppression — patron B22 */}
      <Modal visible={deleteVisible} transparent animationType="fade" onRequestClose={closeDeleteModal}>
        <Pressable style={styles.modalOverlay} onPress={closeDeleteModal}>
          <Pressable
            onPress={(e) => e.stopPropagation()}
            style={[styles.modalCard, { backgroundColor: colors.surface }]}
          >
            <MaterialIcons
              name="delete-outline"
              size={36}
              color={colors.error}
              style={styles.modalIcon}
            />
            <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
              {t('phone.deleteConfirmTitle')}
            </Text>
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 16 }]}>
              {t('phone.deleteConfirmHint')}
            </Text>

            <PasswordTextField
              label={t('common.fields.password')}
              value={deletePassword}
              onChangeText={(v) => { setDeletePassword(v); if (deleteError) setDeleteError(''); }}
              placeholder={t('settings.passwordConfirmPlaceholder')}
            />

            {deleteError ? (
              <View style={[styles.messageCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
                <Text style={[typo.bodySmall, styles.messageText, { color: colors.onErrorContainer }]}>
                  {deleteError}
                </Text>
              </View>
            ) : null}

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.surfaceContainerHigh }]}
                onPress={closeDeleteModal}
                disabled={deleting}
              >
                <Text style={[styles.modalBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalBtn, { backgroundColor: colors.error }, (!deletePassword || deleting) && styles.modalBtnDisabled]}
                onPress={handleDelete}
                disabled={!deletePassword || deleting}
              >
                {deleting ? (
                  <ActivityIndicator size={18} color={colors.onError} />
                ) : (
                  <Text style={[styles.modalBtnText, { color: colors.onError }]}>{t('common.actions.delete')}</Text>
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
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 48 },
  loadingWrap: { alignItems: 'center', paddingTop: 72 },
  formCard: { borderRadius: 20, borderWidth: 1, padding: 20, marginTop: 24 },
  fieldGap: { marginTop: 18 },
  noTopMargin: { marginTop: 0 },
  visibilityRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  visibilityIcon: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  visibilityTextWrap: { flex: 1 },
  transactionalNote: { marginTop: 10, fontStyle: 'italic' },
  messageCard: {
    flexDirection: 'row', alignItems: 'flex-start', gap: 10,
    borderRadius: 14, padding: 14, marginTop: 18,
  },
  messageText: { flex: 1 },
  deleteBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    height: 52, borderRadius: 28, borderWidth: 1, marginTop: 24,
  },
  successWrap: { alignItems: 'center', paddingTop: 64, paddingHorizontal: 8 },
  successCircle: { width: 96, height: 96, borderRadius: 48, alignItems: 'center', justifyContent: 'center' },
  successBtn: { width: '100%', marginTop: 32 },
  // Modal (B22)
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  modalIcon: { alignSelf: 'center', marginBottom: 12 },
  modalActions: { flexDirection: 'row', gap: 12, marginTop: 20 },
  modalBtn: { flex: 1, height: 48, borderRadius: 999, alignItems: 'center', justifyContent: 'center' },
  modalBtnDisabled: { opacity: 0.5 },
  modalBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
