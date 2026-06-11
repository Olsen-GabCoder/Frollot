export const LightColors: ColorScheme = {
  // Primary - Prune raffinee
  primary: '#6B4E78',
  onPrimary: '#FFFFFF',
  primaryContainer: '#EEE2F1',
  onPrimaryContainer: '#281733',

  // Secondary - Rose poudre
  secondary: '#A4677F',
  onSecondary: '#FFFFFF',
  secondaryContainer: '#F7E2EA',
  onSecondaryContainer: '#38202A',

  // Tertiary - Champagne/Or doux
  tertiary: '#A98750',
  onTertiary: '#FFFFFF',
  tertiaryContainer: '#F4E9D4',
  onTertiaryContainer: '#362814',

  // State
  success: '#4C7A57',
  onSuccess: '#FFFFFF',
  successContainer: '#D8EDDD',
  onSuccessContainer: '#14301B',

  error: '#B3261E',
  onError: '#FFFFFF',
  errorContainer: '#F9DEDC',
  onErrorContainer: '#410E0B',

  warning: '#946A1A',
  onWarning: '#FFFFFF',
  warningContainer: '#F6E8CA',
  onWarningContainer: '#2E2106',

  info: '#3C6A8A',
  onInfo: '#FFFFFF',
  infoContainer: '#D8E8F2',
  onInfoContainer: '#0E2533',

  // Neutrals
  background: '#FBF7F9',
  onBackground: '#221A26',
  surface: '#FFFFFF',
  onSurface: '#221A26',
  surfaceVariant: '#ECE3EA',
  onSurfaceVariant: '#6C5F6E',
  surfaceDim: '#DFD7DD',
  surfaceBright: '#FBF7F9',
  surfaceContainerLowest: '#FFFFFF',
  surfaceContainer: '#F6F0F4',
  surfaceContainerHigh: '#F0E9EE',
  surfaceContainerHighest: '#EAE1E8',

  outline: '#897C8A',
  outlineVariant: '#DACFD9',

  inverseSurface: '#372E3A',
  inverseOnSurface: '#F6EEF3',
  inversePrimary: '#D9BBE2',

  scrim: '#000000',
  shadow: '#000000',
};

export const DarkColors: ColorScheme = {
  // Primary
  primary: '#D9BBE2',
  onPrimary: '#3C2A48',
  primaryContainer: '#534060',
  onPrimaryContainer: '#F1DCF6',

  // Secondary
  secondary: '#E8B6C8',
  onSecondary: '#502738',
  secondaryContainer: '#6B3D4F',
  onSecondaryContainer: '#FBDDE8',

  // Tertiary
  tertiary: '#E0C28A',
  onTertiary: '#3E2E10',
  tertiaryContainer: '#574322',
  onTertiaryContainer: '#FBE6BF',

  // State
  success: '#9FD3A8',
  onSuccess: '#16321D',
  successContainer: '#324C39',
  onSuccessContainer: '#BBEFC3',

  error: '#F2B8B5',
  onError: '#601410',
  errorContainer: '#8C1D18',
  onErrorContainer: '#F9DEDC',

  warning: '#E6C277',
  onWarning: '#2E2106',
  warningContainer: '#5B4516',
  onWarningContainer: '#F8E3BB',

  info: '#A6CBE2',
  onInfo: '#0E2533',
  infoContainer: '#284C5F',
  onInfoContainer: '#CDE6F4',

  // Neutrals
  background: '#15111A',
  onBackground: '#E9DFE9',
  surface: '#15111A',
  onSurface: '#E9DFE9',
  surfaceVariant: '#4A3F4D',
  onSurfaceVariant: '#CDBFCC',
  surfaceDim: '#15111A',
  surfaceBright: '#3C3640',
  surfaceContainerLowest: '#100B14',
  surfaceContainer: '#211B26',
  surfaceContainerHigh: '#2C2531',
  surfaceContainerHighest: '#37303C',

  outline: '#978A98',
  outlineVariant: '#4A3F4D',

  inverseSurface: '#E9DFE9',
  inverseOnSurface: '#342B38',
  inversePrimary: '#6B4E78',

  scrim: '#000000',
  shadow: '#000000',
};

export interface ColorScheme {
  primary: string;
  onPrimary: string;
  primaryContainer: string;
  onPrimaryContainer: string;
  secondary: string;
  onSecondary: string;
  secondaryContainer: string;
  onSecondaryContainer: string;
  tertiary: string;
  onTertiary: string;
  tertiaryContainer: string;
  onTertiaryContainer: string;
  success: string;
  onSuccess: string;
  successContainer: string;
  onSuccessContainer: string;
  error: string;
  onError: string;
  errorContainer: string;
  onErrorContainer: string;
  warning: string;
  onWarning: string;
  warningContainer: string;
  onWarningContainer: string;
  info: string;
  onInfo: string;
  infoContainer: string;
  onInfoContainer: string;
  background: string;
  onBackground: string;
  surface: string;
  onSurface: string;
  surfaceVariant: string;
  onSurfaceVariant: string;
  surfaceDim: string;
  surfaceBright: string;
  surfaceContainerLowest: string;
  surfaceContainer: string;
  surfaceContainerHigh: string;
  surfaceContainerHighest: string;
  outline: string;
  outlineVariant: string;
  inverseSurface: string;
  inverseOnSurface: string;
  inversePrimary: string;
  scrim: string;
  shadow: string;
}
