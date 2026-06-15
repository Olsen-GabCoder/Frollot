/*
 * S7 — Centre d'aide (FAQ produit)
 *
 * SOURCES : contenu produit rédigé d'après les fonctionnalités réelles de l'app
 * (réservations, file d'attente, posts/collections/archives/signalement, Premium
 * 10 000 FCFA via Airtel Money / Moov Money — décisions commerciales validées :
 * non-reconduction + garantie de remboursement 48 h, cf. terms.tsx art. 10-12).
 * Les délais de traitement des signalements (24 h / 72 h) sont alignés sur
 * l'ordonnance n°0011/PR/2026 (cf. sources détaillées en tête de terms.tsx).
 *
 * À FAIRE AVANT PRODUCTION (Olsen) :
 *  1. Remplacer « support@frollot.ga » par l'adresse de support réellement ouverte.
 *  2. Relire les pas-à-pas Premium/Airtel/Moov une fois le flux de paiement réellement
 *     implémenté (l'écran de souscription Premium n'existe pas encore) et ajuster
 *     les intitulés de boutons aux écrans définitifs.
 */
import { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';

interface FaqItem {
  q: string;
  a: string;
}

interface FaqCategory {
  id: string;
  icon: keyof typeof MaterialIcons.glyphMap;
  title: string;
  items: FaqItem[];
}

const CATEGORIES: FaqCategory[] = [
  {
    id: 'compte',
    icon: 'person-outline',
    title: 'Compte et connexion',
    items: [
      {
        q: 'Comment créer un compte ?',
        a: "Depuis l'écran d'accueil, touchez « S'inscrire », renseignez vos informations d'identité, votre adresse e-mail et un mot de passe, puis validez. Un compte est strictement personnel : indiquez des informations exactes, c'est une exigence de la réglementation gabonaise des plateformes sociales. L'accès aux fonctionnalités sociales est réservé aux personnes d'au moins 16 ans.",
      },
      {
        q: "J'ai oublié mon mot de passe.",
        a: "Sur l'écran de connexion, touchez « Mot de passe oublié », saisissez l'adresse e-mail de votre compte et suivez le lien de réinitialisation reçu par e-mail. Le lien est à usage unique et expire rapidement : si c'est le cas, recommencez la procédure.",
      },
      {
        q: 'Comment changer mon adresse e-mail ?',
        a: 'Allez dans Réglages > Compte > Adresse e-mail. La modification est confirmée par un code envoyé à la nouvelle adresse afin de vérifier que vous y avez bien accès.',
      },
      {
        q: 'Comment supprimer mon compte ?',
        a: "Vous pouvez supprimer votre compte à tout moment depuis les réglages, ou en écrivant à support@frollot.ga. La suppression entraîne celle de vos contenus et de vos données dans les conditions détaillées par notre Politique de confidentialité (Réglages > Légal).",
      },
    ],
  },
  {
    id: 'reservations',
    icon: 'event-available',
    title: "Réservations et file d'attente",
    items: [
      {
        q: 'Comment réserver une prestation ?',
        a: "Ouvrez la fiche d'un salon, touchez « Réserver une prestation », choisissez la prestation, le coiffeur et le créneau, puis confirmez. Le récapitulatif de votre rendez-vous reste accessible dans votre historique de réservations.",
      },
      {
        q: 'Comment annuler ou déplacer un rendez-vous ?',
        a: "Ouvrez la réservation concernée depuis votre historique et choisissez « Annuler » ou sélectionnez un nouveau créneau. Par respect pour les professionnels, annulez dès que possible : des absences répétées sans annulation peuvent restreindre votre accès à la réservation.",
      },
      {
        q: "À quoi sert la file d'attente ?",
        a: "Certains salons fonctionnent aussi sans rendez-vous. La file d'attente vous permet de prendre votre tour à distance : vous voyez votre position évoluer en temps réel et vous vous présentez au salon au bon moment.",
      },
      {
        q: 'Le prix de la prestation se paie-t-il dans l\'application ?',
        a: "Non. Le prix de la prestation est payé directement au salon, selon les modalités convenues avec lui. Frollot est un intermédiaire de mise en relation : le contrat de prestation est conclu entre vous et le professionnel. Seul l'abonnement Premium se paie dans l'application.",
      },
    ],
  },
  {
    id: 'social',
    icon: 'forum',
    title: 'Réseau social : posts, collections, archives',
    items: [
      {
        q: 'Que puis-je publier ?',
        a: "Des photos et vidéos de réalisations, des avant/après, des tendances, des conseils et des inspirations. Vous devez disposer des droits sur ce que vous publiez, et notamment de l'accord des personnes reconnaissables. Les règles complètes figurent dans nos Conditions (Réglages > Légal). Les contenus créés avec une intelligence artificielle doivent être signalés comme tels.",
      },
      {
        q: 'À quoi servent les collections ?',
        a: "Les collections vous permettent d'organiser les publications que vous avez enregistrées : touchez le menu « ... » d'un post puis « Ajouter à une collection ». Le marque-page enregistre un post dans vos favoris.",
      },
      {
        q: "Qu'est-ce que l'archivage d'un post ?",
        a: "Archiver l'un de vos posts le retire de votre fil sans le supprimer. Vous retrouvez vos posts archivés depuis votre profil et pouvez les restaurer à tout moment.",
      },
      {
        q: 'Comment signaler un contenu ou un compte ?',
        a: "Touchez le menu « ... » du post ou du profil concerné, puis « Signaler » et choisissez un motif. Pour les signalements de harcèlement, nous accusons réception sous 24 heures et nous vous notifions notre décision sous 72 heures, conformément à la réglementation gabonaise. En cas d'urgence, écrivez aussi à support@frollot.ga.",
      },
    ],
  },
  {
    id: 'premium',
    icon: 'workspace-premium',
    title: 'Premium et paiement',
    items: [
      {
        q: 'Que comprend Frollot Premium et combien coûte-t-il ?',
        a: "Frollot Premium donne accès aux fonctionnalités étendues présentées sur l'écran de souscription. Son prix est de 10 000 FCFA toutes taxes comprises (TVA 18 % incluse) pour la durée indiquée au moment de la souscription. L'abonnement ne se renouvelle jamais automatiquement : à l'échéance, il s'arrête de lui-même et vous décidez librement de souscrire à nouveau.",
      },
      {
        q: 'Comment payer avec Airtel Money ?',
        a: "Sur l'écran de souscription, choisissez Airtel Money et indiquez le numéro de téléphone associé à votre compte Airtel Money. Vous recevez alors une demande de paiement sur votre téléphone : validez-la avec votre code secret Airtel Money. Une fois la transaction confirmée par l'opérateur, votre Premium est activé immédiatement. Frollot ne vous demandera jamais votre code secret : il se saisit uniquement sur l'interface de votre opérateur.",
      },
      {
        q: 'Comment payer avec Moov Money ?',
        a: "Sur l'écran de souscription, choisissez Moov Money et indiquez le numéro associé à votre compte Moov Money. Validez ensuite la demande de paiement reçue sur votre téléphone avec votre code secret Moov Money. L'activation du Premium est immédiate après confirmation de l'opérateur. Là encore, votre code secret n'est jamais saisi dans Frollot.",
      },
      {
        q: 'Le paiement a échoué ou a été débité sans activer le Premium.',
        a: "En cas d'échec (solde insuffisant, délai de validation expiré, incident réseau), rien n'est dû : recommencez simplement l'opération. Si votre compte Mobile Money a été débité sans que le Premium soit activé, écrivez à support@frollot.ga en joignant la référence de la transaction (visible dans l'historique de votre opérateur) : nous régularisons après vérification.",
      },
      {
        q: 'Puis-je me faire rembourser ?',
        a: "Oui : Frollot offre une garantie de remboursement de 48 heures. Si vous n'avez pas utilisé les fonctionnalités Premium, vous pouvez demander le remboursement intégral dans les 48 heures suivant le paiement, à support@frollot.ga, avec la référence de votre transaction. Le remboursement est effectué sur le compte Mobile Money ayant servi au paiement. Les détails figurent à l'article 12 de nos Conditions.",
      },
    ],
  },
  {
    id: 'confidentialite',
    icon: 'privacy-tip',
    title: 'Confidentialité et données personnelles',
    items: [
      {
        q: 'Quelles données collectez-vous et pourquoi ?',
        a: "Uniquement les données nécessaires au service : identité, profil, contenus publiés, réservations, références de transactions Premium et données techniques de sécurité. Le détail complet — finalités, durées, destinataires — figure dans notre Politique de confidentialité (Réglages > Légal > Politique de confidentialité).",
      },
      {
        q: 'Stockez-vous mes informations Mobile Money ?',
        a: "Non. Votre code secret n'est jamais saisi dans Frollot et nous n'y avons jamais accès. Nous ne conservons que le montant, la date, le statut et la référence de la transaction, transmis par l'opérateur, pour activer votre abonnement et traiter les réclamations.",
      },
      {
        q: 'Comment exercer mes droits (accès, rectification, suppression...) ?',
        a: "La loi gabonaise sur la protection des données vous reconnaît des droits d'accès, de rectification, d'effacement, de limitation, de portabilité et d'opposition. Vous pouvez les exercer depuis les réglages de votre compte ou par e-mail — la marche à suivre, ainsi que le recours auprès de l'autorité de contrôle (APDPVP), sont décrits à la section 8 de la Politique de confidentialité.",
      },
    ],
  },
  {
    id: 'technique',
    icon: 'build',
    title: 'Problèmes techniques',
    items: [
      {
        q: "L'application ne se charge pas ou s'affiche mal.",
        a: "Vérifiez votre connexion internet, fermez complètement l'application puis rouvrez-la. Si le problème persiste, vérifiez qu'une mise à jour est disponible sur votre magasin d'applications et installez-la.",
      },
      {
        q: 'Je ne reçois pas les e-mails de Frollot.',
        a: "Vérifiez votre dossier de courrier indésirable et que l'adresse de votre compte est correcte (Réglages > Compte). Ajoutez nos adresses à vos contacts pour fiabiliser la réception.",
      },
      {
        q: 'Comment signaler un bug ?',
        a: "Écrivez-nous via l'écran « Contacter le support » (Réglages > Support) en décrivant le problème, votre appareil et, si possible, une capture d'écran. Chaque signalement est lu et nous y répondons dans les meilleurs délais.",
      },
    ],
  },
];

export default function HelpScreen() {
  const { colors, typography: typo } = useTheme();
  const [openId, setOpenId] = useState<string | null>(null);

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  const toggle = (key: string) => setOpenId((cur) => (cur === key ? null : key));

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name="arrow-back" size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <Text style={[typo.overline, { color: colors.secondary }]}>Support</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>Centre d'aide</Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
          Les réponses aux questions les plus fréquentes sur Frollot. Vous ne trouvez pas la vôtre ? Notre équipe vous
          répond via l'écran « Contacter le support ».
        </Text>

        {CATEGORIES.map((cat) => (
          <View key={cat.id}>
            <View style={styles.catHeader}>
              <View style={[styles.catIcon, { backgroundColor: colors.surfaceContainerHigh }]}>
                <MaterialIcons name={cat.icon} size={18} color={colors.primary} />
              </View>
              <Text style={[typo.titleMedium, { color: colors.onBackground }]}>{cat.title}</Text>
            </View>
            <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              {cat.items.map((item, i) => {
                const key = `${cat.id}-${i}`;
                const open = openId === key;
                return (
                  <View key={key}>
                    <TouchableOpacity style={styles.qRow} activeOpacity={0.6} onPress={() => toggle(key)}>
                      <Text style={[typo.titleSmall, { color: colors.onSurface, flex: 1 }]}>{item.q}</Text>
                      <MaterialIcons
                        name={open ? 'expand-less' : 'expand-more'}
                        size={22}
                        color={open ? colors.primary : colors.onSurfaceVariant}
                      />
                    </TouchableOpacity>
                    {open && (
                      <Text style={[typo.bodyMedium, styles.answer, { color: colors.onSurfaceVariant }]}>{item.a}</Text>
                    )}
                    {i < cat.items.length - 1 && (
                      <View style={[styles.separator, { backgroundColor: colors.outlineVariant }]} />
                    )}
                  </View>
                );
              })}
            </View>
          </View>
        ))}

        {/* Liens croisés */}
        <View style={[styles.linksCard, { backgroundColor: colors.surfaceContainer }]}>
          <Text style={[typo.titleSmall, { color: colors.onSurface }]}>Pour aller plus loin</Text>
          <TouchableOpacity style={styles.linkRow} activeOpacity={0.6} onPress={() => router.push('/settings/contact' as never)}>
            <MaterialIcons name="support-agent" size={18} color={colors.primary} />
            <Text style={[typo.bodyMedium, { color: colors.primary, flex: 1 }]}>Contacter le support</Text>
            <MaterialIcons name="chevron-right" size={18} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.linkRow} activeOpacity={0.6} onPress={() => router.push('/settings/terms' as never)}>
            <MaterialIcons name="description" size={18} color={colors.primary} />
            <Text style={[typo.bodyMedium, { color: colors.primary, flex: 1 }]}>Conditions Générales d'Utilisation et de Vente</Text>
            <MaterialIcons name="chevron-right" size={18} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.linkRow} activeOpacity={0.6} onPress={() => router.push('/settings/privacy' as never)}>
            <MaterialIcons name="privacy-tip" size={18} color={colors.primary} />
            <Text style={[typo.bodyMedium, { color: colors.primary, flex: 1 }]}>Politique de confidentialité</Text>
            <MaterialIcons name="chevron-right" size={18} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 64 },
  catHeader: { flexDirection: 'row', alignItems: 'center', gap: 10, marginTop: 28, marginBottom: 10, marginLeft: 4 },
  catIcon: { width: 32, height: 32, borderRadius: 16, alignItems: 'center', justifyContent: 'center' },
  card: { borderRadius: 20, borderWidth: 1, overflow: 'hidden' },
  qRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 14, paddingHorizontal: 16 },
  answer: { paddingHorizontal: 16, paddingBottom: 14, lineHeight: 22 },
  separator: { height: StyleSheet.hairlineWidth, marginLeft: 16 },
  linksCard: { borderRadius: 20, padding: 16, marginTop: 32, gap: 4 },
  linkRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 10 },
});
