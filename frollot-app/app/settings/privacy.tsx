/*
 * S7 — Politique de confidentialité
 *
 * SOURCES RÉELLEMENT CONSULTÉES (recherche du 2026-06-12) :
 *  - Loi n°025/2023 du 12 juillet 2023 portant modification de la loi n°001/2011 du
 *    25 septembre 2011 relative à la protection des données à caractère personnel —
 *    texte intégral lu au JO n°218 Bis du 15/07/2023 :
 *    https://www.afapdp.org/wp-content/uploads/2023/12/Gabon-Loi-025-2023-du-12-juillet-2023-portant-modification-de-la-loi-001-2011-du-25-septembre-relative-a-la-protection-des-donnees-a-caractere-personnel.pdf
 *    Articles cités VÉRIFIÉS dans le texte : art. 7 (création APDPVP, autorité
 *    administrative indépendante) ; art. 8 (réclamations auprès de l'APDPVP) ;
 *    art. 43-44 (droit d'accès, gratuit) ; art. 50-53 (rectification, effacement) ;
 *    art. 55 (limitation) ; art. 58-59 (portabilité) ; art. 60-62 (opposition, dont
 *    prospection) ; art. 66 (décision automatisée / profilage) ; art. 70 (licéité :
 *    loyauté, finalités, minimisation, exactitude, conservation limitée).
 *  - Ordonnance n°0011/PR/2026 du 26/02/2026 (réseaux sociaux, majorité numérique 16 ans,
 *    identification des utilisateurs) : https://journal-officiel.ga/22404-0011-pr-2026-/
 *  - Règlement n°04/18/CEMAC/UMAC/COBAC du 21/12/2018 (services de paiement CEMAC) :
 *    https://www.beac.int/wp-content/uploads/2019/07/REGLEMENT-N-04-18-CEMAC-UMAC-COBAC-du-21-d%C3%A9cembre-2018.pdf
 *  - APDPVP (ex-CNPDCP) : https://www.cnpdcp.ga/
 *
 * À FAIRE AVANT PRODUCTION (Olsen) :
 *  1. Remplacer l'identité du responsable de traitement (section 1) : forme sociale,
 *     RCCM, NIF, adresse exacte du siège ; désigner la personne référente (DPO).
 *  2. Remplacer « privacy@frollot.ga » / « support@frollot.ga » par les adresses réelles.
 *  3. Accomplir les formalités préalables auprès de l'APDPVP (déclaration / demande
 *     d'autorisation selon les traitements — exigées par la loi) AVANT le lancement,
 *     et reporter ici le numéro de récépissé.
 *  4. Faire valider par un juriste : durées de conservation chiffrées (proposées ici à
 *     titre de politique interne, pas imposées par un texte vérifié), encadrement des
 *     transferts hors Gabon (clauses contractuelles types selon l'hébergeur réellement
 *     choisi), et modalités de collecte du NIP (ordonnance n°0011/PR/2026 — connues via
 *     doctrine, à confirmer au JO n°110).
 */
import { useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';

interface LegalSection {
  id: string;
  title: string;
  body: string[];
}

const UPDATED_AT = 'Dernière mise à jour : 12 juin 2026';

const SECTIONS: LegalSection[] = [
  {
    id: 'responsable',
    title: '1. Responsable du traitement et cadre légal',
    body: [
      "La présente politique décrit la manière dont Frollot, dont le siège est établi à Libreville, République Gabonaise, collecte et traite les données à caractère personnel des utilisateurs de son application de réservation et de réseau social dédiés à la coiffure.",
      "Frollot agit en qualité de responsable du traitement au sens de la loi n°001/2011 du 25 septembre 2011 relative à la protection des données à caractère personnel, telle que modifiée par la loi n°025/2023 du 12 juillet 2023.",
      "L'autorité de contrôle compétente est l'Autorité pour la Protection des Données Personnelles et de la Vie Privée (APDPVP), autorité administrative indépendante instituée par l'article 7 de la loi n°025/2023. Frollot accomplit auprès de l'APDPVP les formalités préalables requises pour ses traitements.",
      "Pour toute question relative à vos données : privacy@frollot.ga.",
    ],
  },
  {
    id: 'donnees',
    title: '2. Données que nous collectons',
    body: [
      "Données d'identification : nom, prénom, adresse électronique, numéro de téléphone, ainsi que les éléments d'identification requis par la réglementation gabonaise applicable aux plateformes sociales, dont le numéro d'identification personnel lorsque la loi l'exige.",
      "Données de profil : photographie de profil, biographie, type de compte (client ou professionnel), préférences de langue et d'apparence.",
      "Contenus publiés : photographies, vidéos, publications, commentaires, avis, collections, réactions, ainsi que les signalements que vous effectuez.",
      "Données de réservation : prestations réservées, dates et créneaux, salon ou coiffeur concerné, historique des rendez-vous et présence en file d'attente.",
      "Données de transaction Premium : montant, date, statut et référence de la transaction (voir section 3).",
      "Données techniques : identifiants de connexion, journaux techniques, type d'appareil, version de l'application, données nécessaires à la sécurité du service.",
      "Conformément au principe de minimisation posé par l'article 70 de la loi, nous ne collectons que les données adéquates, pertinentes et non excessives au regard des finalités décrites à la section 4.",
    ],
  },
  {
    id: 'paiement',
    title: '3. Données de paiement Mobile Money',
    body: [
      "Le paiement de l'abonnement Premium s'effectue via Airtel Money ou Moov Money. La transaction est exécutée par l'opérateur de Mobile Money, prestataire de services de paiement agréé dans le cadre du règlement n°04/18/CEMAC/UMAC/COBAC du 21 décembre 2018 relatif aux services de paiement dans la CEMAC.",
      "Frollot ne collecte, ne stocke et ne voit jamais votre code confidentiel (PIN) Mobile Money. La validation du paiement s'effectue exclusivement entre vous et votre opérateur.",
      "Les seules données de paiement conservées par Frollot sont : le montant, la date, le statut de la transaction et la référence communiquée par l'opérateur. Ces données servent à activer votre abonnement, à établir votre justificatif et à traiter les réclamations et remboursements.",
      "Le traitement de vos données par l'opérateur de Mobile Money est régi par la politique de confidentialité propre à cet opérateur.",
    ],
  },
  {
    id: 'finalites',
    title: '4. Finalités et bases légales',
    body: [
      "Conformément à l'article 70 de la loi, vos données sont collectées pour des finalités déterminées, explicites et légitimes, et ne sont pas traitées ultérieurement de manière incompatible avec ces finalités :",
      "• fourniture du service (création du compte, réservations, réseau social, Premium) — base : l'exécution du contrat qui nous lie ;",
      "• identification des utilisateurs de la plateforme sociale et traitement des signalements — base : le respect de nos obligations légales, notamment celles issues de l'ordonnance n°0011/PR/2026 ;",
      "• facturation, comptabilité et traitement des remboursements — base : nos obligations légales et l'exécution du contrat ;",
      "• sécurité du service, prévention de la fraude et des abus — base : notre intérêt légitime à protéger la plateforme et ses utilisateurs ;",
      "• communications commerciales et mises en avant personnalisées — base : votre consentement, que vous pouvez retirer à tout moment ;",
      "• amélioration du service au moyen de statistiques d'usage — base : notre intérêt légitime, sur des données agrégées dans toute la mesure du possible.",
      "Aucune décision produisant des effets juridiques à votre égard n'est fondée exclusivement sur un traitement automatisé ; vous disposez en toute hypothèse des garanties prévues à l'article 66 de la loi.",
    ],
  },
  {
    id: 'durees',
    title: '5. Durées de conservation',
    body: [
      "Conformément à l'article 70 de la loi, vos données sont conservées sous une forme permettant votre identification pendant une durée n'excédant pas celle nécessaire aux finalités poursuivies. À titre de politique générale :",
      "• les données de compte et de profil sont conservées pendant la durée de vie du compte, puis supprimées ou anonymisées dans un délai maximal de douze mois après sa suppression ;",
      "• les contenus publiés sont supprimés avec le compte, sous réserve des copies techniques temporaires ;",
      "• les données de réservation sont conservées pendant la durée de vie du compte aux fins d'historique, puis supprimées ou anonymisées ;",
      "• les données de transaction Premium sont conservées pendant la durée requise par les obligations comptables et fiscales en vigueur en République Gabonaise ;",
      "• les journaux techniques de sécurité sont conservés pour une durée courte et proportionnée, puis supprimés.",
      "Les données faisant l'objet d'un signalement ou nécessaires à la constatation, à l'exercice ou à la défense d'un droit en justice peuvent être conservées le temps de la procédure.",
    ],
  },
  {
    id: 'destinataires',
    title: '6. Destinataires et sous-traitants',
    body: [
      "Vos données sont traitées par le personnel habilité de Frollot, dans la limite de ses attributions. Elles peuvent être communiquées aux catégories de destinataires suivantes :",
      "• nos sous-traitants techniques (hébergement, envoi d'e-mails, notifications), qui agissent sur nos instructions et sont tenus contractuellement à la confidentialité et à la sécurité ;",
      "• les opérateurs de Mobile Money, pour l'exécution des paiements Premium (section 3) ;",
      "• les professionnels (salons et coiffeurs) auprès desquels vous réservez, qui reçoivent les informations strictement nécessaires à la prestation : votre nom, la prestation et le créneau réservés ;",
      "• les autorités judiciaires et administratives gabonaises, lorsque la loi nous y oblige.",
      "Frollot ne vend ni ne loue vos données personnelles à des tiers.",
    ],
  },
  {
    id: 'transferts',
    title: '7. Transferts de données hors du Gabon',
    body: [
      "Certains de nos sous-traitants techniques peuvent être établis hors de la République Gabonaise, notamment pour l'hébergement de l'application.",
      "Lorsqu'un transfert de données vers un pays tiers est nécessaire, Frollot l'encadre conformément à la législation gabonaise sur la protection des données personnelles, notamment au moyen de clauses contractuelles garantissant un niveau de protection adéquat, et accomplit les formalités requises auprès de l'APDPVP.",
      "Conformément à la loi, vous avez le droit d'être informé des garanties entourant tout transfert de vos données vers un pays tiers ; vous pouvez exercer ce droit à l'adresse privacy@frollot.ga.",
    ],
  },
  {
    id: 'droits',
    title: '8. Vos droits',
    body: [
      "La loi n°001/2011 modifiée par la loi n°025/2023 vous reconnaît les droits suivants, que vous pouvez exercer gratuitement :",
      "• droit d'accès (articles 43 et 44) : obtenir la confirmation que vos données sont traitées, ainsi qu'une copie de celles-ci et des informations sur les finalités, les destinataires, la durée de conservation et l'origine des données ;",
      "• droit de rectification (articles 50 à 52) : faire rectifier, compléter ou mettre à jour les données inexactes, incomplètes ou obsolètes ;",
      "• droit à l'effacement (article 53) : obtenir, dans les meilleurs délais, la suppression de vos données, notamment lorsqu'elles ne sont plus nécessaires, lorsque vous retirez votre consentement ou lorsque le traitement est illicite ;",
      "• droit à la limitation du traitement (article 55) ;",
      "• droit à la portabilité (articles 58 et 59) : recevoir les données que vous nous avez fournies dans un format structuré, couramment utilisé et lisible par machine, et les transmettre à un autre responsable de traitement ;",
      "• droit d'opposition (articles 60 à 62) : vous opposer à tout moment, pour des raisons tenant à votre situation particulière, à un traitement de vos données, et vous opposer sans condition à tout traitement à des fins de prospection.",
      "Pour exercer ces droits : depuis l'application (réglages du compte) ou par écrit à privacy@frollot.ga, en justifiant de votre identité. Nous répondons dans les meilleurs délais.",
      "Si vous estimez que vos droits ne sont pas respectés, vous pouvez introduire une réclamation auprès de l'Autorité pour la Protection des Données Personnelles et de la Vie Privée (APDPVP), compétente pour recevoir les réclamations, pétitions et plaintes en vertu de l'article 8 de la loi.",
    ],
  },
  {
    id: 'mineurs',
    title: '9. Mineurs',
    body: [
      "Conformément à la majorité numérique fixée à seize ans par l'ordonnance n°0011/PR/2026 du 26 février 2026, l'accès aux fonctionnalités sociales de Frollot est réservé aux personnes d'au moins seize ans ; en dessous de cet âge, une autorisation parentale est requise dans les conditions prévues par la réglementation.",
      "Frollot ne collecte pas sciemment de données relatives à des mineurs en violation de ces règles. Si nous constatons qu'un compte a été créé en méconnaissance de la réglementation applicable aux mineurs, le compte est suspendu et les données collectées sont supprimées.",
      "Tout parent ou titulaire de l'autorité parentale qui pense qu'un mineur utilise l'application sans autorisation peut nous contacter à privacy@frollot.ga afin que nous prenions les mesures nécessaires.",
    ],
  },
  {
    id: 'cookies',
    title: '10. Cookies, traceurs et publicité',
    body: [
      "L'application utilise des traceurs strictement nécessaires à son fonctionnement : maintien de la session, préférences de langue et de thème, sécurité. Ces traceurs ne requièrent pas de consentement.",
      "Tout traceur non strictement nécessaire — mesure d'audience non anonymisée, personnalisation publicitaire — n'est déposé qu'avec votre consentement préalable, recueilli de manière libre, spécifique et éclairée, conformément à la législation gabonaise sur la protection des données. Vous pouvez retirer ce consentement à tout moment depuis les réglages.",
      "Si des publicités ciblées ou personnalisées venaient à être proposées dans l'application, elles reposeraient exclusivement sur votre consentement, et vous conserveriez la possibilité de vous y opposer sans condition, conformément au droit d'opposition à la prospection prévu par la loi.",
    ],
  },
  {
    id: 'securite',
    title: '11. Sécurité',
    body: [
      "Frollot met en œuvre des mesures techniques et organisationnelles appropriées pour protéger vos données contre la destruction, la perte, l'altération, la divulgation ou l'accès non autorisés : chiffrement des communications, stockage sécurisé des mots de passe, authentification par jetons à durée limitée, contrôle des accès internes selon le principe du moindre privilège, journalisation des opérations sensibles.",
      "En cas de violation de données personnelles susceptible d'engendrer un risque pour vos droits et libertés, Frollot prend sans délai les mesures correctrices nécessaires et procède aux notifications requises par la législation gabonaise, auprès de l'APDPVP et, le cas échéant, des personnes concernées.",
    ],
  },
  {
    id: 'modifications',
    title: '12. Modifications et contact',
    body: [
      "La présente politique peut être mise à jour pour refléter les évolutions du service ou de la réglementation. Toute modification substantielle vous est notifiée dans l'application avant son entrée en vigueur. La date de dernière mise à jour figure en tête du document.",
      "Pour toute question relative à la présente politique ou à vos données personnelles : privacy@frollot.ga. Pour toute autre demande : support@frollot.ga, ou l'écran « Contacter le support » accessible depuis les réglages.",
    ],
  },
];

export default function PrivacyScreen() {
  const { colors, typography: typo } = useTheme();
  const scrollRef = useRef<ScrollView>(null);
  const sectionY = useRef<Record<string, number>>({});

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/settings'));

  const scrollToSection = (id: string) => {
    const y = sectionY.current[id];
    if (y != null) scrollRef.current?.scrollTo({ y: Math.max(y - 12, 0), animated: true });
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name="arrow-back" size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <ScrollView ref={scrollRef} contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <Text style={[typo.overline, { color: colors.secondary }]}>Légal</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>
          Politique de confidentialité
        </Text>
        <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 8 }]}>{UPDATED_AT}</Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
          Votre vie privée est au cœur de Frollot. Cette politique explique quelles données nous traitons, pourquoi,
          combien de temps, avec qui, et les droits que la loi gabonaise vous reconnaît.
        </Text>

        {/* Sommaire cliquable */}
        <View style={[styles.tocCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
          <Text style={[typo.overline, { color: colors.secondary, marginBottom: 8 }]}>Sommaire</Text>
          {SECTIONS.map((s) => (
            <TouchableOpacity key={s.id} style={styles.tocRow} activeOpacity={0.6} onPress={() => scrollToSection(s.id)}>
              <MaterialIcons name="chevron-right" size={16} color={colors.primary} />
              <Text style={[typo.bodySmall, { color: colors.primary, flex: 1 }]} numberOfLines={2}>
                {s.title}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Sections */}
        {SECTIONS.map((s) => (
          <View key={s.id} onLayout={(e) => { sectionY.current[s.id] = e.nativeEvent.layout.y; }} style={styles.section}>
            <Text style={[typo.titleMedium, { color: colors.onBackground }]}>{s.title}</Text>
            {s.body.map((p, i) =>
              p.startsWith('• ') ? (
                <View key={i} style={styles.bulletRow}>
                  <Text style={[typo.bodyMedium, { color: colors.primary }]}>•</Text>
                  <Text style={[typo.bodyMedium, styles.paragraph, { color: colors.onSurfaceVariant, flex: 1, marginTop: 0 }]}>
                    {p.slice(2)}
                  </Text>
                </View>
              ) : (
                <Text key={i} style={[typo.bodyMedium, styles.paragraph, { color: colors.onSurfaceVariant }]}>
                  {p}
                </Text>
              )
            )}
          </View>
        ))}

        <Text style={[typo.bodySmall, styles.footer, { color: colors.onSurfaceVariant }]}>
          Délégué à la protection des données : privacy@frollot.ga
        </Text>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 64 },
  tocCard: { borderRadius: 20, borderWidth: 1, padding: 16, marginTop: 20 },
  tocRow: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingVertical: 5 },
  section: { marginTop: 28 },
  paragraph: { marginTop: 10, lineHeight: 22 },
  bulletRow: { flexDirection: 'row', gap: 8, marginTop: 8, paddingLeft: 8 },
  footer: { textAlign: 'center', marginTop: 36 },
});
