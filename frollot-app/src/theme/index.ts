import { useColorScheme } from 'react-native';
import { LightColors, DarkColors, ColorScheme } from './colors';
import { typography } from './typography';
import { radius, spacing, elevation } from './tokens';
import { usePreferencesStore } from '../stores/preferencesStore';

export interface Theme {
  colors: ColorScheme;
  typography: typeof typography;
  radius: typeof radius;
  spacing: typeof spacing;
  elevation: typeof elevation;
  isDark: boolean;
}

export function createTheme(isDark: boolean): Theme {
  return {
    colors: isDark ? DarkColors : LightColors,
    typography,
    radius,
    spacing,
    elevation,
    isDark,
  };
}

export function useTheme(): Theme {
  const systemScheme = useColorScheme();
  const themeMode = usePreferencesStore((s) => s.themeMode);
  const isDark =
    themeMode === 'dark' ? true :
    themeMode === 'light' ? false :
    systemScheme === 'dark';
  return createTheme(isDark);
}

export { LightColors, DarkColors, typography, radius, spacing, elevation };
