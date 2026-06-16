import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

interface TabDef {
  key: string;
  label: string;
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
}

interface ProfileTabBarProps {
  tabs: TabDef[];
  activeKey: string;
  onChange: (key: string) => void;
}

export function ProfileTabBar({ tabs, activeKey, onChange }: ProfileTabBarProps) {
  const { colors } = useTheme();

  return (
    <View style={[styles.row, { borderBottomColor: colors.outlineVariant }]}>
      {tabs.map((tab) => {
        const active = tab.key === activeKey;
        return (
          <TouchableOpacity
            key={tab.key}
            onPress={() => onChange(tab.key)}
            style={styles.tab}
            activeOpacity={0.7}
          >
            {tab.icon && (
              <MaterialCommunityIcons
                name={tab.icon}
                size={16}
                color={active ? colors.primary : colors.onSurfaceVariant}
                style={styles.icon}
              />
            )}
            <Text
              style={[
                styles.label,
                { color: active ? colors.primary : colors.onSurfaceVariant },
              ]}
            >
              {tab.label}
            </Text>
            {active && (
              <View style={[styles.indicator, { backgroundColor: colors.primary }]} />
            )}
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    gap: 22,
    borderBottomWidth: 1,
  },
  tab: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    position: 'relative',
  },
  icon: {
    marginEnd: 6,
  },
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
  indicator: {
    position: 'absolute',
    bottom: -1,
    start: 0,
    end: 0,
    height: 3,
    borderRadius: 3,
  },
});
