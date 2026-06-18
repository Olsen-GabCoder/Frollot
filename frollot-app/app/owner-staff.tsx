import { useState, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  RefreshControl,
  I18nManager,
  Alert,
  ActivityIndicator,
  Modal,
  Pressable,
  Switch,
} from 'react-native';
import { router, useLocalSearchParams, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { useAuthStore } from '../src/stores/authStore';
import { useToast } from '../src/contexts/ToastContext';
import { salonsApi } from '../src/api/salons';
import { StaffMember, InvitationResponse, ServiceCategory, SERVICE_CATEGORY_META } from '../src/types';
import { Avatar, AccessDenied } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';
import { resolveMediaUrl } from '../src/utils/media';

const ASSIGNABLE_ROLES = ['manager', 'hairstylist', 'apprentice'] as const;

export default function OwnerStaffScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();
  const { showToast } = useToast();
  const { role, isLoading: permLoading, can } = usePermissions(salonId);
  const canListInvitations = can('invitation.list');
  const canCreateInvitation = can('invitation.create');
  const canCancelInvitation = can('invitation.cancel');
  const canRemoveStaff = can('staff.remove');

  const [staff, setStaff] = useState<StaffMember[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [invitations, setInvitations] = useState<InvitationResponse[]>([]);
  const [cancellingId, setCancellingId] = useState<string | null>(null);

  // Action menu state (replaces Alert.alert which doesn't work on web)
  const [menuMember, setMenuMember] = useState<StaffMember | null>(null);

  // Edit dialog state
  const [editMember, setEditMember] = useState<StaffMember | null>(null);
  const [editRole, setEditRole] = useState('');
  const [editSpecs, setEditSpecs] = useState<ServiceCategory[]>([]);
  const [editActive, setEditActive] = useState(true);
  const [isSavingEdit, setIsSavingEdit] = useState(false);

  const openEditDialog = (member: StaffMember) => {
    setEditMember(member);
    setEditRole(member.role);
    setEditSpecs([...member.specialties]);
    setEditActive(member.isActive);
  };
  const closeEditDialog = () => setEditMember(null);

  const handleSaveEdit = async () => {
    if (!editMember || !salonId) return;
    setIsSavingEdit(true);
    try {
      await salonsApi.updateStaffMember(salonId, editMember.id, {
        role: editRole,
        specialties: editSpecs,
        isActive: editActive,
      });
      showToast(t('profile.ownerStaff.editDialog.saveSuccess'), 'success');
      closeEditDialog();
      await loadStaff();
    } catch (e: any) {
      showToast(e?.response?.data?.message || e?.message || t('profile.ownerStaff.editDialog.saveError'), 'error');
    } finally {
      setIsSavingEdit(false);
    }
  };

  const toggleSpec = (cat: ServiceCategory) => {
    setEditSpecs((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
    );
  };

  const loadStaff = useCallback(async () => {
    if (!salonId) return;
    setError(null);
    try {
      const [staffList, invList] = await Promise.all([
        salonsApi.getSalonStaff(salonId),
        canListInvitations
          ? salonsApi.getSalonInvitations(salonId)
          : Promise.resolve([] as InvitationResponse[]),
      ]);
      setStaff(staffList);
      setInvitations(invList.filter((inv) => inv.status === 'PENDING'));
    } catch (e: any) {
      console.error('loadStaff', e);
      setError(e?.message || String(e));
    } finally {
      setIsLoading(false);
    }
  }, [salonId, canListInvitations]);

  useFocusEffect(
    useCallback(() => {
      loadStaff();
    }, [loadStaff]),
  );

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadStaff();
    setRefreshing(false);
  }, [loadStaff]);

  const handleRemove = useCallback((member: StaffMember) => {
    Alert.alert(
      t('profile.ownerStaff.removeTitle'),
      t('profile.ownerStaff.removeMessage', { name: `${member.userFirstName} ${member.userLastName}` }),
      [
        { text: t('common.actions.cancel'), style: 'cancel' },
        {
          text: t('profile.ownerStaff.removeConfirm'),
          style: 'destructive',
          onPress: async () => {
            if (!salonId) return;
            setRemovingId(member.id);
            try {
              await salonsApi.removeStaffMember(salonId, member.id);
              setStaff((prev) => prev.filter((s) => s.id !== member.id));
              showToast(t('profile.ownerStaff.memberRemoved', { name: `${member.userFirstName} ${member.userLastName}` }), 'success');
            } catch (e: any) {
              showToast(e?.message || String(e), 'error');
            } finally {
              setRemovingId(null);
            }
          },
        },
      ],
    );
  }, [salonId, t]);

  const handleCancelInvitation = useCallback((inv: InvitationResponse) => {
    Alert.alert(
      t('profile.ownerStaff.cancelInvTitle'),
      t('profile.ownerStaff.cancelInvMessage', { name: inv.invitedUserName || inv.invitedEmail || '' }),
      [
        { text: t('common.actions.cancel'), style: 'cancel' },
        {
          text: t('profile.ownerStaff.cancelInvConfirm'),
          style: 'destructive',
          onPress: async () => {
            if (!salonId) return;
            setCancellingId(inv.id);
            try {
              await salonsApi.cancelInvitation(salonId, inv.id);
              setInvitations((prev) => prev.filter((i) => i.id !== inv.id));
              showToast(t('profile.ownerStaff.invitationCancelled'), 'success');
            } catch (e: any) {
              showToast(e?.message || String(e), 'error');
            } finally {
              setCancellingId(null);
            }
          },
        },
      ],
    );
  }, [salonId, t]);

  const canEditStaff = can('staff.update');
  const hasStaffMenuActions = canEditStaff || canRemoveStaff;


  const openMenu = (member: StaffMember) => setMenuMember(member);
  const closeMenu = () => setMenuMember(null);

  const handleMenuEdit = () => {
    if (menuMember) openEditDialog(menuMember);
    closeMenu();
  };
  const handleMenuRemove = () => {
    if (menuMember) handleRemove(menuMember);
    closeMenu();
  };

  const menuIsMemberOwner = menuMember?.role === 'owner';
  const menuIsSelf = menuMember?.userId === user?.id;
  const menuShowEdit = canEditStaff && !menuIsMemberOwner;
  const menuShowRemove = canRemoveStaff && !menuIsMemberOwner && !menuIsSelf;

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
        <Text style={[st.topTitle, { color: colors.onSurface }]}>{t('profile.ownerStaff.title')}</Text>
        {!isLoading && (
          <Text style={[st.countBadge, { color: colors.onSurfaceVariant }]}>
            {t('profile.ownerStaff.count', { count: staff.length })}
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
          {/* Pending invitations section (visible if invitation.list permission) */}
          {canListInvitations && invitations.length > 0 && (
            <View style={{ marginBottom: 16 }}>
              <Text style={[st.sectionLabel, { color: colors.onSurfaceVariant }]}>
                {t('profile.ownerStaff.pendingSection', { count: invitations.length })}
              </Text>
              {invitations.map((inv) => (
                <View key={inv.id} style={[st.row, { backgroundColor: colors.surfaceContainerHigh }]}>
                  <Avatar
                    imageUrl={inv.invitedUserAvatar ? resolveMediaUrl(inv.invitedUserAvatar) : undefined}
                    initials={inv.invitedUserName ? inv.invitedUserName.split(' ').map((w) => w[0]).join('').slice(0, 2) : '?'}
                    size={44}
                  />
                  <View style={st.rowBody}>
                    <Text style={[st.rowName, { color: colors.onSurface }]} numberOfLines={1}>
                      {inv.invitedUserName || inv.invitedEmail || '?'}
                    </Text>
                    <Text style={[st.rowRole, { color: colors.tertiary }]}>
                      {t('profile.ownerStaff.pendingLabel')} · {t('profile.ownerStaff.expiresOn', { date: new Date(inv.expiresAt).toLocaleDateString() })}
                    </Text>
                  </View>
                  {canCancelInvitation && (
                    cancellingId === inv.id ? (
                      <ActivityIndicator size="small" color={colors.error} />
                    ) : (
                      <TouchableOpacity onPress={() => handleCancelInvitation(inv)} hitSlop={8} style={st.menuBtn}>
                        <MaterialCommunityIcons name="close-circle-outline" size={22} color={colors.error} />
                      </TouchableOpacity>
                    )
                  )}
                </View>
              ))}
            </View>
          )}

          {staff.length === 0 && invitations.length === 0 ? (
            <View style={st.emptyContainer}>
              <MaterialCommunityIcons name="account-multiple-outline" size={48} color={colors.onSurfaceVariant} />
              <Text style={[st.emptyText, { color: colors.onSurfaceVariant }]}>
                {t('profile.ownerStaff.empty')}
              </Text>
              {canCreateInvitation && (
                <TouchableOpacity
                  style={[st.emptyCta, { backgroundColor: colors.primary }]}
                  onPress={() => router.push({ pathname: '/create-staff' as any, params: { salonId } })}
                >
                  <Text style={[st.emptyCtaText, { color: colors.onPrimary }]}>
                    {t('profile.ownerStaff.inviteFirst')}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          ) : (
            staff.map((member) => (
              <View key={member.id} style={[st.row, { backgroundColor: colors.surface }]}>
                <Avatar
                  imageUrl={member.userAvatarUrl ? resolveMediaUrl(member.userAvatarUrl) : undefined}
                  initials={`${member.userFirstName?.[0] || ''}${member.userLastName?.[0] || ''}`}
                  size={48}
                />
                <View style={st.rowBody}>
                  <Text style={[st.rowName, { color: colors.onSurface }]} numberOfLines={1}>
                    {member.userFirstName} {member.userLastName}
                  </Text>
                  <Text style={[st.rowRole, { color: colors.primary }]}>
                    {t(`salon.roles.${member.role}`)}
                  </Text>
                  {member.specialtyLabels.length > 0 && (
                    <Text style={[st.rowSpecialties, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                      {member.specialtyLabels.join(', ')}
                    </Text>
                  )}
                </View>
                {/* Active/Inactive badge */}
                <View style={[st.badge, { backgroundColor: member.isActive ? colors.tertiary + '22' : colors.error + '22' }]}>
                  <Text style={[st.badgeText, { color: member.isActive ? colors.tertiary : colors.error }]}>
                    {member.isActive ? t('profile.ownerStaff.active') : t('profile.ownerStaff.inactive')}
                  </Text>
                </View>
                {hasStaffMenuActions && member.role !== 'owner' && (
                  removingId === member.id ? (
                    <ActivityIndicator size="small" color={colors.error} />
                  ) : (
                    <TouchableOpacity onPress={() => openMenu(member)} hitSlop={8} style={st.menuBtn}>
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

      {/* FAB — Invite */}
      {canCreateInvitation && (
        <TouchableOpacity
          style={[st.fab, { backgroundColor: colors.primary }]}
          onPress={() => router.push({ pathname: '/create-staff' as any, params: { salonId } })}
          activeOpacity={0.8}
        >
          <MaterialCommunityIcons name="account-plus-outline" size={26} color={colors.onPrimary} />
        </TouchableOpacity>
      )}

      {/* Action menu (replaces Alert.alert — works on web) */}
      <Modal visible={!!menuMember} transparent animationType="fade" onRequestClose={closeMenu}>
        <Pressable style={st.dialogOverlay} onPress={closeMenu}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[st.menuCard, { backgroundColor: colors.surface }]}>
            <Text style={[st.dialogTitle, { color: colors.onSurface }]}>
              {menuMember ? `${menuMember.userFirstName} ${menuMember.userLastName}` : ''}
            </Text>
            {menuIsMemberOwner && (
              <Text style={[st.dialogSubtitle, { color: colors.onSurfaceVariant }]}>{t('profile.ownerStaff.ownerProtected')}</Text>
            )}
            {menuShowEdit && (
              <TouchableOpacity style={[st.menuAction, { borderColor: colors.outlineVariant }]} onPress={handleMenuEdit}>
                <MaterialCommunityIcons name="pencil-outline" size={20} color={colors.primary} />
                <Text style={[st.menuActionText, { color: colors.onSurface }]}>{t('profile.ownerStaff.edit')}</Text>
              </TouchableOpacity>
            )}
            {menuShowRemove && (
              <TouchableOpacity style={[st.menuAction, { borderColor: colors.outlineVariant }]} onPress={handleMenuRemove}>
                <MaterialCommunityIcons name="account-remove-outline" size={20} color={colors.error} />
                <Text style={[st.menuActionText, { color: colors.error }]}>{t('profile.ownerStaff.remove')}</Text>
              </TouchableOpacity>
            )}
            <TouchableOpacity style={[st.menuCancelBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={closeMenu}>
              <Text style={[st.dialogBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>

      {/* Edit member dialog */}
      <Modal visible={!!editMember} transparent animationType="fade" onRequestClose={closeEditDialog}>
        <Pressable style={st.dialogOverlay} onPress={closeEditDialog}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[st.dialogCard, { backgroundColor: colors.surface }]}>
            <Text style={[st.dialogTitle, { color: colors.onSurface }]}>
              {editMember ? `${editMember.userFirstName} ${editMember.userLastName}` : ''}
            </Text>
            <Text style={[st.dialogSubtitle, { color: colors.onSurfaceVariant }]}>
              {t('profile.ownerStaff.editDialog.title')}
            </Text>

            {/* Role picker */}
            <Text style={[st.dialogLabel, { color: colors.onSurface }]}>
              {t('profile.ownerStaff.editDialog.roleLabel')}
            </Text>
            <View style={st.rolesRow}>
              {ASSIGNABLE_ROLES.map((r) => {
                const active = editRole === r;
                return (
                  <TouchableOpacity
                    key={r}
                    style={[st.roleChip, active ? { backgroundColor: colors.primary } : { backgroundColor: colors.surfaceContainerHigh }]}
                    onPress={() => setEditRole(r)}
                  >
                    <Text style={[st.roleChipText, { color: active ? colors.onPrimary : colors.onSurfaceVariant }]}>
                      {t(`salon.roles.${r}`)}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Specialties multi-select */}
            <Text style={[st.dialogLabel, { color: colors.onSurface, marginTop: 16 }]}>
              {t('profile.ownerStaff.editDialog.specialtiesLabel')}
            </Text>
            <View style={st.specsGrid}>
              {Object.values(ServiceCategory).map((cat) => {
                const meta = SERVICE_CATEGORY_META[cat];
                const selected = editSpecs.includes(cat);
                return (
                  <TouchableOpacity
                    key={cat}
                    style={[st.specChip, selected ? { backgroundColor: colors.primaryContainer } : { backgroundColor: colors.surfaceContainerHigh }]}
                    onPress={() => toggleSpec(cat)}
                  >
                    <MaterialCommunityIcons name={meta.icon} size={16} color={selected ? colors.onPrimaryContainer : colors.onSurfaceVariant} />
                    <Text style={[st.specChipText, { color: selected ? colors.onPrimaryContainer : colors.onSurfaceVariant }]}>
                      {t(meta.labelKey)}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Active toggle */}
            <View style={st.toggleRow}>
              <Text style={[st.dialogLabel, { color: colors.onSurface, flex: 1 }]}>
                {t('profile.ownerStaff.editDialog.activeLabel')}
              </Text>
              <Switch
                value={editActive}
                onValueChange={setEditActive}
                trackColor={{ false: colors.outlineVariant, true: colors.primary + '80' }}
                thumbColor={editActive ? colors.primary : colors.onSurfaceVariant}
              />
            </View>

            {/* Actions */}
            <View style={st.dialogActions}>
              <TouchableOpacity style={[st.dialogBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={closeEditDialog}>
                <Text style={[st.dialogBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[st.dialogBtn, { backgroundColor: colors.primary }]}
                onPress={handleSaveEdit}
                disabled={isSavingEdit}
              >
                {isSavingEdit ? (
                  <ActivityIndicator size="small" color={colors.onPrimary} />
                ) : (
                  <Text style={[st.dialogBtnText, { color: colors.onPrimary }]}>{t('profile.ownerStaff.editDialog.save')}</Text>
                )}
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
  emptyContainer: { alignItems: 'center', paddingTop: 60, gap: 12 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center' },
  emptyCta: { paddingVertical: 12, paddingHorizontal: 24, borderRadius: 999, marginTop: 8 },
  emptyCtaText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  errorText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', marginTop: 40, paddingHorizontal: 16 },
  row: { flexDirection: 'row', alignItems: 'center', borderRadius: 14, padding: 12, marginBottom: 10, gap: 12 },
  rowBody: { flex: 1 },
  rowName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  rowRole: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', marginTop: 2 },
  rowSpecialties: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  badge: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 999 },
  badgeText: { fontFamily: 'Manrope-SemiBold', fontSize: 10, fontWeight: '600' },
  menuBtn: { padding: 4 },
  sectionLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', marginBottom: 8 },
  fab: { position: 'absolute', bottom: 24, end: 20, width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', elevation: 4, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.25, shadowRadius: 4 },
  // Action menu
  menuCard: { width: '100%', maxWidth: 340, borderRadius: 20, padding: 20, gap: 10 },
  menuAction: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 14, paddingHorizontal: 16, borderRadius: 12, borderWidth: 1 },
  menuActionText: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  menuCancelBtn: { paddingVertical: 12, borderRadius: 999, alignItems: 'center', marginTop: 4 },
  // Edit dialog
  dialogOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center', padding: 24 },
  dialogCard: { width: '100%', maxWidth: 400, borderRadius: 20, padding: 24, gap: 4 },
  dialogTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', textAlign: 'center' },
  dialogSubtitle: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', marginBottom: 16 },
  dialogLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', marginBottom: 8 },
  rolesRow: { flexDirection: 'row', gap: 8 },
  roleChip: { flex: 1, paddingVertical: 10, borderRadius: 999, alignItems: 'center' },
  roleChipText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  specsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  specChip: { flexDirection: 'row', alignItems: 'center', gap: 4, paddingVertical: 6, paddingHorizontal: 12, borderRadius: 999 },
  specChipText: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  toggleRow: { flexDirection: 'row', alignItems: 'center', marginTop: 16, marginBottom: 8 },
  dialogActions: { flexDirection: 'row', gap: 10, marginTop: 16 },
  dialogBtn: { flex: 1, paddingVertical: 12, borderRadius: 999, alignItems: 'center', justifyContent: 'center' },
  dialogBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
