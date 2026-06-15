/*
 * S7 — Contacter le support
 *
 * Écran fonctionnel sans API backend : ouverture du client e-mail via Linking
 * (mailto:) avec objet pré-catégorisé. Les délais annoncés pour les signalements
 * (accusé de réception 24 h, décision 72 h) sont alignés sur l'ordonnance
 * n°0011/PR/2026 (cf. sources détaillées en tête de terms.tsx). La catégorie
 * « Données personnelles » oriente vers la procédure d'exercice des droits décrite
 * à la section 8 de privacy.tsx (loi n°001/2011 modifiée par loi n°025/2023, APDPVP).
 *
 * À FAIRE AVANT PRODUCTION (Olsen) :
 *  1. Ouvrir réellement les boîtes support@frollot.ga et privacy@frollot.ga
 *     (ou remplacer par les adresses définitives, ici ET dans terms/privacy/help).
 *  2. Confirmer les délais de réponse annoncés pour les demandes générales (48 h
 *     ouvrées) : engagement de service interne, à ajuster à la capacité réelle.
 */
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Linking } from 'react-native';
import { router } from 'expo-router';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';

const SUPPORT_EMAIL = 'support@frollot.ga';
const PRIVACY_EMAIL = 'privacy@frollot.ga';

interface ContactTopic {
  icon: keyof typeof MaterialIcons.glyphMap;
  title: string;
  desc: string;
  email: string;
  subject: string;
  /** Note affichée sous la carte (délai, renvoi procédure). */
  note?: string;
}

const TOPICS: ContactTopic[] = [
  {
    icon: 'person-outline',
    title: 'Compte et connexion',
    desc: 'Accès au compte, e-mail, mot de passe, suppression de compte.',
    email: SUPPORT_EMAIL,
    subject: '[Compte] ',
  },
  {
    icon: 'event-available',
    title: 'Réservations',
    desc: "Problème avec un rendez-vous, une file d'attente ou un salon.",
    email: SUPPORT_EMAIL,
    subject: '[Réservation] ',
  },
  {
    icon: 'workspace-premium',
    title: 'Premium et paiement',
    desc: 'Souscription, paiement Airtel Money / Moov Money, remboursement 48 h. Joignez la référence de votre transaction.',
    email: SUPPORT_EMAIL,
    subject: '[Premium/Paiement] ',
  },
  {
    icon: 'flag',
    title: 'Signaler un contenu ou un compte',
    desc: "Harcèlement, contenu illicite, usurpation d'identité. Utilisez d'abord le bouton « Signaler » du post ou du profil, puis complétez ici si besoin.",
    email: SUPPORT_EMAIL,
    subject: '[Signalement] ',
    note: 'Signalements de harcèlement : accusé de réception sous 24 h, décision notifiée sous 72 h.',
  },
  {
    icon: 'privacy-tip',
    title: 'Données personnelles (APDPVP)',
    desc: "Exercice de vos droits : accès, rectification, effacement, limitation, portabilité, opposition. Justifiez de votre identité dans votre message.",
    email: PRIVACY_EMAIL,
    subject: '[Droits données personnelles] ',
    note: "La procédure complète et le recours auprès de l'APDPVP sont décrits à la section 8 de la Politique de confidentialité.",
  },
  {
    icon: 'build',
    title: 'Problème technique',
    desc: "Bug, écran qui ne charge pas, notifications. Précisez votre appareil et joignez une capture d'écran si possible.",
    email: SUPPORT_EMAIL,
    subject: '[Technique] ',
  },
];

export default function ContactScreen() {
  const { colors, typography: typo } = useTheme();

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  const openMail = (topic: ContactTopic) => {
    const url = `mailto:${topic.email}?subject=${encodeURIComponent(topic.subject)}`;
    Linking.openURL(url).catch(() => {
      // Pas de client mail configuré : l'adresse reste visible à l'écran, rien à faire de plus.
    });
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name="arrow-back" size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <Text style={[typo.overline, { color: colors.secondary }]}>Support</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>Contacter le support</Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
          Choisissez le sujet de votre demande : votre message sera pré-adressé à la bonne équipe. Nous répondons aux
          demandes générales sous 48 heures ouvrées.
        </Text>

        {TOPICS.map((topic) => (
          <View key={topic.title}>
            <TouchableOpacity
              style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}
              activeOpacity={0.7}
              onPress={() => openMail(topic)}
            >
              <View style={[styles.cardIcon, { backgroundColor: colors.surfaceContainerHigh }]}>
                <MaterialIcons name={topic.icon} size={20} color={colors.primary} />
              </View>
              <View style={styles.cardText}>
                <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{topic.title}</Text>
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 2 }]}>{topic.desc}</Text>
              </View>
              <MaterialIcons name="mail-outline" size={20} color={colors.primary} />
            </TouchableOpacity>
            {topic.note && (
              <View style={styles.noteRow}>
                <MaterialIcons name="info-outline" size={14} color={colors.secondary} />
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, flex: 1 }]}>{topic.note}</Text>
              </View>
            )}
          </View>
        ))}

        {/* Coordonnées directes */}
        <View style={[styles.directCard, { backgroundColor: colors.surfaceContainer }]}>
          <Text style={[typo.titleSmall, { color: colors.onSurface }]}>Nous écrire directement</Text>
          <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 6 }]}>
            Support général : {SUPPORT_EMAIL}{'\n'}
            Données personnelles : {PRIVACY_EMAIL}{'\n'}
            Frollot — Libreville, République Gabonaise
          </Text>
        </View>

        <TouchableOpacity style={styles.helpLink} activeOpacity={0.6} onPress={() => router.push('/settings/help' as never)}>
          <MaterialIcons name="help-outline" size={18} color={colors.primary} />
          <Text style={[typo.bodyMedium, { color: colors.primary }]}>
            Consultez d'abord le Centre d'aide : votre réponse s'y trouve peut-être déjà.
          </Text>
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 64 },
  card: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    borderRadius: 20, borderWidth: 1, padding: 16, marginTop: 14,
  },
  cardIcon: { width: 38, height: 38, borderRadius: 19, alignItems: 'center', justifyContent: 'center' },
  cardText: { flex: 1 },
  noteRow: { flexDirection: 'row', alignItems: 'flex-start', gap: 6, marginTop: 6, paddingHorizontal: 8 },
  directCard: { borderRadius: 20, padding: 16, marginTop: 28 },
  helpLink: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 24, paddingHorizontal: 4 },
});
