import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Button } from '../ui/Button';
import { useTheme } from '../../theme';

interface EmptyStateProps {
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
  title: string;
  message?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({ icon = 'inbox-outline', title, message, actionLabel, onAction }: EmptyStateProps) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <MaterialCommunityIcons name={icon} size={64} color={colors.outlineVariant} />
      <Text style={[styles.title, { color: colors.onSurface }]}>{title}</Text>
      {message && <Text style={[styles.message, { color: colors.onSurfaceVariant }]}>{message}</Text>}
      {actionLabel && onAction && (
        <Button kind="tonal" onPress={onAction} style={{ marginTop: 16 }}>{actionLabel}</Button>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 8 },
  title: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600', textAlign: 'center' },
  message: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
});
