import React from 'react';
import { View, Text, ActivityIndicator, StyleSheet } from 'react-native';
import { useTheme } from '../../theme';

interface LoadingStateProps {
  message?: string;
}

export function LoadingState({ message }: LoadingStateProps) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      <ActivityIndicator size={48} color={colors.primary} />
      {message && <Text style={[styles.message, { color: colors.onSurfaceVariant }]}>{message}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 12 },
  message: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
});
