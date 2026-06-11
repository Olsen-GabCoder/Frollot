import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { LinearGradient } from 'expo-linear-gradient' ;
import { useTheme } from '../../theme';
import { resolveMediaUrl } from '../../utils/media';

type AvatarTone = 'primary' | 'secondary' | 'tertiary';

interface AvatarProps {
  initials?: string;
  size?: number;
  ring?: boolean;
  tone?: AvatarTone;
  imageUrl?: string;
}

export function Avatar({ initials = 'F', size = 40, ring = false, tone = 'primary', imageUrl }: AvatarProps) {
  const { colors } = useTheme();
  const toneBg: Record<AvatarTone, string> = {
    primary: colors.primary,
    secondary: colors.secondary,
    tertiary: colors.tertiary,
  };

  const inner = imageUrl ? (
    <Image
      source={{ uri: resolveMediaUrl(imageUrl) }}
      style={{ width: size, height: size, borderRadius: size / 2 }}
      contentFit="cover"
    />
  ) : (
    <View style={[styles.circle, { width: size, height: size, borderRadius: size / 2, backgroundColor: toneBg[tone] }]}>
      <Text style={[styles.initials, { fontSize: size * 0.4, color: colors.onPrimary }]}>{initials}</Text>
    </View>
  );

  if (!ring) return inner;

  return (
    <View style={[styles.ringOuter, { borderRadius: (size + 8) / 2, borderWidth: 2, borderColor: colors.secondary }]}>
      <View style={[styles.ringInner, { padding: 2, borderRadius: (size + 4) / 2, backgroundColor: colors.surface }]}>
        {inner}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  circle: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  initials: {
    fontFamily: 'Manrope-Bold',
    fontWeight: '700',
  },
  ringOuter: {
    padding: 0,
  },
  ringInner: {},
});
