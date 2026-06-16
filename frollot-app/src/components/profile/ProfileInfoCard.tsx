import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

export interface InfoCardItem {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  value: string;
  onEdit?: () => void;
}

interface ProfileInfoCardProps {
  items: InfoCardItem[];
}

export function ProfileInfoCard({ items }: ProfileInfoCardProps) {
  const { colors } = useTheme();

  if (items.length === 0) return null;

  return (
    <View style={[styles.card, { backgroundColor: colors.surface }]}>
      {items.map((item, i) => {
        const row = (
          <View key={i}>
            <View style={styles.row}>
              <MaterialCommunityIcons name={item.icon} size={20} color={colors.onSurfaceVariant} />
              <Text style={[styles.value, { color: colors.onSurface }]}>{item.value}</Text>
              {item.onEdit && (
                <MaterialCommunityIcons name="pencil-outline" size={16} color={colors.onSurfaceVariant} />
              )}
            </View>
            {i < items.length - 1 && (
              <View style={[styles.sep, { backgroundColor: colors.outlineVariant + '4D' }]} />
            )}
          </View>
        );

        return item.onEdit ? (
          <TouchableOpacity key={i} onPress={item.onEdit} activeOpacity={0.7}>
            <View style={styles.row}>
              <MaterialCommunityIcons name={item.icon} size={20} color={colors.onSurfaceVariant} />
              <Text style={[styles.value, { color: colors.onSurface }]}>{item.value}</Text>
              <MaterialCommunityIcons name="pencil-outline" size={16} color={colors.onSurfaceVariant} />
            </View>
            {i < items.length - 1 && (
              <View style={[styles.sep, { backgroundColor: colors.outlineVariant + '4D' }]} />
            )}
          </TouchableOpacity>
        ) : row;
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    marginHorizontal: 16,
    marginTop: 12,
    paddingHorizontal: 16,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    gap: 12,
  },
  value: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    flex: 1,
  },
  sep: {
    height: 1,
  },
});
