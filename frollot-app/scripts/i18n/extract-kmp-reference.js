#!/usr/bin/env node
/**
 * S9-0 — Extracteur KMP -> REFERENCE consultable (PAS un importeur de cles).
 *
 * Parse les strings_{fr,en,es,de,ar}.kt du frontend KMP (format `"cle" to "valeur"`
 * dans des mapOf) et produit scripts/i18n/kmp-reference.json : un index par VALEUR FR
 * -> { en, es, de, ar } + cles KMP d'origine (metadonnee indicative SEULEMENT).
 *
 * But : pendant la generation/relecture des traductions RN (S9-2/S9-3), retrouver
 * comment le KMP traduisait un texte FR donne. Ce fichier ne doit JAMAIS etre
 * charge par l'app (il vit hors de src/) et ne genere AUCUNE cle de production.
 *
 * Usage : node scripts/i18n/extract-kmp-reference.js
 */

const fs = require('fs');
const path = require('path');

const KMP_DIR = path.resolve(
  __dirname,
  '../../../frontend/composeApp/src/commonMain/kotlin/com/frollot/mobile/localization/resources'
);
const OUT_FILE = path.join(__dirname, 'kmp-reference.json');
const LANGS = ['fr', 'en', 'es', 'de', 'ar'];

// Desechappe les sequences Kotlin presentes dans les bundles (\n \t \" \\ \$ \')
function unescapeKotlin(s) {
  return s.replace(/\\(.)/g, (_, c) => {
    switch (c) {
      case 'n': return '\n';
      case 't': return '\t';
      case 'r': return '\r';
      case '"': return '"';
      case "'": return "'";
      case '\\': return '\\';
      case '$': return '$';
      default: return c; // sequence inconnue : conserver le caractere
    }
  });
}

// Parse un strings_xx.kt -> Map<cle, valeur>
function parseKtFile(file) {
  const src = fs.readFileSync(file, 'utf8');
  const map = new Map();
  // `"cle" to "valeur"` — les deux chaines peuvent contenir des echappements \"
  const re = /"((?:[^"\\]|\\.)*)"\s+to\s+"((?:[^"\\]|\\.)*)"/g;
  let m;
  while ((m = re.exec(src)) !== null) {
    map.set(unescapeKotlin(m[1]), unescapeKotlin(m[2]));
  }
  return map;
}

function main() {
  const bundles = {};
  for (const lang of LANGS) {
    const file = path.join(KMP_DIR, `strings_${lang}.kt`);
    if (!fs.existsSync(file)) {
      console.error(`ERREUR : fichier KMP introuvable : ${file}`);
      process.exit(1);
    }
    bundles[lang] = parseKtFile(file);
    console.log(`strings_${lang}.kt : ${bundles[lang].size} cles parsees`);
  }

  // Index par valeur FR. Doublons (plusieurs cles KMP -> meme texte FR) : on
  // regroupe les cles, on prend les traductions de la premiere cle qui en a,
  // et on signale les cas ou deux cles du groupe divergent dans une autre langue.
  const reference = {};
  const collisions = [];
  const divergences = [];

  for (const [key, frValue] of bundles.fr) {
    if (!reference[frValue]) {
      reference[frValue] = { kmpKeys: [key] };
      for (const lang of ['en', 'es', 'de', 'ar']) {
        const v = bundles[lang].get(key);
        if (v !== undefined) reference[frValue][lang] = v;
      }
    } else {
      const entry = reference[frValue];
      entry.kmpKeys.push(key);
      collisions.push({ frValue, keys: entry.kmpKeys.slice() });
      for (const lang of ['en', 'es', 'de', 'ar']) {
        const v = bundles[lang].get(key);
        if (v === undefined) continue;
        if (entry[lang] === undefined) {
          entry[lang] = v;
        } else if (entry[lang] !== v) {
          divergences.push({ frValue, lang, kept: entry[lang], other: v, otherKey: key });
        }
      }
    }
  }

  fs.writeFileSync(OUT_FILE, JSON.stringify(reference, null, 2), 'utf8');

  const entries = Object.keys(reference);
  console.log('---');
  console.log(`Reference ecrite : ${path.relative(process.cwd(), OUT_FILE)}`);
  console.log(`Valeurs FR distinctes : ${entries.length} (sur ${bundles.fr.size} cles fr)`);
  console.log(`Groupes de doublons (plusieurs cles KMP -> meme texte FR) : ${new Set(collisions.map((c) => c.frValue)).size}`);
  console.log(`Divergences de traduction au sein d'un groupe : ${divergences.length}`);
  if (divergences.length) {
    for (const d of divergences.slice(0, 10)) {
      console.log(`  [${d.lang}] "${d.frValue.slice(0, 40)}" : garde "${d.kept.slice(0, 40)}" / autre (${d.otherKey}) "${d.other.slice(0, 40)}"`);
    }
  }
  console.log('--- Echantillon (10 entrees) ---');
  for (const frValue of entries.slice(0, 10)) {
    const e = reference[frValue];
    console.log(`FR "${frValue.replace(/\n/g, '\\n').slice(0, 50)}" -> en "${(e.en ?? '?').replace(/\n/g, '\\n').slice(0, 50)}" | ar "${(e.ar ?? '?').slice(0, 30)}" (cles: ${e.kmpKeys.join(', ').slice(0, 60)})`);
  }
}

main();
