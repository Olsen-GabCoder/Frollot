/**
 * Écran de défi 2FA au login (S9d-2) — dernière pièce du chantier 2FA (S9a-S9d).
 *
 * Atterrissage depuis login.tsx quand le backend (S9b) répond au login par
 * { requiresTwoFactor: true, twoFactorToken } SANS vrais tokens. Le jeton 2fa_pending
 * (5 min, 5 essais max par jti) vit UNIQUEMENT en mémoire dans authStore
 * (pendingTwoFactorToken) — jamais persisté, jamais dans l'URL.
 *
 * - Un seul champ : TOTP 6 chiffres OU code de récupération XXXX-XXXX (le backend
 *   verifyLoginCode accepte les deux, on passe la chaîne brute).
 * - Succès -> finalizeSession (MÊME chemin qu'un login normal) -> accueil.
 * - Erreurs backend affichées telles quelles ; messages contenant « reconnecter »
 *   (jeton expiré 5 min / épuisé 5 essais) = jeton mort -> seul recours : retour login.
 * - Anti double-soumission : chaque essai compte dans le plafond de 5 par jti.
 * - Annuler / arrivée sans jeton (URL directe, refresh web) -> retour login propre.
 */
import { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  I18nManager,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { TextField, PrimaryButton, OutlineButton } from '../../src/components/ui';
import { useTheme } from '../../src/theme';

export default function TwoFactorScreen() {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const { loginTwoFactor, clearTwoFactorChallenge, pendingTwoFactorToken } = useAuthStore();

  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  // Jeton mort (expiré / 5 essais épuisés) : réessayer ici est inutile, il faut
  // repasser par le login pour obtenir un nouveau défi.
  const [tokenDead, setTokenDead] = useState(false);
  // Évite que l'effet « pas de jeton -> retour login » ne double la navigation
  // déclenchée par le succès (finalizeSession vide pendingTwoFactorToken) ou par Annuler.
  const navigatedRef = useRef(false);

  // Arrivée sans défi en cours (URL directe, refresh web qui a perdu la mémoire) :
  // rien à faire ici, retour au login.
  useEffect(() => {
    if (!pendingTwoFactorToken && !navigatedRef.current) {
      router.replace('/(auth)/login');
    }
  }, [pendingTwoFactorToken]);

  const leaveToLogin = () => {
    navigatedRef.current = true;
    clearTwoFactorChallenge(); // jette le jeton, rien n'a été stocké
    if (router.canGoBack()) {
      router.back(); // B24b
    } else {
      router.replace('/(auth)/login');
    }
  };

  const handleSubmit = async () => {
    if (submitting) return; // anti double-soumission (5 essais max par jeton)
    const trimmed = code.trim();
    if (!trimmed) {
      setError(t('auth.twoFactor.codeRequired'));
      return;
    }
    if (!pendingTwoFactorToken) return;
    setError(null);
    setSubmitting(true);
    try {
      await loginTwoFactor(pendingTwoFactorToken, trimmed);
      navigatedRef.current = true;
      router.replace('/(tabs)'); // même destination finale qu'un login normal
    } catch (e: any) {
      // Erreur backend telle quelle (« Code de vérification incorrect. »,
      // « Trop de tentatives... », « Jeton de vérification invalide ou expiré... »)
      const message: string =
        e?.response?.data?.message || e?.message || t('auth.twoFactor.verifyError');
      if (message.includes('reconnecter')) {
        setTokenDead(true);
      }
      setError(message);
      setSubmitting(false);
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.surface }]}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        style={styles.flex}
      >
        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <TouchableOpacity
            onPress={leaveToLogin}
            disabled={submitting}
            style={styles.backBtn}
            accessibilityLabel={t('auth.twoFactor.cancelA11y')}
          >
            <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
          </TouchableOpacity>

          <View style={[styles.iconWrap, { backgroundColor: colors.primaryContainer }]}>
            <MaterialCommunityIcons name="shield-lock" size={34} color={colors.onPrimaryContainer} />
          </View>

          <Text style={[styles.headline, { color: colors.onBackground }]}>
            {t('auth.twoFactor.title')}
          </Text>
          <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>
            {t('auth.twoFactor.subtitle')}
          </Text>

          <View style={styles.field}>
            <TextField
              label={t('auth.twoFactor.codeLabel')}
              icon="key-outline"
              placeholder={t('auth.twoFactor.codePlaceholder')}
              value={code}
              onChangeText={setCode}
              autoCapitalize="characters"
              autoCorrect={false}
              autoComplete="one-time-code"
              editable={!submitting && !tokenDead}
              onSubmitEditing={tokenDead ? undefined : handleSubmit}
            />
          </View>

          {error && (
            <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
              <MaterialCommunityIcons name="alert-circle" size={18} color={colors.onErrorContainer} />
              <Text style={[styles.errorText, { color: colors.onErrorContainer }]}>{error}</Text>
            </View>
          )}

          {tokenDead ? (
            // Jeton expiré (5 min) ou épuisé (5 essais) : il faut repartir du login
            <PrimaryButton icon="login" full onPress={leaveToLogin}>
              {t('auth.twoFactor.backToLogin')}
            </PrimaryButton>
          ) : (
            <>
              <PrimaryButton
                icon="check"
                full
                onPress={handleSubmit}
                loading={submitting}
                disabled={!code.trim()}
              >
                {t('auth.twoFactor.verifyButton')}
              </PrimaryButton>
              <View style={styles.cancelGap}>
                <OutlineButton full onPress={leaveToLogin} disabled={submitting}>
                  {t('common.actions.cancel')}
                </OutlineButton>
              </View>
            </>
          )}

          <Text style={[styles.hint, { color: colors.onSurfaceVariant }]}>
            {t('auth.twoFactor.hint')}
          </Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  flex: { flex: 1 },
  content: { paddingTop: 52, paddingHorizontal: 24, paddingBottom: 32 },
  backBtn: {
    width: 40, height: 40, borderRadius: 20,
    alignItems: 'center', justifyContent: 'center',
    marginBottom: 12,
  },
  iconWrap: {
    width: 64, height: 64, borderRadius: 32,
    alignItems: 'center', justifyContent: 'center',
    marginBottom: 18,
  },
  headline: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 34,
    fontWeight: '600',
    lineHeight: 38,
    letterSpacing: -0.5,
  },
  subtitle: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    lineHeight: 20,
    letterSpacing: 0.2,
    marginTop: 10,
  },
  field: { marginTop: 24, marginBottom: 16 },
  errorCard: {
    flexDirection: 'row', alignItems: 'center', gap: 10,
    padding: 14, borderRadius: 12, marginBottom: 14,
  },
  errorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  cancelGap: { marginTop: 12 },
  hint: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    lineHeight: 18,
    textAlign: 'center',
    marginTop: 20,
  },
});
