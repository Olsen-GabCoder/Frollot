import { TextStyle } from 'react-native';

const FONT_DISPLAY = 'CormorantGaramond_600SemiBold';
const FONT_SANS = 'Manrope_400Regular';
const FONT_SANS_MEDIUM = 'Manrope_500Medium';
const FONT_SANS_SEMIBOLD = 'Manrope_600SemiBold';
const FONT_SANS_BOLD = 'Manrope_700Bold';

export const typography = {
  displayLarge: {
    fontFamily: FONT_DISPLAY,
    fontSize: 57,
    lineHeight: 60,
    letterSpacing: -1,
  } as TextStyle,

  displayMedium: {
    fontFamily: FONT_DISPLAY,
    fontSize: 45,
    lineHeight: 50,
    letterSpacing: -0.5,
  } as TextStyle,

  displaySmall: {
    fontFamily: FONT_DISPLAY,
    fontSize: 36,
    lineHeight: 42,
  } as TextStyle,

  headlineLarge: {
    fontFamily: FONT_DISPLAY,
    fontSize: 32,
    lineHeight: 38,
  } as TextStyle,

  headlineMedium: {
    fontFamily: FONT_DISPLAY,
    fontSize: 28,
    lineHeight: 34,
  } as TextStyle,

  headlineSmall: {
    fontFamily: FONT_DISPLAY,
    fontSize: 24,
    lineHeight: 30,
  } as TextStyle,

  titleLarge: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 22,
    lineHeight: 28,
  } as TextStyle,

  titleMedium: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 16,
    lineHeight: 24,
    letterSpacing: 0.15,
  } as TextStyle,

  titleSmall: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 14,
    lineHeight: 20,
    letterSpacing: 0.1,
  } as TextStyle,

  bodyLarge: {
    fontFamily: FONT_SANS,
    fontSize: 16,
    lineHeight: 24,
    letterSpacing: 0.15,
  } as TextStyle,

  bodyMedium: {
    fontFamily: FONT_SANS,
    fontSize: 14,
    lineHeight: 20,
    letterSpacing: 0.2,
  } as TextStyle,

  bodySmall: {
    fontFamily: FONT_SANS,
    fontSize: 12,
    lineHeight: 16,
    letterSpacing: 0.3,
  } as TextStyle,

  labelLarge: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 14,
    lineHeight: 20,
    letterSpacing: 0.1,
  } as TextStyle,

  labelMedium: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 12,
    lineHeight: 16,
    letterSpacing: 0.5,
  } as TextStyle,

  labelSmall: {
    fontFamily: FONT_SANS_SEMIBOLD,
    fontSize: 11,
    lineHeight: 16,
    letterSpacing: 0.5,
  } as TextStyle,

  overline: {
    fontFamily: FONT_SANS_BOLD,
    fontSize: 11,
    lineHeight: 16,
    letterSpacing: 2,
    textTransform: 'uppercase',
  } as TextStyle,
} as const;

export type TypographyKey = keyof typeof typography;
