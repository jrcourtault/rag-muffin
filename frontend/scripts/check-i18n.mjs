/**
 * Vérifie que chaque clé des fichiers de traduction est utilisée dans le code source,
 * et que chaque fichier de traduction a les mêmes clés.
 *
 * Usage : node scripts/check-i18n.mjs
 *
 * Cross-platform (pas de dépendance à grep).
 */

import { readFileSync, readdirSync, statSync } from 'fs';
import { join, dirname, extname } from 'path';
import { fileURLToPath } from 'url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const i18nDir = join(root, 'src/i18n');
const srcDir = join(root, 'src');

// --- Helpers ---

/** Recursively collect all .ts and .html files under a directory */
function collectSourceFiles(dir) {
  const results = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const fullPath = join(dir, entry.name);
    if (entry.isDirectory()) {
      // skip node_modules / hidden dirs
      if (entry.name === 'node_modules' || entry.name.startsWith('.')) continue;
      results.push(...collectSourceFiles(fullPath));
    } else {
      const ext = extname(entry.name);
      if (ext === '.ts' || ext === '.html') {
        results.push(fullPath);
      }
    }
  }
  return results;
}

/** Read and concatenate all source files, returning per-file entries for context searches */
function loadSourceFiles(files) {
  return files.map((f) => ({
    path: f,
    content: readFileSync(f, 'utf8'),
  }));
}

/** Check if a literal string appears in any source file */
function isStringInSources(sources, needle) {
  return sources.some((s) => s.content.includes(needle));
}

/** Check if a string appears in a transloco context in any source file */
function isInTranslocoContext(sources, key) {
  for (const s of sources) {
    if (!s.content.includes(key)) continue;
    // Check surrounding context for transloco usage
    if (s.content.includes('transloco') || s.content.includes('translate(') || s.content.includes("t('")) {
      return true;
    }
  }
  return false;
}

// --- Charger tous les fichiers de traduction ---
const files = readdirSync(i18nDir).filter((f) => f.endsWith('.json'));
const translations = {};
for (const file of files) {
  const lang = file.replace('.json', '');
  translations[lang] = JSON.parse(readFileSync(join(i18nDir, file), 'utf8'));
}

const langs = Object.keys(translations);
let hasError = false;

// --- 1. Vérifier la cohérence entre fichiers (mêmes clés) ---
if (langs.length > 1) {
  const refLang = langs[0];
  const refKeys = new Set(Object.keys(translations[refLang]));

  for (const lang of langs.slice(1)) {
    const keys = new Set(Object.keys(translations[lang]));

    const missingInLang = [...refKeys].filter((k) => !keys.has(k));
    const extraInLang = [...keys].filter((k) => !refKeys.has(k));

    if (missingInLang.length > 0) {
      hasError = true;
      console.error(`\n❌ Clés présentes dans ${refLang}.json mais absentes de ${lang}.json :`);
      missingInLang.forEach((k) => console.error(`  - ${k}`));
    }
    if (extraInLang.length > 0) {
      hasError = true;
      console.error(`\n❌ Clés présentes dans ${lang}.json mais absentes de ${refLang}.json :`);
      extraInLang.forEach((k) => console.error(`  - ${k}`));
    }
  }
}

// --- Charger les fichiers source une seule fois ---
const sourceFiles = loadSourceFiles(collectSourceFiles(srcDir));

// --- 2. Vérifier que chaque clé est utilisée dans le code source ---
const allKeys = Object.keys(translations[langs[0]]);
const unused = [];

for (const key of allKeys) {
  if (isStringInSources(sourceFiles, key)) continue;

  // Clé dynamique ? Vérifier si le préfixe est utilisé avec concaténation
  const prefix = key.replace(/\.[^.]+$/, '.');
  const hasDynamicUsage = sourceFiles.some((s) => {
    if (!s.content.includes(prefix)) return false;
    // Check for concatenation pattern: 'prefix.' + variable
    const regex = new RegExp(`['"]${prefix.replace(/\./g, '\\.')}['"]\\s*\\+|\\+\\s*['"]${prefix.replace(/\./g, '\\.')}`);
    return regex.test(s.content);
  });

  if (!hasDynamicUsage) {
    unused.push(key);
  }
}

if (unused.length > 0) {
  hasError = true;
  console.error('\n❌ Clés inutilisées dans le code source :');
  unused.forEach((k) => console.error(`  - ${k}`));
}

// --- 3. Vérifier que chaque clé utilisée dans le code existe dans les traductions ---
const knownKeys = new Set(allKeys);

// Collect all 'dotted.key' patterns from source files
const dottedKeyPattern = /'([a-zA-Z][a-zA-Z0-9_.]*\.[a-zA-Z][a-zA-Z0-9_.]*)'/g;
const usedKeys = new Set();
for (const s of sourceFiles) {
  let match;
  while ((match = dottedKeyPattern.exec(s.content)) !== null) {
    usedKeys.add(match[1]);
  }
}

const missing = [...usedKeys].filter((k) => {
  if (knownKeys.has(k)) return false;
  // Dynamic prefix — not a full key
  if (allKeys.some((known) => known.startsWith(k)) && !knownKeys.has(k)) return false;
  // Must have a dot and not be a path
  if (!k.includes('.') || k.includes('/')) return false;
  // Must be used in a transloco context
  return isInTranslocoContext(sourceFiles, k);
});

if (missing.length > 0) {
  hasError = true;
  console.error('\n❌ Clés utilisées dans le code mais absentes des traductions :');
  missing.forEach((k) => console.error(`  - ${k}`));
}

// --- Résultat ---
if (hasError) {
  process.exit(1);
} else {
  console.log(`✅ i18n OK — ${allKeys.length} clés, ${langs.length} langues, tout est cohérent.`);
}