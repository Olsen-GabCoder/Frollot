/*
 * S7 — Conditions Générales d'Utilisation et de Vente (CGU/CGV)
 *
 * SOURCES RÉELLEMENT CONSULTÉES (recherche du 2026-06-12) :
 *  - Loi n°025/2023 du 12 juillet 2023 portant modification de la loi n°001/2011 du
 *    25 septembre 2011 relative à la protection des données à caractère personnel —
 *    texte intégral lu au JO n°218 Bis du 15/07/2023 :
 *    https://www.afapdp.org/wp-content/uploads/2023/12/Gabon-Loi-025-2023-du-12-juillet-2023-portant-modification-de-la-loi-001-2011-du-25-septembre-relative-a-la-protection-des-donnees-a-caractere-personnel.pdf
 *    (art. 7 : création de l'APDPVP ; art. 43 et s. : droits des personnes)
 *  - Ordonnance n°0011/PR/2026 du 26 février 2026 portant réglementation de l'usage des
 *    réseaux sociaux (JO n°110, 8-15 avril 2026) : https://journal-officiel.ga/22404-0011-pr-2026-/
 *    + analyse doctrinale : https://www.village-justice.com/articles/gabon-dote-droit-des-reseaux-sociaux-analyse-des-ordonnances-fevrier-2026,57009.html
 *    (majorité numérique 16 ans ; identification des utilisateurs ; signalement 24h/72h)
 *  - Loi n°025/2021 du 28 décembre 2021 portant réglementation des transactions
 *    électroniques : https://journal-officiel.ga/18190-025-2021-/
 *    (obligations d'information de l'opérateur de commerce électronique)
 *  - Règlement n°04/18/CEMAC/UMAC/COBAC du 21 décembre 2018 relatif aux services de
 *    paiement dans la CEMAC : https://www.beac.int/wp-content/uploads/2019/07/REGLEMENT-N-04-18-CEMAC-UMAC-COBAC-du-21-d%C3%A9cembre-2018.pdf
 *  - Acte uniforme OHADA révisé portant sur le droit commercial général (15/12/2010) :
 *    https://dgi.ga/wp-content/uploads/2025/04/ACTE-UNIFORME-REVISE-PORTANT-SUR-LE-DROIT-COMMERCIAL-GENERAL.pdf
 *  - TVA Gabon, taux normal 18 % (CGI, art. 209 et s.) : https://dgi.ga/imposition-des-personnes-morales/taxes-sur-le-chiffre-daffaires/tva/
 *
 * À FAIRE AVANT PRODUCTION (Olsen) :
 *  1. Remplacer l'identité de l'éditeur (article 1) : forme sociale exacte, capital,
 *     n° RCCM, NIF, adresse complète du siège à Libreville.
 *  2. Remplacer « support@frollot.ga » par l'adresse de support réellement ouverte.
 *  3. Faire confirmer par un juriste gabonais : (a) la clause de remboursement volontaire
 *     48 h (aucun droit de rétractation légal vérifié au Gabon pour les services
 *     numériques — c'est un engagement commercial, pas une obligation) ; (b) les
 *     modalités d'application de l'ordonnance n°0011/PR/2026 (identification NIP,
 *     autorisation parentale certifiée 13-16 ans) connues via doctrine, à confirmer
 *     au JO n°110 ; (c) la date d'entrée en vigueur affichée.
 *  4. Vérifier l'assujettissement effectif à la TVA (seuil de 60 000 000 FCFA de CA —
 *     si non assujetti, reformuler « TVA incluse » à l'article 10.
 */
import { useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { router } from 'expo-router';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';

interface LegalSection {
  id: string;
  title: string;
  /** Paragraphes. Préfixe "• " = puce. */
  body: string[];
}

const UPDATED_AT = 'Dernière mise à jour : 12 juin 2026';

const SECTIONS: LegalSection[] = [
  {
    id: 'editeur',
    title: 'Article 1 — Éditeur et objet',
    body: [
      "L'application Frollot (ci-après « l'Application ») est éditée par Frollot, dont le siège est établi à Libreville, République Gabonaise (ci-après « Frollot », « nous »). Frollot peut être contactée à tout moment à l'adresse support@frollot.ga.",
      "Conformément à la législation gabonaise sur les transactions électroniques, et notamment à la loi n°025/2021 du 28 décembre 2021 portant réglementation des transactions électroniques, Frollot met à la disposition de ses utilisateurs un accès facile et direct aux informations relatives à son identité, à ses services et à ses prix.",
      "Les présentes Conditions Générales d'Utilisation et de Vente (ci-après « les Conditions ») ont pour objet de définir les modalités d'accès et d'utilisation de l'Application, qui combine : un service de mise en relation et de réservation entre des clients et des salons de coiffure et coiffeurs professionnels ; un réseau social dédié à la coiffure et à la beauté ; et une offre payante dénommée « Frollot Premium ».",
    ],
  },
  {
    id: 'definitions',
    title: 'Article 2 — Définitions',
    body: [
      "« Application » désigne l'application mobile et le site web Frollot, ainsi que l'ensemble de leurs fonctionnalités.",
      "« Utilisateur » désigne toute personne physique titulaire d'un compte sur l'Application, qu'elle agisse en qualité de Client ou de Professionnel.",
      "« Client » désigne l'Utilisateur qui consulte les profils, publie ou consulte des contenus et réserve des prestations auprès des Professionnels.",
      "« Professionnel » désigne le salon de coiffure, le barbier ou le coiffeur indépendant inscrit sur l'Application pour y présenter son activité, ses prestations et ses disponibilités. Le Professionnel exerce sous le statut commercial qui lui est propre, dans le respect du droit applicable, notamment de l'Acte uniforme OHADA portant sur le droit commercial général.",
      "« Contenu » désigne tout élément publié par un Utilisateur sur l'Application : photographies, vidéos, textes, avis, commentaires, descriptions et réactions.",
      "« Premium » désigne l'offre payante décrite à l'article 10 des présentes.",
      "« Réservation » désigne la prise de rendez-vous effectuée par un Client auprès d'un Professionnel par l'intermédiaire de l'Application.",
    ],
  },
  {
    id: 'acceptation',
    title: 'Article 3 — Acceptation et modification des Conditions',
    body: [
      "La création d'un compte et l'utilisation de l'Application emportent acceptation pleine et entière des présentes Conditions. L'Utilisateur qui n'accepte pas les Conditions doit s'abstenir d'utiliser l'Application.",
      "Frollot peut faire évoluer les Conditions pour tenir compte des évolutions légales, techniques ou commerciales. Toute modification substantielle est portée à la connaissance des Utilisateurs par une notification dans l'Application au moins quinze jours avant son entrée en vigueur. La poursuite de l'utilisation de l'Application après cette date vaut acceptation des Conditions modifiées. L'Utilisateur qui refuse les nouvelles Conditions peut supprimer son compte à tout moment, sans frais.",
      "La version en vigueur des Conditions est accessible en permanence depuis les réglages de l'Application.",
    ],
  },
  {
    id: 'age',
    title: 'Article 4 — Accès au service et âge minimum',
    body: [
      "Conformément à la réglementation gabonaise de l'usage des réseaux sociaux issue de l'ordonnance n°0011/PR/2026 du 26 février 2026, la majorité numérique est fixée à seize ans. L'accès aux fonctionnalités sociales de l'Application est en conséquence réservé aux personnes âgées d'au moins seize ans.",
      "Les personnes mineures de moins de seize ans ne peuvent accéder à l'Application qu'avec l'autorisation expresse de leurs parents ou du titulaire de l'autorité parentale, dans les conditions prévues par la réglementation en vigueur. Frollot se réserve le droit de demander tout justificatif et de suspendre ou supprimer tout compte créé en violation du présent article.",
      "Lors de son inscription, l'Utilisateur s'engage à fournir des informations d'identification exactes, complètes et à jour, et à les maintenir telles pendant toute la durée d'utilisation du service. La réglementation gabonaise impose aux utilisateurs de plateformes sociales de s'identifier de manière véridique ; la création d'un compte sous une fausse identité ou l'usurpation de l'identité d'un tiers est interdite et expose son auteur aux sanctions prévues par la loi.",
    ],
  },
  {
    id: 'compte',
    title: 'Article 5 — Compte et sécurité',
    body: [
      "Le compte est strictement personnel. L'Utilisateur est seul responsable de la confidentialité de ses identifiants de connexion et de toutes les opérations effectuées depuis son compte.",
      "L'Utilisateur s'engage à informer Frollot sans délai, à l'adresse support@frollot.ga, de toute utilisation non autorisée de son compte ou de toute atteinte à la sécurité de ses identifiants. Frollot ne saurait être tenue responsable des conséquences d'une divulgation des identifiants imputable à l'Utilisateur.",
      "Frollot met en œuvre des mesures de sécurité conformes à l'état de l'art pour protéger les comptes, décrites dans la Politique de confidentialité accessible depuis les réglages.",
    ],
  },
  {
    id: 'services-gratuits',
    title: 'Article 6 — Services gratuits',
    body: [
      "L'Application propose gratuitement les services suivants : la création d'un compte et d'un profil ; la consultation des profils de Professionnels, de leurs prestations, de leurs tarifs et de leurs disponibilités ; la prise de Réservations et l'inscription en file d'attente ; la publication, la consultation et le partage de Contenus au sein du réseau social ; la publication d'avis après une prestation ; la constitution de collections et l'archivage de publications.",
      "La gratuité de ces services ne confère à l'Utilisateur aucun droit acquis à leur maintien. Frollot peut faire évoluer, suspendre ou supprimer une fonctionnalité gratuite, sous réserve d'en informer les Utilisateurs dans un délai raisonnable lorsque l'évolution est substantielle.",
    ],
  },
  {
    id: 'publications',
    title: 'Article 7 — Règles de publication et responsabilité des Contenus',
    body: [
      "Chaque Utilisateur est seul responsable des Contenus qu'il publie sur l'Application, conformément au régime de responsabilité institué par la réglementation gabonaise de l'usage des réseaux sociaux (ordonnance n°0011/PR/2026). La diffusion d'un Contenu illicite engage la responsabilité personnelle de son auteur, y compris lorsque le Contenu est relayé ou amplifié.",
      "Sont notamment interdits sur l'Application :",
      "• les contenus haineux, injurieux, diffamatoires ou discriminatoires, ainsi que toute forme d'incitation à la haine ou à la violence ;",
      "• le harcèlement et le cyberharcèlement, sous quelque forme que ce soit ;",
      "• les contenus portant atteinte à la dignité humaine, à la vie privée ou au droit à l'image d'autrui ;",
      "• l'usurpation d'identité et la création de faux profils ;",
      "• les contenus à caractère pornographique ou d'une violence manifeste ;",
      "• les contenus générés ou manipulés par intelligence artificielle présentés comme authentiques : tout contenu produit par un procédé d'intelligence artificielle doit porter une mention explicite, et les hypertrucages (« deepfakes ») destinés à nuire ou à tromper sont strictement interdits ;",
      "• les contenus contraires à l'ordre public, à la législation gabonaise ou aux droits de propriété intellectuelle de tiers ;",
      "• les publications commerciales trompeuses, notamment les avis fictifs ou rémunérés non signalés.",
      "En publiant un Contenu, l'Utilisateur garantit qu'il dispose de l'ensemble des droits et autorisations nécessaires, notamment du consentement des personnes identifiables apparaissant sur les photographies et vidéos.",
    ],
  },
  {
    id: 'moderation',
    title: 'Article 8 — Modération et signalement',
    body: [
      "Tout Utilisateur peut signaler un Contenu ou un compte qu'il estime contraire aux présentes Conditions ou à la loi, au moyen de la fonction « Signaler » disponible sur chaque publication et chaque profil.",
      "En cohérence avec les délais institués par la réglementation gabonaise des réseaux sociaux, Frollot s'engage à accuser réception de tout signalement relatif à des faits de harcèlement dans un délai de vingt-quatre heures et à notifier sa décision à l'auteur du signalement dans un délai de soixante-douze heures. Les contenus synthétiques non consentis portant atteinte à une personne sont retirés dans les meilleurs délais à compter de leur signalement.",
      "Frollot peut, de manière proportionnée : retirer ou rendre inaccessible un Contenu ; adresser un avertissement à l'Utilisateur ; restreindre, suspendre ou supprimer un compte en cas de manquement grave ou répété. Frollot coopère avec les autorités judiciaires et administratives gabonaises dans les conditions prévues par la loi.",
      "L'Utilisateur dont un Contenu a été retiré peut contester la décision en écrivant à support@frollot.ga ; sa contestation est examinée par l'équipe de modération.",
    ],
  },
  {
    id: 'propriete',
    title: 'Article 9 — Propriété intellectuelle',
    body: [
      "L'Application, sa structure, sa charte graphique, ses textes, ses logos et la marque Frollot sont la propriété exclusive de Frollot ou de ses concédants. Toute reproduction ou représentation, totale ou partielle, sans autorisation écrite préalable est interdite.",
      "L'Utilisateur conserve l'entière propriété des Contenus qu'il publie. En les publiant, il concède à Frollot une licence non exclusive, gratuite et mondiale d'hébergement, de reproduction et de représentation de ces Contenus, aux seules fins de l'exploitation, de la promotion et de l'amélioration de l'Application, pour la durée de présence du Contenu sur l'Application.",
      "Cette licence prend fin lors de la suppression du Contenu ou du compte, sous réserve des copies techniques temporaires et des obligations légales de conservation.",
    ],
  },
  {
    id: 'premium',
    title: 'Article 10 — Offre Frollot Premium (conditions de vente)',
    body: [
      "Frollot Premium est une offre payante donnant accès à des fonctionnalités étendues de l'Application, dont le détail est présenté sur l'écran de souscription avant tout paiement.",
      "Le prix de l'abonnement Premium est de 10 000 francs CFA toutes taxes comprises, incluant la taxe sur la valeur ajoutée au taux normal de 18 % en vigueur en République Gabonaise. Le prix, la durée de l'abonnement et le contenu de l'offre sont récapitulés de manière claire avant la validation du paiement, conformément aux obligations d'information posées par la loi n°025/2021 sur les transactions électroniques.",
      "L'abonnement Premium est conclu pour une durée déterminée, indiquée au moment de la souscription. Il ne fait l'objet d'aucune reconduction tacite : à son échéance, l'abonnement prend fin de plein droit et l'Utilisateur retrouve l'usage gratuit de l'Application. La souscription d'une nouvelle période requiert une démarche volontaire et un nouveau paiement.",
      "Frollot adresse à l'Utilisateur une confirmation de souscription récapitulant la prestation, le prix payé, la référence de la transaction et la période couverte. Cette confirmation est conservée et reste accessible depuis le compte.",
    ],
  },
  {
    id: 'paiement',
    title: 'Article 11 — Paiement par Mobile Money',
    body: [
      "Le paiement de l'abonnement Premium s'effectue par Mobile Money, via les services Airtel Money et Moov Money. Ces services sont fournis par des prestataires de services de paiement exerçant sous le régime du règlement n°04/18/CEMAC/UMAC/COBAC du 21 décembre 2018 relatif aux services de paiement dans la Communauté Économique et Monétaire de l'Afrique Centrale.",
      "La transaction de paiement est exécutée par l'opérateur de Mobile Money choisi par l'Utilisateur, selon les conditions propres à ce service. L'Utilisateur valide le paiement directement auprès de son opérateur, au moyen de son code confidentiel.",
      "Frollot ne collecte, ne stocke et n'a accès à aucun moment au code confidentiel (PIN) Mobile Money de l'Utilisateur. Frollot ne conserve que les informations strictement nécessaires au suivi de la commande : montant, date, statut et référence de la transaction communiquée par l'opérateur.",
      "En cas d'échec du paiement (solde insuffisant, délai de validation expiré, incident technique), aucun montant n'est dû et l'abonnement n'est pas activé. Si un montant a été débité sans activation du Premium, l'Utilisateur est invité à contacter support@frollot.ga avec la référence de la transaction ; la situation est régularisée après vérification auprès de l'opérateur.",
    ],
  },
  {
    id: 'remboursement',
    title: 'Article 12 — Garantie de remboursement',
    body: [
      "Frollot offre à chaque Utilisateur une garantie commerciale de remboursement : l'Utilisateur peut demander le remboursement intégral de son abonnement Premium dans les quarante-huit heures suivant le paiement, à condition que les fonctionnalités Premium n'aient pas été utilisées pendant cette période.",
      "La demande s'effectue par message à support@frollot.ga, en indiquant la référence de la transaction. Le remboursement est effectué sur le compte Mobile Money ayant servi au paiement, dans un délai maximal de quatorze jours à compter de l'acceptation de la demande.",
      "Cette garantie est un engagement volontaire de Frollot ; elle s'ajoute aux droits que l'Utilisateur tient, le cas échéant, de la législation applicable, et ne les restreint en aucune manière.",
      "Au-delà du délai de quarante-huit heures ou en cas d'utilisation des fonctionnalités Premium, l'abonnement n'est pas remboursable, sauf défaillance du service imputable à Frollot rendant le Premium durablement inutilisable, auquel cas un remboursement au prorata de la période non consommée est accordé.",
    ],
  },
  {
    id: 'reservations',
    title: 'Article 13 — Réservations : rôle de Frollot',
    body: [
      "Frollot agit exclusivement en qualité d'intermédiaire technique de mise en relation entre les Clients et les Professionnels. Le contrat de prestation de coiffure est conclu directement et uniquement entre le Client et le Professionnel concerné.",
      "Les informations relatives aux prestations — descriptions, durées, tarifs, disponibilités — sont renseignées par les Professionnels sous leur seule responsabilité. Le prix de la prestation est payé par le Client directement au Professionnel, selon les modalités convenues entre eux ; il est distinct du prix de l'abonnement Premium visé à l'article 10.",
      "Frollot n'est pas partie au contrat de prestation et ne garantit ni la qualité, ni la conformité, ni l'exécution des prestations réalisées par les Professionnels. Toute réclamation relative à une prestation doit être adressée en premier lieu au Professionnel concerné. Frollot peut néanmoins être informée via support@frollot.ga et prendre, le cas échéant, des mesures à l'égard d'un Professionnel défaillant (avertissement, déréférencement, suspension).",
      "Le Client s'engage à honorer ses Réservations ou à les annuler dans un délai raisonnable depuis l'Application. Des absences répétées et non annoncées peuvent entraîner une restriction de l'accès au service de Réservation.",
    ],
  },
  {
    id: 'responsabilite',
    title: 'Article 14 — Suspension, résiliation et responsabilité',
    body: [
      "L'Utilisateur peut supprimer son compte à tout moment depuis les réglages de l'Application. La suppression du compte entraîne la fin des présentes Conditions à son égard, sans préjudice des stipulations qui survivent par nature (propriété intellectuelle, responsabilité, litiges).",
      "Frollot peut suspendre ou résilier le compte d'un Utilisateur en cas de manquement grave ou répété aux présentes Conditions, après mise en demeure restée sans effet, sauf lorsque la gravité du manquement ou une obligation légale justifie une mesure immédiate. La résiliation pour manquement n'ouvre droit à aucun remboursement de la période Premium en cours.",
      "Frollot s'engage à fournir le service avec diligence et selon les règles de l'art, dans le cadre d'une obligation de moyens. L'accès à l'Application peut être momentanément interrompu pour maintenance, mise à jour ou en cas de force majeure, ou en raison de défaillances des réseaux de communications électroniques indépendantes de la volonté de Frollot.",
      "La responsabilité de Frollot ne saurait être engagée à raison des Contenus publiés par les Utilisateurs, des prestations fournies par les Professionnels, ou des dommages indirects tels que perte de chiffre d'affaires ou perte de clientèle. En toute hypothèse, la responsabilité de Frollot au titre de l'abonnement Premium est limitée au montant effectivement payé par l'Utilisateur au cours des douze derniers mois.",
    ],
  },
  {
    id: 'litiges',
    title: 'Article 15 — Droit applicable et litiges',
    body: [
      "Les présentes Conditions sont régies par le droit de la République Gabonaise, complété, pour les matières qui en relèvent, par les Actes uniformes de l'Organisation pour l'Harmonisation en Afrique du Droit des Affaires (OHADA), notamment l'Acte uniforme révisé portant sur le droit commercial général.",
      "En cas de difficulté, l'Utilisateur est invité à rechercher d'abord une solution amiable en contactant le support à l'adresse support@frollot.ga. Frollot s'engage à examiner toute réclamation avec diligence et à y répondre dans un délai raisonnable.",
      "À défaut de résolution amiable dans un délai de soixante jours, le litige sera porté devant les juridictions compétentes de Libreville, sous réserve des règles impératives de compétence applicables.",
      "Si l'une des stipulations des présentes Conditions est déclarée nulle ou inapplicable, les autres stipulations conservent leur pleine validité.",
    ],
  },
];

export default function TermsScreen() {
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
          Conditions Générales d'Utilisation et de Vente
        </Text>
        <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginTop: 8 }]}>{UPDATED_AT}</Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
          Les présentes conditions régissent l'utilisation de l'application Frollot, service de réservation et réseau
          social dédiés à la coiffure, ainsi que la souscription de l'offre Frollot Premium.
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
          Pour toute question relative aux présentes conditions : support@frollot.ga
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
