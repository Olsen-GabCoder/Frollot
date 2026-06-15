import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { Button } from '../ui/Button';
import { useTheme } from '../../theme';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  return (
    <View style={styles.container}>
      <MaterialCommunityIcons name="alert-circle-outline" size={64} color={colors.error} />
      <Text style={[styles.message, { color: colors.onSurfaceVariant }]}>{message ?? t('common.states.error')}</Text>
      {onRetry && (
        <Button kind="tonal" onPress={onRetry} style={{ marginTop: 16 }}>{t('common.actions.retry')}</Button>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 32, gap: 8 },
  message: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
});
