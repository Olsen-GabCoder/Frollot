import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

interface ChipProps {
  children: string;
  selected?: boolean;
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
  onPress?: () => void;
}

export function Chip({ children, selected = false, icon, onPress }: ChipProps) {
  const { colors } = useTheme();
  const fg = selected ? colors.onPrimaryContainer : colors.onSurfaceVariant;
  return (
    <TouchableOpacity
      onPress={onPress}
      activeOpacity={0.7}
      style={[
        styles.chip,
        selected
          ? { backgroundColor: colors.primaryContainer, borderWidth: 1, borderColor: 'transparent' }
          : { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.outlineVariant },
      ]}
    >
      {icon && <MaterialCommunityIcons name={icon} size={16} color={fg} />}
      <Text style={[styles.label, { color: fg }]}>{children}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    height: 36,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
  },
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontWeight: '600',
    fontSize: 13,
  },
});
