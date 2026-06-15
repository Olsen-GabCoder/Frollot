import { Platform } from 'react-native';

/**
 * Base URL for the API server, matching the Axios client configuration.
 * Used to resolve relative media paths and rewrite hardcoded 10.0.2.2 URLs.
 */
const API_BASE_URL = __DEV__
  ? Platform.select({
      android: 'http://10.0.2.2:8090',
      ios: 'http://localhost:8090',
      default: 'http://localhost:8090',
    })!
  : 'https://api.frollot.com';

/**
 * Known hosts that may be stored in the database but are unreachable
 * from certain platforms (e.g., 10.0.2.2 is the Android emulator's
 * alias for localhost, unreachable from web browsers).
 */
const REWRITABLE_HOSTS = ['http://10.0.2.2:8090'];

/**
 * Resolves a media URL to an absolute URL reachable from the current client.
 *
 * Handles three cases:
 * 1. null/undefined/empty → returns undefined (no image)
 * 2. Relative path (/uploads/...) → prefixes with API_BASE_URL
 * 3. Absolute URL with unreachable host (http://10.0.2.2:8090/...) → rewrites host to API_BASE_URL
 * 4. Any other absolute URL → returns as-is
 */
export function resolveMediaUrl(raw: string | null | undefined): string | undefined {
  if (!raw) return undefined;

  // Case 2: relative path
  if (raw.startsWith('/')) {
    return `${API_BASE_URL}${raw}`;
  }

  // Case 3: rewrite unreachable hosts
  for (const host of REWRITABLE_HOSTS) {
    if (raw.startsWith(host)) {
      return `${API_BASE_URL}${raw.substring(host.length)}`;
    }
  }

  // Case 4: already a valid absolute URL
  return raw;
}
