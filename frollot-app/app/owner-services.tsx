import { useState, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  RefreshControl,
  I18nManager,
  ActivityIndicator,
  Modal,
  Pressable,
} from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { SalonService } from '../src/types';
import { ServiceImageStack, AccessDenied } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';

// ---------------------------------------------------------------------------
// Main screen
// ---------------------------------------------------------------------------
export default function OwnerServicesScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);

  const [services, setServices] = useState<SalonService[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // Action menu + delete confirm (Modal-based, works on web)
  const [menuService, setMenuService] = useState<SalonService | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<SalonService | null>(null);

  const loadServices = useCallback(async () => {
    if (!salonId) return;
    setError(null);
    try {
      const list = await salonsApi.getSalonServices(salonId);
      setServices(list);
    } catch (e: any) {
      console.error('loadServices', e);
      setError(e?.message || String(e));
    } finally {
      setIsLoading(false);
    }
  }, [salonId]);

  // Reload every time the screen gains focus (covers return from create-service)
  useFocusEffect(
    useCallback(() => {
      loadServices();
    }, [loadServices]),
  );

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadServices();
    setRefreshing(false);
  }, [loadServices]);

  const handleDeleteConfirm = async () => {
    if (!deleteTarget || !salonId) return;
    setDeletingId(deleteTarget.id);
    setDeleteTarget(null);
    try {
      await salonsApi.deleteSalonService(salonId, deleteTarget.id);
      setServices((prev) => prev.filter((s) => s.id !== deleteTarget.id));
    } catch {
      // silently handled — the item stays in the list
    } finally {
      setDeletingId(null);
    }
  };

  const canEdit = can('service.update');
  const canDelete = can('service.delete');
  const hasMenuActions = canEdit || canDelete;

  const handleMenuEdit = () => {
    if (menuService) {
      router.push({ pathname: '/create-service' as any, params: { salonId, serviceId: menuService.id } });
    }
    setMenuService(null);
  };
  const handleMenuDelete = () => {
    if (menuService) setDeleteTarget(menuService);
    setMenuService(null);
  };

  if (permLoading) {
    return <View style={[st.container, { backgroundColor: colors.background, justifyContent: 'center', alignItems: 'center' }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (role === 'none') {
    return <AccessDenied />;
  }

  return (
    <View style={[st.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[st.topBar, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[st.topTitle, { color: colors.onSurface }]}>{t('profile.ownerServices.title')}</Text>
        {!isLoading && (
          <Text style={[st.countBadge, { color: colors.onSurfaceVariant }]}>
            {t('profile.ownerServices.count', { count: services.length })}
          </Text>
        )}
      </View>

      {/* Content */}
      {isLoading ? (
        <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 40 }} />
      ) : error ? (
        <Text style={[st.errorText, { color: colors.error }]}>{error}</Text>
      ) : (
        <ScrollView
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
          contentContainerStyle={st.scrollContent}
        >
          {services.length === 0 ? (
            <View style={st.emptyContainer}>
              <MaterialCommunityIcons name="content-cut" size={48} color={colors.onSurfaceVariant} />
              <Text style={[st.emptyText, { color: colors.onSurfaceVariant }]}>
                {t('profile.ownerServices.empty')}
              </Text>
              {can('service.create') && (
                <TouchableOpacity
                  style={[st.emptyCta, { backgroundColor: colors.primary }]}
                  onPress={() => router.push({ pathname: '/create-service' as any, params: { salonId } })}
                >
                  <Text style={[st.emptyCtaText, { color: colors.onPrimary }]}>
                    {t('profile.ownerServices.addFirst')}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          ) : (
            services.map((svc) => (
              <View key={svc.id} style={[st.row, { backgroundColor: colors.surface }]}>
                <ServiceImageStack imageUrls={svc.imageUrls || []} category={svc.category} size={52} colors={colors} />
                <View style={st.rowBody}>
                  <Text style={[st.rowName, { color: colors.onSurface }]} numberOfLines={1}>{svc.name}</Text>
                  <Text style={[st.rowMeta, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                    {svc.categoryLabel || svc.category}{' · '}{svc.formattedDuration || `${svc.durationMinutes} min`}
                  </Text>
                  <Text style={[st.rowPrice, { color: colors.onSurface }]}>
                    {svc.price} FCFA
                  </Text>
                </View>
                {hasMenuActions && (
                  deletingId === svc.id ? (
                    <ActivityIndicator size="small" color={colors.error} />
                  ) : (
                    <TouchableOpacity onPress={() => setMenuService(svc)} hitSlop={8} style={st.menuBtn}>
                      <MaterialCommunityIcons name="dots-vertical" size={22} color={colors.onSurfaceVariant} />
                    </TouchableOpacity>
                  )
                )}
              </View>
            ))
          )}
          <View style={{ height: 80 }} />
        </ScrollView>
      )}

      {/* FAB — Add */}
      {can('service.create') && (
        <TouchableOpacity
          style={[st.fab, { backgroundColor: colors.primary }]}
          onPress={() => router.push({ pathname: '/create-service' as any, params: { salonId } })}
          activeOpacity={0.8}
        >
          <MaterialCommunityIcons name="plus" size={26} color={colors.onPrimary} />
        </TouchableOpacity>
      )}

      {/* Action menu modal */}
      <Modal visible={!!menuService} transparent animationType="fade" onRequestClose={() => setMenuService(null)}>
        <Pressable style={st.overlay} onPress={() => setMenuService(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[st.menuCard, { backgroundColor: colors.surface }]}>
            <Text style={[st.menuTitle, { color: colors.onSurface }]} numberOfLines={1}>
              {menuService?.name || ''}
            </Text>
            {canEdit && (
              <TouchableOpacity style={[st.menuAction, { borderColor: colors.outlineVariant }]} onPress={handleMenuEdit}>
                <MaterialCommunityIcons name="pencil-outline" size={20} color={colors.primary} />
                <Text style={[st.menuActionText, { color: colors.onSurface }]}>{t('profile.ownerServices.edit')}</Text>
              </TouchableOpacity>
            )}
            {canDelete && (
              <TouchableOpacity style={[st.menuAction, { borderColor: colors.outlineVariant }]} onPress={handleMenuDelete}>
                <MaterialCommunityIcons name="trash-can-outline" size={20} color={colors.error} />
                <Text style={[st.menuActionText, { color: colors.error }]}>{t('profile.ownerServices.delete')}</Text>
              </TouchableOpacity>
            )}
            <TouchableOpacity style={[st.menuCancelBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={() => setMenuService(null)}>
              <Text style={[st.menuCancelText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Delete confirmation modal */}
      <Modal visible={!!deleteTarget} transparent animationType="fade" onRequestClose={() => setDeleteTarget(null)}>
        <Pressable style={st.overlay} onPress={() => setDeleteTarget(null)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[st.menuCard, { backgroundColor: colors.surface }]}>
            <MaterialCommunityIcons name="alert-circle-outline" size={36} color={colors.error} style={{ alignSelf: 'center' }} />
            <Text style={[st.menuTitle, { color: colors.onSurface, textAlign: 'center' }]}>
              {t('profile.ownerServices.deleteTitle')}
            </Text>
            <Text style={[st.confirmMsg, { color: colors.onSurfaceVariant }]}>
              {t('profile.ownerServices.deleteMessage', { name: deleteTarget?.name || '' })}
            </Text>
            <View style={st.confirmActions}>
              <TouchableOpacity style={[st.confirmBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={() => setDeleteTarget(null)}>
                <Text style={[st.menuCancelText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[st.confirmBtn, { backgroundColor: colors.error }]} onPress={handleDeleteConfirm}>
                <Text style={[st.menuCancelText, { color: colors.onError }]}>{t('profile.ownerServices.deleteConfirm')}</Text>
              </TouchableOpacity>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const st = StyleSheet.create({
  container: { flex: 1 },
  topBar: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16, gap: 12 },
  topTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  countBadge: { fontFamily: 'Manrope-Regular', fontSize: 13 },
  scrollContent: { paddingHorizontal: 16, paddingTop: 8 },
  // Empty state
  emptyContainer: { alignItems: 'center', paddingTop: 60, gap: 12 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
  emptyCta: { paddingVertical: 12, paddingHorizontal: 24, borderRadius: 999, marginTop: 8 },
  emptyCtaText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  errorText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', marginTop: 40, paddingHorizontal: 16 },
  // Service row
  row: { flexDirection: 'row', alignItems: 'center', borderRadius: 14, padding: 12, marginBottom: 10, gap: 12 },
  rowBody: { flex: 1 },
  rowName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  rowMeta: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  rowPrice: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700', marginTop: 4 },
  menuBtn: { padding: 4 },
  // FAB
  fab: { position: 'absolute', bottom: 24, end: 20, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 4, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.25, shadowRadius: 4 },
  // Modals
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center', padding: 24 },
  menuCard: { width: '100%', maxWidth: 340, borderRadius: 20, padding: 20, gap: 10 },
  menuTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 20, fontWeight: '600', textAlign: 'center', marginBottom: 4 },
  menuAction: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 14, paddingHorizontal: 16, borderRadius: 12, borderWidth: 1 },
  menuActionText: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  menuCancelBtn: { paddingVertical: 12, borderRadius: 999, alignItems: 'center', marginTop: 4 },
  menuCancelText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  confirmMsg: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', lineHeight: 22 },
  confirmActions: { flexDirection: 'row', gap: 10, marginTop: 8 },
  confirmBtn: { flex: 1, paddingVertical: 12, borderRadius: 999, alignItems: 'center' },
});
