import { router } from 'expo-router';

/**
 * Navigate to the correct profile screen based on user type.
 *
 * Backend UserType enum values: 'client' | 'hairstylist' | 'salon_owner' | 'admin'
 * PostResponse.authorUserType uses these same values.
 *
 * For salon entities (not a UserType but used in salon contexts), pass 'salon'.
 */
export function navigateToProfile(
  userType: string | undefined | null,
  userId: string | undefined | null,
): void {
  if (!userId) return;

  switch (userType) {
    case 'client':
      router.push(`/profile/client/${userId}`);
      break;
    case 'hairstylist':
      router.push(`/profile/coiffeur/${userId}`);
      break;
    case 'salon_owner':
      router.push(`/profile/owner/${userId}`);
      break;
    case 'salon':
      router.push(`/profile/salon/${userId}`);
      break;
    case 'admin':
      // Admin profiles not supported — treat as client for now
      router.push(`/profile/client/${userId}`);
      break;
    default:
      // Unknown userType — do nothing, don't crash
      if (__DEV__) {
        console.warn(`navigateToProfile: unknown userType "${userType}" for userId "${userId}"`);
      }
      break;
  }
}
