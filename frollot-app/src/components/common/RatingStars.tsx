import React from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

interface RatingStarsProps {
  value?: number;
  size?: number;
  interactive?: boolean;
  onRatingChange?: (rating: number) => void;
}

export function RatingStars({ value = 0, size = 16, interactive = false, onRatingChange }: RatingStarsProps) {
  const { colors } = useTheme();
  return (
    <View style={styles.container}>
      {[1, 2, 3, 4, 5].map((i) => {
        const filled = i <= Math.round(value);
        const star = (
          <MaterialCommunityIcons
            key={i}
            name={filled ? 'star' : 'star-outline'}
            size={size}
            color={filled ? colors.tertiary : colors.outlineVariant}
          />
        );
        if (interactive && onRatingChange) {
          return (
            <TouchableOpacity key={i} onPress={() => onRatingChange(i)} hitSlop={4}>
              {star}
            </TouchableOpacity>
          );
        }
        return star;
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    gap: 1,
  },
});
