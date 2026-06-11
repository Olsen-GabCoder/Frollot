import React, { useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withTiming, withDelay, runOnJS } from 'react-native-reanimated';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

export type ToastType = 'success' | 'error' | 'info';

interface ToastProps {
  message: string;
  type?: ToastType;
  visible: boolean;
  onDismiss?: () => void;
  duration?: number;
}

export function Toast({ message, type = 'info', visible, onDismiss, duration = 3000 }: ToastProps) {
  const { colors } = useTheme();
  const toastConfig: Record<ToastType, { bg: string; fg: string; icon: keyof typeof MaterialCommunityIcons.glyphMap }> = {
    success: { bg: colors.successContainer, fg: colors.onSuccessContainer, icon: 'check-circle' },
    error:   { bg: colors.errorContainer, fg: colors.onErrorContainer, icon: 'alert-circle' },
    info:    { bg: colors.primaryContainer, fg: colors.onPrimaryContainer, icon: 'information' },
  };

  const translateY = useSharedValue(-100);
  const opacity = useSharedValue(0);

  useEffect(() => {
    if (visible) {
      translateY.value = withTiming(0, { duration: 300 });
      opacity.value = withTiming(1, { duration: 300 });
      if (onDismiss) {
        translateY.value = withDelay(duration, withTiming(-100, { duration: 300 }, () => {
          runOnJS(onDismiss)();
        }));
        opacity.value = withDelay(duration, withTiming(0, { duration: 300 }));
      }
    } else {
      translateY.value = withTiming(-100, { duration: 300 });
      opacity.value = withTiming(0, { duration: 300 });
    }
  }, [visible]);

  const animStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
    opacity: opacity.value,
  }));

  const config = toastConfig[type];

  return (
    <Animated.View style={[styles.container, animStyle, { backgroundColor: config.bg, borderColor: colors.outlineVariant }]}>
      <MaterialCommunityIcons name={config.icon} size={22} color={config.fg} />
      <Text style={[styles.message, { color: config.fg }]}>{message}</Text>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 50,
    left: 16,
    right: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 16,
    borderRadius: 16,
    borderWidth: 0.5,
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.12,
    shadowRadius: 3,
    elevation: 1,
    zIndex: 999,
  },
  message: {
    flex: 1,
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
});
