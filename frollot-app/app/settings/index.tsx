import { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Modal, Pressable } from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { usePreferencesStore } from '../../src/stores/preferencesStore';
import { LogoutConfirmModal } from '../../src/components/common';

type ThemeMode = 'system' | 'light' | 'dark';
type Language = 'en' | 'fr' | 'es' | 'de' | 'ar';

const LANGUAGES: { code: Language; label: string }[] = [
  { code: 'fr', label: 'Français' },
  { code: 'en', label: 'English' },
  { code: 'es', label: 'Español' },
  { code: 'de', label: 'Deutsch' },
  { code: 'ar', label: 'العربية' },
];

const THEME_MODES: { mode: ThemeMode; label: string; icon: 'settings-suggest' | 'light-mode' | 'dark-mode' }[] = [
  { mode: 'system', label: 'Système', icon: 'settings-suggest' },
  { mode: 'light', label: 'Clair', icon: 'light-mode' },
  { mode: 'dark', label: 'Sombre', icon: 'dark-mode' },
];

interface RowDef {
  icon: keyof typeof MaterialIcons.glyphMap;
  label: string;
  sub?: string;
  /** Cible de navigation. Absente = écran pas encore créé -> ligne désactivée + badge « Bientôt ». */
  route?: string;
}

export default function SettingsScreen() {
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { user } = useAuthStore();
  const { themeMode, language, setThemeMode, setLanguage } = usePreferencesStore();
  const [showLanguageModal, setShowLanguageModal] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  const currentLanguageLabel = LANGUAGES.find((l) => l.code === language)?.label ?? 'Français';

  // route absente = écran à venir (familles A/B/C de l'Étape 8)
  const sections: { title: string; rows: RowDef[] }[] = [
    {
      title: 'Compte',
      rows: [
        { icon: 'alternate-email', label: t('settings.changeEmail'), sub: user?.email ?? undefined, route: '/settings/change-email' },
        { icon: 'phone-iphone', label: t('settings.changePhone'), sub: 'Numéro de téléphone' },
      ],
    },
    {
      title: 'Confidentialité & sécurité',
      rows: [
        { icon: 'lock-outline', label: t('settings.security'), sub: 'Mot de passe, 2FA, sessions' },
        { icon: 'block', label: t('settings.blockedUsers'), sub: 'Gérer les comptes bloqués' },
        { icon: 'verified-user', label: 'Demande de vérification', sub: 'Obtenir le badge vérifié' },
      ],
    },
    {
      title: 'Support',
      rows: [
        { icon: 'help-outline', label: t('settings.helpCenter'), sub: 'FAQ et guides' },
        { icon: 'support-agent', label: t('settings.contactSupport'), sub: 'Notre équipe vous répond' },
      ],
    },
    {
      title: 'Légal',
      rows: [
        { icon: 'description', label: t('settings.termsOfService') },
        { icon: 'privacy-tip', label: t('settings.privacyPolicy') },
      ],
    },
  ];

  const renderRow = (row: RowDef, isLast: boolean) => {
    const comingSoon = !row.route;
    const content = (
      <>
        <View style={[styles.rowIconCircle, { backgroundColor: colors.surfaceContainerHigh }]}>
          <MaterialIcons
            name={row.icon}
            size={20}
            color={comingSoon ? colors.onSurfaceVariant : colors.primary}
          />
        </View>
        <View style={styles.rowTextWrap}>
          <Text style={[typo.titleSmall, { color: colors.onSurface }]} numberOfLines={1}>
            {row.label}
          </Text>
          {row.sub && (
            <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 1 }]} numberOfLines={1}>
              {row.sub}
            </Text>
          )}
        </View>
        {comingSoon ? (
          <View style={[styles.soonBadge, { backgroundColor: colors.tertiaryContainer }]}>
            <Text style={[typo.labelSmall, { color: colors.onTertiaryContainer }]}>Bientôt</Text>
          </View>
        ) : (
          <MaterialIcons name="chevron-right" size={22} color={colors.onSurfaceVariant} />
        )}
      </>
    );

    return (
      <View key={row.label}>
        {comingSoon ? (
          <View style={[styles.row, styles.rowDisabled]}>{content}</View>
        ) : (
          <TouchableOpacity style={styles.row} activeOpacity={0.6} onPress={() => router.push(row.route as never)}>
            {content}
          </TouchableOpacity>
        )}
        {!isLast && <View style={[styles.separator, { backgroundColor: colors.outlineVariant }]} />}
      </View>
    );
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name="arrow-back" size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <Text style={[typo.overline, { color: colors.secondary }]}>Votre espace</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
          {t('settings.settings')}
        </Text>

        {/* Identity card */}
        {user && (
          <View style={[styles.identityCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
            <View style={[styles.identityAvatar, { backgroundColor: colors.primaryContainer }]}>
              <Text style={[typo.headlineSmall, { color: colors.onPrimaryContainer }]}>
                {(user.firstName?.[0] || 'F').toUpperCase()}
              </Text>
            </View>
            <View style={styles.rowTextWrap}>
              <Text style={[typo.titleMedium, { color: colors.onSurface }]} numberOfLines={1}>
                {user.firstName} {user.lastName}
              </Text>
              <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                {user.email}
              </Text>
            </View>
            {user.isVerified && <MaterialIcons name="verified" size={20} color={colors.primary} />}
          </View>
        )}

        {/* Sections Compte + Confidentialité */}
        {sections.slice(0, 2).map((section) => (
          <View key={section.title}>
            <Text style={[typo.overline, styles.sectionTitle, { color: colors.secondary }]}>{section.title}</Text>
            <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              {section.rows.map((row, i) => renderRow(row, i === section.rows.length - 1))}
            </View>
          </View>
        ))}

        {/* Apparence — fonctionnel (préférences locales, pas d'API) */}
        <Text style={[typo.overline, styles.sectionTitle, { color: colors.secondary }]}>Apparence</Text>
        <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
          <View style={styles.row}>
            <View style={[styles.rowIconCircle, { backgroundColor: colors.surfaceContainerHigh }]}>
              <MaterialIcons name="palette" size={20} color={colors.primary} />
            </View>
            <View style={styles.rowTextWrap}>
              <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{t('settings.theme')}</Text>
            </View>
          </View>
          <View style={styles.themeSegment}>
            {THEME_MODES.map((tm) => {
              const on = themeMode === tm.mode;
              return (
                <TouchableOpacity
                  key={tm.mode}
                  style={[styles.themeOption, {
                    backgroundColor: on ? colors.primaryContainer : colors.surfaceContainer,
                    borderColor: on ? colors.primary : colors.outlineVariant,
                  }]}
                  onPress={() => setThemeMode(tm.mode)}
                >
                  <MaterialIcons name={tm.icon} size={18} color={on ? colors.primary : colors.onSurfaceVariant} />
                  <Text style={[typo.labelMedium, { color: on ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
                    {tm.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <View style={[styles.separator, { backgroundColor: colors.outlineVariant }]} />
          <TouchableOpacity style={styles.row} activeOpacity={0.6} onPress={() => setShowLanguageModal(true)}>
            <View style={[styles.rowIconCircle, { backgroundColor: colors.surfaceContainerHigh }]}>
              <MaterialIcons name="language" size={20} color={colors.primary} />
            </View>
            <View style={styles.rowTextWrap}>
              <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{t('settings.language')}</Text>
              <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 1 }]}>{currentLanguageLabel}</Text>
            </View>
            <MaterialIcons name="chevron-right" size={22} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
        </View>

        {/* Sections Support + Légal */}
        {sections.slice(2).map((section) => (
          <View key={section.title}>
            <Text style={[typo.overline, styles.sectionTitle, { color: colors.secondary }]}>{section.title}</Text>
            <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              {section.rows.map((row, i) => renderRow(row, i === section.rows.length - 1))}
            </View>
          </View>
        ))}

        {/* Déconnexion — même Modal partagé que le profil */}
        <TouchableOpacity style={[styles.logoutBtn, { borderColor: colors.error }]} onPress={() => setShowLogoutModal(true)}>
          <MaterialIcons name="logout" size={20} color={colors.error} />
          <Text style={[typo.labelLarge, { color: colors.error, marginLeft: 8 }]}>
            {t('settings.logout')}
          </Text>
        </TouchableOpacity>

        <Text style={[typo.labelSmall, styles.version, { color: colors.onSurfaceVariant }]}>
          Frollot · Version 1.0.0
        </Text>
      </ScrollView>

      <LogoutConfirmModal visible={showLogoutModal} onClose={() => setShowLogoutModal(false)} />

      {/* Language selector modal */}
      <Modal visible={showLanguageModal} transparent animationType="fade" onRequestClose={() => setShowLanguageModal(false)}>
        <Pressable style={styles.modalOverlay} onPress={() => setShowLanguageModal(false)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[typo.overline, { color: colors.secondary, textAlign: 'center' }]}>Apparence</Text>
            <Text style={[typo.headlineSmall, { color: colors.onSurface, textAlign: 'center', marginBottom: 16 }]}>
              {t('settings.language')}
            </Text>
            {LANGUAGES.map((lang) => {
              const on = language === lang.code;
              return (
                <TouchableOpacity
                  key={lang.code}
                  style={[styles.langOption, {
                    backgroundColor: on ? colors.primaryContainer : 'transparent', // design-fixed — transparent au repos
                    borderColor: on ? colors.primary : colors.outlineVariant,
                  }]}
                  onPress={async () => {
                    await setLanguage(lang.code);
                    setShowLanguageModal(false);
                  }}
                >
                  <Text style={[typo.titleSmall, { color: on ? colors.onPrimaryContainer : colors.onSurface, flex: 1 }]}>
                    {lang.label}
                  </Text>
                  {on && <MaterialIcons name="check-circle" size={20} color={colors.primary} />}
                </TouchableOpacity>
              );
            })}
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 48 },
  // Identity
  identityCard: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    borderRadius: 20, borderWidth: 1, padding: 16, marginTop: 20,
  },
  identityAvatar: { width: 52, height: 52, borderRadius: 26, alignItems: 'center', justifyContent: 'center' },
  // Sections
  sectionTitle: { marginTop: 28, marginBottom: 10, marginLeft: 4 },
  card: { borderRadius: 20, borderWidth: 1, overflow: 'hidden' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 14, paddingHorizontal: 16 },
  rowDisabled: { opacity: 0.55 },
  rowIconCircle: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  rowTextWrap: { flex: 1 },
  separator: { height: StyleSheet.hairlineWidth, marginLeft: 68 },
  soonBadge: { paddingVertical: 4, paddingHorizontal: 10, borderRadius: 999 },
  // Theme segment
  themeSegment: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingBottom: 14 },
  themeOption: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    gap: 6, paddingVertical: 10, borderRadius: 12, borderWidth: 1,
  },
  // Logout
  logoutBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    height: 52, borderRadius: 28, borderWidth: 1, marginTop: 32,
  },
  // Version
  version: { textAlign: 'center', marginTop: 24 },
  // Modal
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  modalCard: { width: '100%', maxWidth: 340, borderRadius: 24, padding: 24 },
  langOption: {
    flexDirection: 'row', alignItems: 'center',
    borderRadius: 14, borderWidth: 1, paddingVertical: 12, paddingHorizontal: 16, marginBottom: 8,
  },
});
