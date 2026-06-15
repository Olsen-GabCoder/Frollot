import React, { useState } from 'react';
import { Text, View, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../theme';
import { getFlagUrl } from '../../utils/countries';

interface CountryFlagProps {
  iso2: string;
  /** Diamètre du cercle (défaut 28) */
  size?: number;
}

/**
 * Drapeau pays : SVG flagcdn.com via expo-image (cache disque auto).
 * FALLBACK monogramme (cercle teinté + code ISO) si le drapeau ne charge pas
 * (offline au 1er chargement, CDN indisponible) — jamais un trou dans la ligne.
 */
export function CountryFlag({ iso2, size = 28 }: CountryFlagProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const [failed, setFailed] = useState(false);

  const circle = { width: size, height: size, borderRadius: size / 2 };

  if (failed) {
    return (
      <View style={[circle, styles.fallback, { backgroundColor: colors.primaryContainer }]}>
        <Text
          style={[
            styles.fallbackText,
            { color: colors.onPrimaryContainer, fontSize: size * 0.36 },
          ]}
        >
          {iso2.toUpperCase()}
        </Text>
      </View>
    );
  }

  return (
    <Image
      source={{ uri: getFlagUrl(iso2) }}
      style={[circle, { backgroundColor: colors.surfaceContainerHigh }]}
      contentFit="cover"
      transition={120}
      onError={() => setFailed(true)}
      accessibilityLabel={t('phone.flagA11y', { iso: iso2.toUpperCase() })}
    />
  );
}

const styles = StyleSheet.create({
  fallback: { alignItems: 'center', justifyContent: 'center' },
  fallbackText: { fontFamily: 'Manrope-Bold', fontWeight: '700', letterSpacing: 0.5 },
});
