import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

type BadgeTone = 'success' | 'warning' | 'error' | 'info' | 'primary';

interface StatusBadgeProps {
  children: string;
  tone?: BadgeTone;
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
}

export function StatusBadge({ children, tone = 'success', icon }: StatusBadgeProps) {
  const { colors } = useTheme();
  const toneColors: Record<BadgeTone, [string, string]> = {
    success: [colors.successContainer, colors.onSuccessContainer],
    warning: [colors.warningContainer, colors.onWarningContainer],
    error:   [colors.errorContainer, colors.onErrorContainer],
    info:    [colors.infoContainer, colors.onInfoContainer],
    primary: [colors.primaryContainer, colors.onPrimaryContainer],
  };
  const [bg, fg] = toneColors[tone];
  return (
    <View style={[styles.badge, { backgroundColor: bg }]}>
      {icon && <MaterialCommunityIcons name={icon} size={14} color={fg} />}
      <Text style={[styles.label, { color: fg }]}>{children}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 999,
    alignSelf: 'flex-start',
  },
  label: {
    fontFamily: 'Manrope-Bold',
    fontWeight: '700',
    fontSize: 11.5,
  },
});
