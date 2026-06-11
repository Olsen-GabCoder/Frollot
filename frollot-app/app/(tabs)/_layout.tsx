import { Tabs } from 'expo-router';
import { View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../src/theme';
import { MaterialIcons } from '@expo/vector-icons';

export default function TabsLayout() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors } = theme;

  const pillStyle = {
    backgroundColor: colors.secondaryContainer,
    borderRadius: 16,
    paddingHorizontal: 20,
    paddingVertical: 4,
  };

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.onSurfaceVariant,
        tabBarStyle: {
          backgroundColor: colors.surface,
          borderTopColor: colors.outlineVariant,
          height: 64,
          paddingBottom: 8,
          paddingTop: 4,
        },
        tabBarLabelStyle: {
          fontSize: 11,
          fontFamily: 'Manrope_600SemiBold',
          letterSpacing: 0.5,
        },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: t('tabs.home'),
          tabBarIcon: ({ color, size, focused }) => (
            <View style={focused ? pillStyle : undefined}>
              <MaterialIcons name="home" size={size} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="social"
        options={{
          title: t('tabs.social'),
          tabBarIcon: ({ color, size, focused }) => (
            <View style={focused ? pillStyle : undefined}>
              <MaterialIcons name="photo-library" size={size} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="explore"
        options={{
          title: t('tabs.explore'),
          tabBarIcon: ({ color, size, focused }) => (
            <View style={focused ? pillStyle : undefined}>
              <MaterialIcons name="explore" size={size} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="bookings"
        options={{
          title: t('tabs.bookings'),
          tabBarIcon: ({ color, size, focused }) => (
            <View style={focused ? pillStyle : undefined}>
              <MaterialIcons name="calendar-today" size={size} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: t('tabs.profile'),
          tabBarIcon: ({ color, size, focused }) => (
            <View style={focused ? pillStyle : undefined}>
              <MaterialIcons name="person" size={size} color={color} />
            </View>
          ),
        }}
      />
    </Tabs>
  );
}
