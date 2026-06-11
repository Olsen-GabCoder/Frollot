import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useTheme } from '../../theme';

interface SectionHeaderProps {
  title: string;
  action?: string;
  onActionPress?: () => void;
}

export function SectionHeader({ title, action = 'Voir tout', onActionPress }: SectionHeaderProps) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <Text style={[styles.title, { color: colors.onBackground }]}>{title}</Text>
      {onActionPress && (
        <TouchableOpacity onPress={onActionPress}>
          <Text style={[styles.action, { color: colors.primary }]}>{action}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
  },
  title: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 22,
    fontWeight: '600',
  },
  action: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
});
