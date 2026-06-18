import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';

interface AccessDeniedProps {
  message?: string;
  onBack?: () => void;
}

export function AccessDenied({ message, onBack }: AccessDeniedProps) {
  const { t } = useTranslation();
  const { colors } = useTheme();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (router.canGoBack()) {
      router.back();
    } else {
      router.replace('/(tabs)');
    }
  };

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      <View style={s.content}>
        <View style={[s.iconCircle, { backgroundColor: colors.errorContainer }]}>
          <MaterialCommunityIcons name="shield-alert-outline" size={40} color={colors.onErrorContainer} />
        </View>
        <Text style={[s.title, { color: colors.onSurface }]}>
          {t('permissions.accessDenied.title')}
        </Text>
        <Text style={[s.message, { color: colors.onSurfaceVariant }]}>
          {message || t('permissions.accessDenied.message')}
        </Text>
        <TouchableOpacity
          style={[s.button, { backgroundColor: colors.primary }]}
          onPress={handleBack}
          activeOpacity={0.7}
        >
          <Text style={[s.buttonText, { color: colors.onPrimary }]}>
            {t('permissions.accessDenied.back')}
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  content: { alignItems: 'center', paddingHorizontal: 32, gap: 16 },
  iconCircle: { width: 80, height: 80, borderRadius: 40, alignItems: 'center', justifyContent: 'center' },
  title: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 24, fontWeight: '600', textAlign: 'center' },
  message: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 22, textAlign: 'center' },
  button: { paddingVertical: 14, paddingHorizontal: 32, borderRadius: 999, marginTop: 8 },
  buttonText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
