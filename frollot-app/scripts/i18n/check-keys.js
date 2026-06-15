#!/usr/bin/env node
/**
 * S9-0 — Verificateur de parite des cles i18n de PRODUCTION.
 *
 * Verifie que chaque cle de fr.json existe dans en/es/de/ar et inversement
 * (zero manquante, zero orpheline). fr = langue de reference.
 * Sortie : `OK (0 ecart)` ou la liste des ecarts par langue.
 * Code de sortie 1 si ecart (utilisable en pre-commit).
 *
 * Usage : node scripts/i18n/check-keys.js
 */

const fs = require('fs');
const path = require('path');

const LOCALES_DIR = path.resolve(__dirname, '../../src/i18n');
const REF_LANG = 'fr';
const OTHER_LANGS = ['en', 'es', 'de', 'ar'];

// Aplati un JSON imbrique en cles pointees ("common.cancel")
function flattenKeys(obj, prefix = '', out = []) {
  for (const [k, v] of Object.entries(obj)) {
    const key = prefix ? `${prefix}.${k}` : k;
    if (v !== null && typeof v === 'object' && !Array.isArray(v)) {
      flattenKeys(v, key, out);
    } else {
      out.push(key);
    }
  }
  return out;
}

function loadKeys(lang) {
  const file = path.join(LOCALES_DIR, `${lang}.json`);
  if (!fs.existsSync(file)) {
    console.error(`ERREUR : ${file} introuvable`);
    process.exit(1);
  }
  return new Set(flattenKeys(JSON.parse(fs.readFileSync(file, 'utf8'))));
}

// Suffixes de pluriel CLDR (l'arabe en utilise 6 : zero/one/two/few/many/other,
// alors que fr/en/es/de n'en utilisent que 2 : one/other). On tolere les formes
// supplementaires comme non-orphelines SI la base existe en _one ou _other cote fr.
const PLURAL_SUFFIXES = ['_zero', '_one', '_two', '_few', '_many', '_other'];

function isPluralVariant(key, refKeys) {
  for (const suf of PLURAL_SUFFIXES) {
    if (key.endsWith(suf)) {
      const base = key.slice(0, -suf.length);
      if (refKeys.has(base + '_one') || refKeys.has(base + '_other')) return true;
    }
  }
  return false;
}

function main() {
  const refKeys = loadKeys(REF_LANG);
  console.log(`${REF_LANG}.json : ${refKeys.size} cles (reference)`);

  let totalGaps = 0;
  for (const lang of OTHER_LANGS) {
    const keys = loadKeys(lang);
    const missing = [...refKeys].filter((k) => !keys.has(k)); // dans fr, pas dans lang
    const orphan = [...keys].filter((k) => !refKeys.has(k) && !isPluralVariant(k, refKeys)); // dans lang, pas dans fr (tolere les variantes de pluriel CLDR)
    totalGaps += missing.length + orphan.length;
    console.log(`${lang}.json : ${keys.size} cles — manquantes vs fr : ${missing.length}, orphelines : ${orphan.length}`);
    for (const k of missing) console.log(`  [${lang}] MANQUANTE : ${k}`);
    for (const k of orphan) console.log(`  [${lang}] ORPHELINE : ${k}`);
  }

  console.log('---');
  if (totalGaps === 0) {
    console.log('OK (0 ecart)');
  } else {
    console.log(`FAIL (${totalGaps} ecarts)`);
    process.exit(1);
  }
}

main();
