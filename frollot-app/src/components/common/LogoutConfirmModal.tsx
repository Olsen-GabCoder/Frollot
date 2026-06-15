import { Modal, Pressable, Text, TouchableOpacity, StyleSheet, View } from 'react-native';
import { router } from 'expo-router';
import { MaterialIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../theme';
import { useAuthStore } from '../../stores/authStore';

interface LogoutConfirmModalProps {
  visible: boolean;
  onClose: () => void;
}

/**
 * Modal de confirmation de déconnexion partagé (profil + paramètres).
 * Encapsule la confirmation ET la logique de déconnexion.
 */
export function LogoutConfirmModal({ visible, onClose }: LogoutConfirmModalProps) {
  const { colors, typography: typo } = useTheme();
  const { t } = useTranslation();
  const { logout } = useAuthStore();

  const handleLogout = async () => {
    onClose();
    await logout();
    router.replace('/(auth)/login');
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.overlay} onPress={onClose}>
        <Pressable onPress={(e) => e.stopPropagation()} style={[styles.card, { backgroundColor: colors.surface }]}>
          <MaterialIcons name="logout" size={36} color={colors.error} style={{ alignSelf: 'center', marginBottom: 12 }} />
          <Text style={[typo.titleMedium, { color: colors.onSurface, textAlign: 'center', marginBottom: 8 }]}>
            {t('auth.logoutConfirmTitle')}
          </Text>
          <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 20 }]}>
            {t('auth.logoutConfirmHint')}
          </Text>
          <View style={styles.actions}>
            <TouchableOpacity style={[styles.btn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={onClose}>
              <Text style={[styles.btnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.btn, { backgroundColor: colors.error }]} onPress={handleLogout}>
              <Text style={[styles.btnText, { color: colors.onError }]}>{t('auth.logoutButton')}</Text>
            </TouchableOpacity>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  card: { width: '100%', maxWidth: 340, borderRadius: 24, padding: 24 },
  actions: { flexDirection: 'row', gap: 12 },
  btn: { flex: 1, paddingVertical: 14, borderRadius: 999, alignItems: 'center' },
  btnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
