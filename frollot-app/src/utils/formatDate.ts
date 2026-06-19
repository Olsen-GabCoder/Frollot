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

/**
 * Relative short date for posts: "a l'instant", "il y a 5 min", "il y a 3 h",
 * "hier", then short date ("18 juin" / "Jun 18").
 * Requires i18n keys common.time.{justNow,minutesAgo,hoursAgo,yesterday}.
 */
export function formatRelativeShort(date: Date, t: (key: string, opts?: Record<string, unknown>) => string): string {
  if (!isValid(date)) return '';
  const now = Date.now();
  const diffMs = now - date.getTime();
  if (diffMs < 0) return formatDateShort(date);

  const diffMin = Math.floor(diffMs / 60_000);
  if (diffMin < 1) return t('common.time.justNow');
  if (diffMin < 60) return t('common.time.minutesAgo', { count: diffMin });

  const diffH = Math.floor(diffMin / 60);
  if (diffH < 24) return t('common.time.hoursAgo', { count: diffH });

  const today = new Date();
  const yesterday = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 1);
  if (
    date.getFullYear() === yesterday.getFullYear() &&
    date.getMonth() === yesterday.getMonth() &&
    date.getDate() === yesterday.getDate()
  ) {
    return t('common.time.yesterday');
  }

  return formatDateShort(date);
}

/** "18 juin" / "Jun 18" — short date without year (same year) or with year */
function formatDateShort(date: Date): string {
  const now = new Date();
  const sameYear = date.getFullYear() === now.getFullYear();
  return new Intl.DateTimeFormat(locale(), {
    day: 'numeric',
    month: 'short',
    ...(sameYear ? {} : { year: 'numeric' }),
  }).format(date);
}
