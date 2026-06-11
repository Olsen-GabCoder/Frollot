import React from 'react';
import { View, TouchableOpacity, StyleSheet, ViewStyle } from 'react-native';
import { useTheme } from '../../theme';

interface CardProps {
  children: React.ReactNode;
  onPress?: () => void;
  style?: ViewStyle;
  noPadding?: boolean;
}

export function Card({ children, onPress, style, noPadding = false }: CardProps) {
  const { colors } = useTheme();
  const content = (
    <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant + '4D' }, noPadding ? undefined : styles.padding, style]}>
      {children}
    </View>
  );
  if (onPress) {
    return (
      <TouchableOpacity activeOpacity={0.85} onPress={onPress}>
        {content}
      </TouchableOpacity>
    );
  }
  return content;
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 0.5,
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.12,
    shadowRadius: 3,
    elevation: 1,
  },
  padding: {
    padding: 16,
  },
});
