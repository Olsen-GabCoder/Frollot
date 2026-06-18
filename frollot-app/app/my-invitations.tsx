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
} from 'react-native';
import { router, useFocusEffect } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { useTheme } from '../src/theme';
import { salonsApi } from '../src/api/salons';
import { InvitationResponse } from '../src/types';
import { resolveMediaUrl } from '../src/utils/media';

function daysUntil(dateStr: string): number {
  const diff = new Date(dateStr).getTime() - Date.now();
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
}

export default function MyInvitationsScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();

  const [invitations, setInvitations] = useState<InvitationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [processingId, setProcessingId] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const data = await salonsApi.getMyInvitations();
      setInvitations(data);
    } catch (e: any) {
      console.error('loadMyInvitations', e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  }, [load]);

  const handleAccept = useCallback(async (inv: InvitationResponse) => {
    setProcessingId(inv.id);
    try {
      await salonsApi.acceptInvitation(inv.id);
      setInvitations((prev) => prev.filter((i) => i.id !== inv.id));
      Alert.alert(t('common.actions.done'), t('myInvitations.acceptedMessage', { salon: inv.salonName }));
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || String(e));
    } finally {
      setProcessingId(null);
    }
  }, [t]);

  const handleDecline = useCallback((inv: InvitationResponse) => {
    Alert.alert(
      t('myInvitations.declineTitle'),
      t('myInvitations.declineMessage', { salon: inv.salonName }),
      [
        { text: t('common.actions.cancel'), style: 'cancel' },
        {
          text: t('myInvitations.declineConfirm'),
          style: 'destructive',
          onPress: async () => {
            setProcessingId(inv.id);
            try {
              await salonsApi.declineInvitation(inv.id);
              setInvitations((prev) => prev.filter((i) => i.id !== inv.id));
            } catch (e: any) {
              Alert.alert(t('common.states.error'), e?.message || String(e));
            } finally {
              setProcessingId(null);
            }
          },
        },
      ],
    );
  }, [t]);

  return (
    <View style={[st.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[st.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[st.headerTitle, { color: colors.onSurface }]}>{t('myInvitations.title')}</Text>
      </View>

      {isLoading ? (
        <ActivityIndicator size="large" color={colors.primary} style={{ marginTop: 40 }} />
      ) : (
        <ScrollView
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
          contentContainerStyle={st.scroll}
        >
          {invitations.length === 0 ? (
            <View style={st.emptyState}>
              <MaterialCommunityIcons name="email-open-outline" size={56} color={colors.outlineVariant} />
              <Text style={[st.emptyTitle, { color: colors.onSurface }]}>{t('myInvitations.empty')}</Text>
              <Text style={[st.emptySubtitle, { color: colors.onSurfaceVariant }]}>{t('myInvitations.emptyHint')}</Text>
            </View>
          ) : (
            <>
              {invitations.map((inv) => {
                const days = daysUntil(inv.expiresAt);
                const isProcessing = processingId === inv.id;
                const coverUrl = inv.salonCoverUrl ? resolveMediaUrl(inv.salonCoverUrl) : null;

                return (
                  <View key={inv.id} style={[st.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant + '40' }]}>
                    {/* Cover / header */}
                    <View style={[st.cardCover, { backgroundColor: colors.primaryContainer }]}>
                      {coverUrl ? (
                        <Image source={{ uri: coverUrl }} style={st.cardCoverImg} contentFit="cover" />
                      ) : (
                        <MaterialCommunityIcons name="store" size={36} color={colors.primary} style={{ opacity: 0.4 }} />
                      )}
                    </View>

                    {/* Body */}
                    <View style={st.cardBody}>
                      <Text style={[st.salonName, { color: colors.onSurface }]}>{inv.salonName}</Text>
                      <Text style={[st.roleText, { color: colors.primary }]}>
                        {t('myInvitations.invitedAs', { role: t(`salon.roles.${inv.role}`) })}
                      </Text>

                      {/* Expiration badge */}
                      <View style={[st.expBadge, { backgroundColor: days <= 2 ? colors.error + '18' : colors.tertiary + '18' }]}>
                        <MaterialCommunityIcons name="clock-outline" size={13} color={days <= 2 ? colors.error : colors.tertiary} />
                        <Text style={[st.expText, { color: days <= 2 ? colors.error : colors.tertiary }]}>
                          {t('myInvitations.expiresIn', { days })}
                        </Text>
                      </View>

                      {/* Actions */}
                      {isProcessing ? (
                        <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 16 }} />
                      ) : (
                        <View style={st.actions}>
                          <TouchableOpacity
                            style={[st.declineBtn, { borderColor: colors.outlineVariant }]}
                            onPress={() => handleDecline(inv)}
                          >
                            <Text style={[st.declineBtnText, { color: colors.onSurfaceVariant }]}>{t('myInvitations.decline')}</Text>
                          </TouchableOpacity>
                          <TouchableOpacity
                            style={[st.acceptBtn, { backgroundColor: colors.primary }]}
                            onPress={() => handleAccept(inv)}
                          >
                            <MaterialCommunityIcons name="check" size={18} color={colors.onPrimary} />
                            <Text style={[st.acceptBtnText, { color: colors.onPrimary }]}>{t('myInvitations.accept')}</Text>
                          </TouchableOpacity>
                        </View>
                      )}
                    </View>
                  </View>
                );
              })}

              {/* Exclusivity note */}
              <View style={[st.noteCard, { backgroundColor: colors.surfaceContainerHigh }]}>
                <MaterialCommunityIcons name="information-outline" size={16} color={colors.onSurfaceVariant} />
                <Text style={[st.noteText, { color: colors.onSurfaceVariant }]}>{t('myInvitations.exclusivityNote')}</Text>
              </View>
            </>
          )}
          <View style={{ height: 40 }} />
        </ScrollView>
      )}
    </View>
  );
}

const st = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16, gap: 12 },
  headerTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  scroll: { paddingHorizontal: 16, paddingTop: 12 },
  // Empty state
  emptyState: { alignItems: 'center', paddingTop: 60, gap: 10 },
  emptyTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 17, fontWeight: '600' },
  emptySubtitle: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center', maxWidth: 260 },
  // Card
  card: { borderRadius: 20, borderWidth: 1, overflow: 'hidden', marginBottom: 16 },
  cardCover: { height: 100, alignItems: 'center', justifyContent: 'center' },
  cardCoverImg: { width: '100%', height: '100%' },
  cardBody: { padding: 18, gap: 6 },
  salonName: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600' },
  roleText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  expBadge: { flexDirection: 'row', alignItems: 'center', gap: 5, alignSelf: 'flex-start', paddingVertical: 4, paddingHorizontal: 10, borderRadius: 999, marginTop: 4 },
  expText: { fontFamily: 'Manrope-SemiBold', fontSize: 11, fontWeight: '600' },
  // Actions
  actions: { flexDirection: 'row', gap: 10, marginTop: 16 },
  declineBtn: { flex: 1, height: 44, borderRadius: 22, borderWidth: 1.5, alignItems: 'center', justifyContent: 'center' },
  declineBtnText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  acceptBtn: { flex: 2, height: 44, borderRadius: 22, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6 },
  acceptBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
  // Note
  noteCard: { flexDirection: 'row', gap: 8, padding: 14, borderRadius: 12, marginTop: 4 },
  noteText: { fontFamily: 'Manrope-Regular', fontSize: 12, flex: 1 },
});
