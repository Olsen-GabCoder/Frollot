import { useEffect, useState } from 'react';
import {
  View, Text, TouchableOpacity, Pressable, Modal, StyleSheet, ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { collectionsApi } from '../../api/portfolios';
import { useAuthStore } from '../../stores/authStore';
import { useTheme } from '../../theme';
import { CollectionResponse } from '../../types';
import type { ToastType } from '../ui';

interface CollectionPickerModalProps {
  /** Post à classer — null = dialog fermé */
  postId: string | null;
  onClose: () => void;
  /** Feedback délégué à l'écran hôte (qui possède le Toast) */
  onFeedback: (message: string, type: ToastType) => void;
}

/**
 * Dialog « Ajouter à une collection » (B30), extrait de social.tsx pour réutilisation
 * (fil + détail du post). Patron B22 : overlay Pressable qui ferme, carte interne
 * Pressable avec stopPropagation. Charge les collections de l'utilisateur courant
 * à l'ouverture (flag ignore au cleanup).
 */
export function CollectionPickerModal({ postId, onClose, onFeedback }: CollectionPickerModalProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const { user } = useAuthStore();
  const [collections, setCollections] = useState<CollectionResponse[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!postId || !user) return;
    let ignore = false;
    setIsLoading(true);
    collectionsApi.getCollectionsByUser(user.id, true)
      .then((cols) => { if (!ignore) setCollections(cols); })
      .catch((error: any) => {
        if (ignore) return;
        setCollections([]);
        onFeedback(error?.response?.data?.message || t('collections.loadError'), 'error');
      })
      .finally(() => { if (!ignore) setIsLoading(false); });
    return () => { ignore = true; };
  }, [postId, user?.id]);

  const handleSelect = async (collection: CollectionResponse) => {
    if (!postId) return;
    onClose();
    try {
      await collectionsApi.addPostToCollection(collection.id, postId);
      onFeedback(t('collections.addSuccess', { name: collection.name }), 'success');
    } catch (error: any) {
      onFeedback(error?.response?.data?.message || t('collections.addError'), 'error');
    }
  };

  return (
    <Modal visible={!!postId} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.overlay} onPress={onClose}>
        <Pressable onPress={(e) => e.stopPropagation()} style={[styles.card, { backgroundColor: colors.surface }]}>
          <Text style={[styles.title, { color: colors.onSurface }]}>{t('collections.pickerTitle')}</Text>
          {isLoading ? (
            <ActivityIndicator size="small" color={colors.primary} style={styles.spinner} />
          ) : collections.length === 0 ? (
            <Text style={[styles.empty, { color: colors.onSurfaceVariant }]}>{t('collections.pickerEmpty')}</Text>
          ) : (
            collections.map((col) => (
              <TouchableOpacity
                key={col.id}
                style={[styles.item, { borderBottomColor: colors.outlineVariant }]}
                onPress={() => handleSelect(col)}
              >
                <MaterialCommunityIcons name="folder-outline" size={20} color={colors.primary} />
                <Text style={[styles.itemName, { color: colors.onSurface }]}>{col.name}</Text>
              </TouchableOpacity>
            ))
          )}
          <TouchableOpacity onPress={onClose} style={styles.closeBtn}>
            <Text style={[styles.closeText, { color: colors.onSurfaceVariant }]}>{t('common.actions.cancel')}</Text>
          </TouchableOpacity>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', alignItems: 'center', padding: 32 }, // design-fixed
  card: { width: '100%', maxWidth: 360, borderRadius: 24, padding: 24 },
  title: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 20, fontWeight: '600', marginBottom: 12 },
  spinner: { marginVertical: 20 },
  empty: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', paddingVertical: 20 },
  item: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 14, borderBottomWidth: 1 },
  itemName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  closeBtn: { alignItems: 'center', paddingTop: 16 },
  closeText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
