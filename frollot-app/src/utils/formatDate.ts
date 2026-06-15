/**
 * Centralised date formatting based on Intl.DateTimeFormat(i18n.language).
 *
 * Replaces hard-coded 'fr-FR' locale strings and MONTHS/DAYS_SHORT arrays.
 * All functions gracefully handle invalid Date (return '' instead of crashing).
 *
 * RUNTIME GUARD: on module load, probes Intl for Arabic locale support.
 * If Intl does NOT render Arabic month names (e.g. Hermes without ICU data),
 * `intlArabicOK` is false and a console.warn is emitted.
 * Mitigation if intlArabicOK=false: install @formatjs/intl-datetimeformat +
 * its locale data as a polyfill — only the internals of THIS file change,
 * callers remain untouched.
 */
import i18n from '../i18n';

// --- Runtime guard: probe Arabic Intl support once at load ---
const probe = new Intl.DateTimeFormat('ar', { month: 'long' }).format(new Date(2026, 0, 15));
export const intlArabicOK = /[\u0600-\u06FF]/.test(probe);

if (!intlArabicOK) {
  console.warn(
    '[formatDate] Intl does not render Arabic on this engine — date formatting ' +
    'will be degraded in ar; consider adding @formatjs/intl-datetimeformat polyfill.',
  );
}

// --- Helpers ---

function locale(): string {
  return i18n.language || 'fr';
}

function isValid(d: Date): boolean {
  return d instanceof Date && !Number.isNaN(d.getTime());
}

// --- Public API ---

/** "samedi 14 juin" / "Saturday, June 14" / "السبت، ١٤ يونيو" */
export function formatDateLong(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  }).format(date);
}

/** "14 juin 2026, 14:30" / "Jun 14, 2026, 2:30 PM" */
export function formatDateTime(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

/** "14/06/2026 14:30" — short date + time for booking lists */
export function formatDateTimeShort(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

/** "Juin 2026" / "June 2026" — calendar header */
export function formatMonthYear(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    month: 'long',
    year: 'numeric',
  }).format(date);
}

/** "SAM" / "SAT" / "س" — short weekday for calendar chips */
export function formatDayShort(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    weekday: 'short',
  }).format(date).toUpperCase();
}

/** "janvier" / "January" / "يناير" — full month name */
export function formatMonthName(date: Date): string {
  if (!isValid(date)) return '';
  return new Intl.DateTimeFormat(locale(), {
    month: 'long',
  }).format(date);
}
