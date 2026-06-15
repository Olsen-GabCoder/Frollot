import { getCountries, getCountryCallingCode, CountryCode } from 'libphonenumber-js';

/**
 * Données pays pour le sélecteur d'indicatif téléphonique (incrément 2 téléphone).
 *
 * - Liste des pays = getCountries() de libphonenumber-js (source de vérité des
 *   indicatifs : tout pays listé ici est composable, rien d'inventé).
 * - Noms FR = TABLE STATIQUE locale (PAS Intl.DisplayNames : pari Hermes évité,
 *   résultat identique sur Android/iOS/Web). Territoire sans entrée -> fallback
 *   sur le code ISO (jamais de crash, jamais de trou).
 * - Drapeaux : voir getFlagUrl (flagcdn.com, URL centralisée ici — si le SVG
 *   posait problème sur un device Android réel, basculer PNG = UNE ligne).
 */

export interface Country {
  /** Code ISO 3166-1 alpha-2 (ex. 'GA') */
  iso2: CountryCode;
  /** Nom français (fallback : code ISO) */
  nameFr: string;
  /** Indicatif international SANS le '+' (ex. '241') */
  callingCode: string;
}

/** Pays par défaut du marché Frollot. */
export const DEFAULT_ISO2: CountryCode = 'GA';

/**
 * URL du drapeau (flagcdn.com, SVG — supporté Android/iOS/Web par expo-image
 * selon les docs Expo v56). Point UNIQUE : pour basculer en PNG raster,
 * remplacer par `https://flagcdn.com/w80/${iso2.toLowerCase()}.png`.
 */
export function getFlagUrl(iso2: string): string {
  return `https://flagcdn.com/${iso2.toLowerCase()}.svg`;
}

/**
 * Noms français des pays/territoires couverts par libphonenumber-js (245 codes).
 * Généré une fois, trié à l'exécution. AC/TA (Ascension/Tristan da Cunha) sont
 * des codes téléphoniques hors ISO officiel mais composables -> nommés aussi.
 */
const COUNTRY_NAMES_FR: Record<string, string> = {
  AC: 'Île de l’Ascension',
  AD: 'Andorre',
  AE: 'Émirats arabes unis',
  AF: 'Afghanistan',
  AG: 'Antigua-et-Barbuda',
  AI: 'Anguilla',
  AL: 'Albanie',
  AM: 'Arménie',
  AO: 'Angola',
  AR: 'Argentine',
  AS: 'Samoa américaines',
  AT: 'Autriche',
  AU: 'Australie',
  AW: 'Aruba',
  AX: 'Îles Åland',
  AZ: 'Azerbaïdjan',
  BA: 'Bosnie-Herzégovine',
  BB: 'Barbade',
  BD: 'Bangladesh',
  BE: 'Belgique',
  BF: 'Burkina Faso',
  BG: 'Bulgarie',
  BH: 'Bahreïn',
  BI: 'Burundi',
  BJ: 'Bénin',
  BL: 'Saint-Barthélemy',
  BM: 'Bermudes',
  BN: 'Brunei',
  BO: 'Bolivie',
  BQ: 'Pays-Bas caribéens',
  BR: 'Brésil',
  BS: 'Bahamas',
  BT: 'Bhoutan',
  BW: 'Botswana',
  BY: 'Biélorussie',
  BZ: 'Belize',
  CA: 'Canada',
  CC: 'Îles Cocos',
  CD: 'Congo (RDC)',
  CF: 'République centrafricaine',
  CG: 'Congo (Brazzaville)',
  CH: 'Suisse',
  CI: 'Côte d’Ivoire',
  CK: 'Îles Cook',
  CL: 'Chili',
  CM: 'Cameroun',
  CN: 'Chine',
  CO: 'Colombie',
  CR: 'Costa Rica',
  CU: 'Cuba',
  CV: 'Cap-Vert',
  CW: 'Curaçao',
  CX: 'Île Christmas',
  CY: 'Chypre',
  CZ: 'Tchéquie',
  DE: 'Allemagne',
  DJ: 'Djibouti',
  DK: 'Danemark',
  DM: 'Dominique',
  DO: 'République dominicaine',
  DZ: 'Algérie',
  EC: 'Équateur',
  EE: 'Estonie',
  EG: 'Égypte',
  EH: 'Sahara occidental',
  ER: 'Érythrée',
  ES: 'Espagne',
  ET: 'Éthiopie',
  FI: 'Finlande',
  FJ: 'Fidji',
  FK: 'Îles Malouines',
  FM: 'Micronésie',
  FO: 'Îles Féroé',
  FR: 'France',
  GA: 'Gabon',
  GB: 'Royaume-Uni',
  GD: 'Grenade',
  GE: 'Géorgie',
  GF: 'Guyane française',
  GG: 'Guernesey',
  GH: 'Ghana',
  GI: 'Gibraltar',
  GL: 'Groenland',
  GM: 'Gambie',
  GN: 'Guinée',
  GP: 'Guadeloupe',
  GQ: 'Guinée équatoriale',
  GR: 'Grèce',
  GT: 'Guatemala',
  GU: 'Guam',
  GW: 'Guinée-Bissau',
  GY: 'Guyana',
  HK: 'Hong Kong',
  HN: 'Honduras',
  HR: 'Croatie',
  HT: 'Haïti',
  HU: 'Hongrie',
  ID: 'Indonésie',
  IE: 'Irlande',
  IL: 'Israël',
  IM: 'Île de Man',
  IN: 'Inde',
  IO: 'Territoire britannique de l’océan Indien',
  IQ: 'Irak',
  IR: 'Iran',
  IS: 'Islande',
  IT: 'Italie',
  JE: 'Jersey',
  JM: 'Jamaïque',
  JO: 'Jordanie',
  JP: 'Japon',
  KE: 'Kenya',
  KG: 'Kirghizistan',
  KH: 'Cambodge',
  KI: 'Kiribati',
  KM: 'Comores',
  KN: 'Saint-Christophe-et-Niévès',
  KP: 'Corée du Nord',
  KR: 'Corée du Sud',
  KW: 'Koweït',
  KY: 'Îles Caïmans',
  KZ: 'Kazakhstan',
  LA: 'Laos',
  LB: 'Liban',
  LC: 'Sainte-Lucie',
  LI: 'Liechtenstein',
  LK: 'Sri Lanka',
  LR: 'Liberia',
  LS: 'Lesotho',
  LT: 'Lituanie',
  LU: 'Luxembourg',
  LV: 'Lettonie',
  LY: 'Libye',
  MA: 'Maroc',
  MC: 'Monaco',
  MD: 'Moldavie',
  ME: 'Monténégro',
  MF: 'Saint-Martin',
  MG: 'Madagascar',
  MH: 'Îles Marshall',
  MK: 'Macédoine du Nord',
  ML: 'Mali',
  MM: 'Myanmar (Birmanie)',
  MN: 'Mongolie',
  MO: 'Macao',
  MP: 'Îles Mariannes du Nord',
  MQ: 'Martinique',
  MR: 'Mauritanie',
  MS: 'Montserrat',
  MT: 'Malte',
  MU: 'Maurice',
  MV: 'Maldives',
  MW: 'Malawi',
  MX: 'Mexique',
  MY: 'Malaisie',
  MZ: 'Mozambique',
  NA: 'Namibie',
  NC: 'Nouvelle-Calédonie',
  NE: 'Niger',
  NF: 'Île Norfolk',
  NG: 'Nigeria',
  NI: 'Nicaragua',
  NL: 'Pays-Bas',
  NO: 'Norvège',
  NP: 'Népal',
  NR: 'Nauru',
  NU: 'Niue',
  NZ: 'Nouvelle-Zélande',
  OM: 'Oman',
  PA: 'Panama',
  PE: 'Pérou',
  PF: 'Polynésie française',
  PG: 'Papouasie-Nouvelle-Guinée',
  PH: 'Philippines',
  PK: 'Pakistan',
  PL: 'Pologne',
  PM: 'Saint-Pierre-et-Miquelon',
  PR: 'Porto Rico',
  PS: 'Palestine',
  PT: 'Portugal',
  PW: 'Palaos',
  PY: 'Paraguay',
  QA: 'Qatar',
  RE: 'La Réunion',
  RO: 'Roumanie',
  RS: 'Serbie',
  RU: 'Russie',
  RW: 'Rwanda',
  SA: 'Arabie saoudite',
  SB: 'Îles Salomon',
  SC: 'Seychelles',
  SD: 'Soudan',
  SE: 'Suède',
  SG: 'Singapour',
  SH: 'Sainte-Hélène',
  SI: 'Slovénie',
  SJ: 'Svalbard et Jan Mayen',
  SK: 'Slovaquie',
  SL: 'Sierra Leone',
  SM: 'Saint-Marin',
  SN: 'Sénégal',
  SO: 'Somalie',
  SR: 'Suriname',
  SS: 'Soudan du Sud',
  ST: 'Sao Tomé-et-Principe',
  SV: 'Salvador',
  SX: 'Saint-Martin (partie néerlandaise)',
  SY: 'Syrie',
  SZ: 'Eswatini',
  TA: 'Tristan da Cunha',
  TC: 'Îles Turques-et-Caïques',
  TD: 'Tchad',
  TG: 'Togo',
  TH: 'Thaïlande',
  TJ: 'Tadjikistan',
  TK: 'Tokelau',
  TL: 'Timor oriental',
  TM: 'Turkménistan',
  TN: 'Tunisie',
  TO: 'Tonga',
  TR: 'Turquie',
  TT: 'Trinité-et-Tobago',
  TV: 'Tuvalu',
  TW: 'Taïwan',
  TZ: 'Tanzanie',
  UA: 'Ukraine',
  UG: 'Ouganda',
  US: 'États-Unis',
  UY: 'Uruguay',
  UZ: 'Ouzbékistan',
  VA: 'Vatican',
  VC: 'Saint-Vincent-et-les-Grenadines',
  VE: 'Venezuela',
  VG: 'Îles Vierges britanniques',
  VI: 'Îles Vierges américaines',
  VN: 'Vietnam',
  VU: 'Vanuatu',
  WF: 'Wallis-et-Futuna',
  WS: 'Samoa',
  XK: 'Kosovo',
  YE: 'Yémen',
  YT: 'Mayotte',
  ZA: 'Afrique du Sud',
  ZM: 'Zambie',
  ZW: 'Zimbabwe',
};

/**
 * Normalisation pour la recherche : minuscules + accents retirés
 * ('Bénin' -> 'benin'). NFD est supporté par Hermes et le web.
 */
export function normalizeForSearch(text: string): string {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');
}

/** Liste complète, triée alphabétiquement par nom FR (construite une seule fois). */
export const COUNTRIES: Country[] = getCountries()
  .map((iso2) => ({
    iso2,
    // Territoire sans nom FR -> fallback code ISO (pas de crash, ligne lisible)
    nameFr: COUNTRY_NAMES_FR[iso2] ?? iso2,
    callingCode: getCountryCallingCode(iso2),
  }))
  .sort((a, b) => a.nameFr.localeCompare(b.nameFr, 'fr'));

const COUNTRY_BY_ISO2 = new Map(COUNTRIES.map((c) => [c.iso2 as string, c]));

/** Pays par code ISO2 (insensible à la casse). */
export function getCountryByIso2(iso2: string): Country | undefined {
  return COUNTRY_BY_ISO2.get(iso2.toUpperCase());
}

/** Défaut marché : Gabon (+241). */
export function getDefaultCountry(): Country {
  return getCountryByIso2(DEFAULT_ISO2)!;
}

/**
 * Filtre par nom FR OU indicatif, insensible casse/accents :
 * 'gab' ou '241' -> Gabon ; '+241' accepté aussi.
 */
export function searchCountries(query: string): Country[] {
  const q = normalizeForSearch(query.trim().replace(/^\+/, ''));
  if (!q) return COUNTRIES;
  return COUNTRIES.filter(
    (c) =>
      normalizeForSearch(c.nameFr).includes(q) ||
      c.callingCode.startsWith(q) ||
      c.iso2.toLowerCase() === q
  );
}
