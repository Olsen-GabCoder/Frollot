import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';
import { SectionHeader } from '../common/SectionHeader';
import { Card } from '../ui/Card';

interface InfoItem {
  icon: keyof typeof MaterialCommunityIcons.glyphMap;
  label: string;
  value: string;
  onEdit?: () => void;
}

interface ProfileInfoSectionProps {
  title: string;
  items: InfoItem[];
}

export function ProfileInfoSection({ title, items }: ProfileInfoSectionProps) {
  const { colors } = useTheme();

  if (items.length === 0) return null;

  return (
    <View>
      <SectionHeader title={title} />
      <Card style={styles.card}>
        {items.map((item, i) => (
          <View
            key={`${item.label}-${i}`}
            style={[
              styles.row,
              i < items.length - 1 && [styles.rowBorder, { borderBottomColor: colors.outlineVariant + '4D' }],
            ]}
          >
            <MaterialCommunityIcons
              name={item.icon}
              size={20}
              color={colors.onSurfaceVariant}
              style={styles.icon}
            />
            <View style={styles.content}>
              <Text style={[styles.label, { color: colors.onSurfaceVariant }]}>{item.label}</Text>
              <Text style={[styles.value, { color: colors.onBackground }]}>{item.value}</Text>
            </View>
            {item.onEdit && (
              <TouchableOpacity onPress={item.onEdit} hitSlop={8} activeOpacity={0.6}>
                <MaterialCommunityIcons name="pencil-outline" size={18} color={colors.onSurfaceVariant} />
              </TouchableOpacity>
            )}
          </View>
        ))}
      </Card>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: 20,
    marginTop: 8,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
  },
  rowBorder: {
    borderBottomWidth: 1,
  },
  icon: {
    marginEnd: 12,
  },
  content: {
    flex: 1,
  },
  label: {
    fontFamily: 'Manrope-Regular',
    fontSize: 11,
  },
  value: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 1,
  },
});
