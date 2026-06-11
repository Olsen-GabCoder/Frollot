import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { getLocales } from 'expo-localization';

import en from './en.json';
import fr from './fr.json';
import es from './es.json';
import de from './de.json';
import ar from './ar.json';

const deviceLanguage = getLocales()[0]?.languageCode ?? 'fr';

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    fr: { translation: fr },
    es: { translation: es },
    de: { translation: de },
    ar: { translation: ar },
  },
  lng: deviceLanguage,
  fallbackLng: 'fr',
  interpolation: {
    escapeValue: false,
  },
  compatibilityJSON: 'v4',
});

export default i18n;
