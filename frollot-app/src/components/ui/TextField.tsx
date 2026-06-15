import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, TextInputProps, Platform } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../theme';

interface TextFieldProps extends Omit<TextInputProps, 'style'> {
  label: string;
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
  trailingIcon?: keyof typeof MaterialCommunityIcons.glyphMap;
  onTrailingPress?: () => void;
  error?: string;
}

export function TextField({ label, icon, trailingIcon, onTrailingPress, error, onFocus, onBlur, ...rest }: TextFieldProps) {
  const { colors } = useTheme();
  const [focused, setFocused] = useState(false);
  const hasError = !!error;
  const activeColor = hasError ? colors.error : focused ? colors.primary : colors.onSurfaceVariant;
  const borderColor = hasError ? colors.error : focused ? colors.primary : colors.outline;
  const borderWidth = focused ? 2 : 1;

  return (
    <View>
      <Text style={[styles.label, { color: activeColor }]}>{label}</Text>
      <View style={[styles.container, { borderWidth, borderColor, backgroundColor: colors.surface }, rest.multiline && styles.containerMultiline]}>
        {icon && <MaterialCommunityIcons name={icon} size={22} color={activeColor} style={rest.multiline ? { marginTop: 14 } : undefined} />}
        <TextInput
          style={[styles.input, { color: colors.onSurface }, rest.multiline && styles.inputMultiline]}
          placeholderTextColor={colors.onSurfaceVariant}
          onFocus={(e) => { setFocused(true); onFocus?.(e); }}
          onBlur={(e) => { setFocused(false); onBlur?.(e); }}
          {...rest}
        />
        {trailingIcon && (
          <TouchableOpacity onPress={onTrailingPress} hitSlop={8}>
            <MaterialCommunityIcons name={trailingIcon} size={22} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
        )}
      </View>
      {error && <Text style={[styles.error, { color: colors.error }]}>{error}</Text>}
    </View>
  );
}

export function PasswordTextField({ label, ...rest }: Omit<TextFieldProps, 'trailingIcon' | 'onTrailingPress' | 'secureTextEntry'>) {
  const { t } = useTranslation();
  const [visible, setVisible] = useState(false);
  return (
    <TextField
      label={label || t('common.fields.password')}
      icon="lock"
      secureTextEntry={!visible}
      trailingIcon={visible ? 'eye-off' : 'eye'}
      onTrailingPress={() => setVisible(!visible)}
      {...rest}
    />
  );
}

const styles = StyleSheet.create({
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 12.5,
    fontWeight: '600',
    marginBottom: 6,
    letterSpacing: 0.3,
  },
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    height: 56,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  containerMultiline: {
    height: undefined,
    minHeight: 100,
    alignItems: 'flex-start',
    paddingVertical: 12,
  },
  inputMultiline: {
    textAlignVertical: 'top',
    minHeight: 76,
  },
  input: {
    flex: 1,
    fontFamily: 'Manrope-Regular',
    fontSize: 16,
    lineHeight: 24,
    padding: 0,
    margin: 0,
    borderWidth: 0,
    backgroundColor: 'transparent',
    ...Platform.select({
      web: {
        outlineStyle: 'none',
        outlineWidth: 0,
      } as any,
    }),
  },
  error: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    marginTop: 4,
  },
});
