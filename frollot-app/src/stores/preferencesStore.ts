import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import i18next from 'i18next';

type ThemeMode = 'system' | 'light' | 'dark';
type Language = 'en' | 'fr' | 'es' | 'de' | 'ar';

const PREFS_THEME_KEY = 'frollot_theme_mode';
const PREFS_LANG_KEY = 'frollot_language';

interface PreferencesStore {
  themeMode: ThemeMode;
  language: Language;
  isInitialized: boolean;
  setThemeMode: (mode: ThemeMode) => Promise<void>;
  setLanguage: (lang: Language) => Promise<void>;
  initialize: () => Promise<void>;
}

export const usePreferencesStore = create<PreferencesStore>((set) => ({
  themeMode: 'system',
  language: 'fr',
  isInitialized: false,

  setThemeMode: async (mode: ThemeMode) => {
    await AsyncStorage.setItem(PREFS_THEME_KEY, mode);
    set({ themeMode: mode });
  },

  setLanguage: async (lang: Language) => {
    await AsyncStorage.setItem(PREFS_LANG_KEY, lang);
    await i18next.changeLanguage(lang);
    set({ language: lang });
  },

  initialize: async () => {
    try {
      const [theme, lang] = await Promise.all([
        AsyncStorage.getItem(PREFS_THEME_KEY),
        AsyncStorage.getItem(PREFS_LANG_KEY),
      ]);
      const language = (lang as Language) || 'fr';
      await i18next.changeLanguage(language);
      set({
        themeMode: (theme as ThemeMode) || 'system',
        language,
        isInitialized: true,
      });
    } catch {
      set({ isInitialized: true });
    }
  },
}));
