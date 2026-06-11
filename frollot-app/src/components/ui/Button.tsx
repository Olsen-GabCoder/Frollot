import React from 'react';
import { TouchableOpacity, Text, StyleSheet, ViewStyle, ActivityIndicator } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

type ButtonKind = 'primary' | 'outline' | 'text' | 'tonal' | 'secondary';

interface ButtonProps {
  children: string;
  kind?: ButtonKind;
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
  full?: boolean;
  onPress?: () => void;
  disabled?: boolean;
  loading?: boolean;
  style?: ViewStyle;
}

function getKindStyles(colors: ReturnType<typeof useTheme>['colors']): Record<ButtonKind, { bg: string; color: string; border?: string }> {
  return {
    primary:   { bg: colors.primary, color: colors.onPrimary },
    outline:   { bg: 'transparent', color: colors.primary, border: colors.outline },
    text:      { bg: 'transparent', color: colors.primary },
    tonal:     { bg: colors.primaryContainer, color: colors.onPrimaryContainer },
    secondary: { bg: colors.secondaryContainer, color: colors.onSecondaryContainer },
  };
}

export function Button({ children, kind = 'primary', icon, full = false, onPress, disabled, loading, style }: ButtonProps) {
  const { colors } = useTheme();
  const k = getKindStyles(colors)[kind];
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.8}
      style={[
        styles.base,
        { backgroundColor: k.bg },
        k.border ? { borderWidth: 1, borderColor: k.border } : undefined,
        full ? styles.full : undefined,
        disabled ? styles.disabled : undefined,
        style,
      ]}
    >
      {loading ? (
        <ActivityIndicator size={20} color={k.color} />
      ) : (
        <>
          {icon && <MaterialCommunityIcons name={icon} size={20} color={k.color} />}
          <Text style={[styles.label, { color: k.color }]}>{children}</Text>
        </>
      )}
    </TouchableOpacity>
  );
}

export const PrimaryButton = (props: Omit<ButtonProps, 'kind'>) => <Button kind="primary" {...props} />;
export const OutlineButton = (props: Omit<ButtonProps, 'kind'>) => <Button kind="outline" {...props} />;
export const TextButton = (props: Omit<ButtonProps, 'kind'>) => <Button kind="text" {...props} />;
export const TonalButton = (props: Omit<ButtonProps, 'kind'>) => <Button kind="tonal" {...props} />;

const styles = StyleSheet.create({
  base: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    height: 48,
    minHeight: 48,
    paddingHorizontal: 24,
    borderRadius: 999,
  },
  label: {
    fontFamily: 'Manrope-Bold',
    fontWeight: '700',
    fontSize: 15,
  },
  full: {
    width: '100%',
  },
  disabled: {
    opacity: 0.5,
  },
});
