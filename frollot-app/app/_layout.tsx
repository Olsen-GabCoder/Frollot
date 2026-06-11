import { useEffect } from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useColorScheme } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';
import { useFonts } from 'expo-font';
import {
  CormorantGaramond_400Regular,
  CormorantGaramond_600SemiBold,
  CormorantGaramond_700Bold,
} from '@expo-google-fonts/cormorant-garamond';
import {
  Manrope_400Regular,
  Manrope_500Medium,
  Manrope_600SemiBold,
  Manrope_700Bold,
} from '@expo-google-fonts/manrope';
import { useAuthStore } from '../src/stores/authStore';
import { usePreferencesStore } from '../src/stores/preferencesStore';
import '../src/i18n';

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const { initialize: initAuth, isInitialized: authReady } = useAuthStore();
  const { initialize: initPrefs, isInitialized: prefsReady, themeMode } = usePreferencesStore();

  const [fontsLoaded] = useFonts({
    'CormorantGaramond-Regular': CormorantGaramond_400Regular,
    'CormorantGaramond-SemiBold': CormorantGaramond_600SemiBold,
    'CormorantGaramond-Bold': CormorantGaramond_700Bold,
    'Manrope-Regular': Manrope_400Regular,
    'Manrope-Medium': Manrope_500Medium,
    'Manrope-SemiBold': Manrope_600SemiBold,
    'Manrope-Bold': Manrope_700Bold,
  });

  useEffect(() => {
    Promise.all([initAuth(), initPrefs()]).finally(() => {
      if (fontsLoaded) {
        SplashScreen.hideAsync();
      }
    });
  }, []);

  useEffect(() => {
    if (fontsLoaded && authReady && prefsReady) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded, authReady, prefsReady]);

  if (!fontsLoaded || !authReady || !prefsReady) {
    return null;
  }

  const effectiveScheme = themeMode === 'system' ? colorScheme : themeMode;

  return (
    <>
      <StatusBar style={effectiveScheme === 'dark' ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="(auth)" />
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="salon/[id]" options={{ headerShown: false }} />
        <Stack.Screen name="booking/[id]" options={{ headerShown: true, title: '' }} />
      </Stack>
    </>
  );
}
