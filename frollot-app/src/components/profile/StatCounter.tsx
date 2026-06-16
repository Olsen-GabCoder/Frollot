import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../../theme';

interface StatCounterProps {
  value: number;
  label: string;
}

function formatValue(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1).replace(/\.0$/, '')} M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1).replace(/\.0$/, '')} K`;
  return String(n);
}

export function StatCounter({ value, label }: StatCounterProps) {
  const { colors } = useTheme();

  return (
    <View style={styles.container}>
      <Text style={[styles.value, { color: colors.onBackground }]}>{formatValue(value)}</Text>
      <Text style={[styles.label, { color: colors.onSurfaceVariant }]}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    minWidth: 48,
  },
  value: {
    fontFamily: 'Manrope-Bold',
    fontSize: 18,
    fontWeight: '700',
  },
  label: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    marginTop: 2,
  },
});
