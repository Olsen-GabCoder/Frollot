import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

interface BookingStepperProps {
  steps?: string[];
  currentStep?: number;
}

export function BookingStepper({ steps = ['Service', 'Date', 'Coiffeur', 'Recap'], currentStep = 0 }: BookingStepperProps) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      {steps.map((step, i) => {
        const done = i < currentStep;
        const active = i === currentStep;
        return (
          <React.Fragment key={i}>
            <View style={styles.stepCol}>
              <View style={[
                styles.circle,
                done
                  ? { backgroundColor: colors.success }
                  : active
                    ? { backgroundColor: colors.primary }
                    : { backgroundColor: colors.surfaceContainerHigh, borderWidth: 1, borderColor: colors.outlineVariant },
              ]}>
                {done ? (
                  <MaterialCommunityIcons name="check" size={16} color={colors.onSuccess} />
                ) : (
                  <Text style={[styles.circleText, { color: (done || active) ? colors.onPrimary : colors.onSurfaceVariant }]}>
                    {i + 1}
                  </Text>
                )}
              </View>
              <Text style={[styles.label, { color: active ? colors.primary : colors.onSurfaceVariant }]}>
                {step}
              </Text>
            </View>
            {i < steps.length - 1 && (
              <View style={[styles.line, { backgroundColor: done ? colors.success : colors.outlineVariant }]} />
            )}
          </React.Fragment>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 6,
    paddingTop: 8,
  },
  stepCol: {
    alignItems: 'center',
    gap: 5,
  },
  circle: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  circleText: {
    fontFamily: 'Manrope-Bold',
    fontWeight: '700',
    fontSize: 13,
  },
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 11,
    fontWeight: '600',
  },
  line: {
    flex: 1,
    height: 2,
    marginHorizontal: 4,
    marginBottom: 18,
  },
});
