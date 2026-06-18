import { useState, useCallback, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  I18nManager,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { useToast } from '../src/contexts/ToastContext';
import { salonsApi } from '../src/api/salons';
import { InvitableStylist, Invitability } from '../src/types';
import { Avatar, AccessDenied } from '../src/components/common';
import { usePermissions } from '../src/hooks/usePermissions';
import { resolveMediaUrl } from '../src/utils/media';

const DEBOUNCE_MS = 300;
const MIN_QUERY = 2;

export default function InviteStaffScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const { colors, typography: typo } = useTheme();
  const { showToast } = useToast();
  const { role, isLoading: permLoading } = usePermissions(salonId);

  const [query, setQuery] = useState('');
  const [results, setResults] = useState<InvitableStylist[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [invitingId, setInvitingId] = useState<string | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const doSearch = useCallback(async (q: string) => {
    if (!salonId || q.length < MIN_QUERY) {
      setResults([]);
      setHasSearched(false);
      return;
    }
    setIsSearching(true);
    try {
      const data = await salonsApi.searchInvitableStylists(salonId, q);
      setResults(data);
      setHasSearched(true);
    } catch (e: any) {
      console.error('searchStylists', e);
      setResults([]);
      setHasSearched(true);
    } finally {
      setIsSearching(false);
    }
  }, [salonId]);

  const handleQueryChange = useCallback((text: string) => {
    setQuery(text);
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (text.length < MIN_QUERY) {
      setResults([]);
      setHasSearched(false);
      return;
    }
    debounceRef.current = setTimeout(() => doSearch(text), DEBOUNCE_MS);
  }, [doSearch]);

  const handleInvite = useCallback(async (stylist: InvitableStylist) => {
    if (!salonId) return;
    setInvitingId(stylist.id);
    try {
      await salonsApi.createInvitation(salonId, { invitedUserId: stylist.id });
      const name = `${stylist.firstName || ''} ${stylist.lastName || ''}`.trim();
      showToast(t('profile.ownerStaff.inviteSent', { name }), 'success');
      // Navigate back safely — toast survives (global provider)
      if (router.canGoBack()) {
        router.back();
      } else {
        router.replace('/(tabs)');
      }
    } catch (e: any) {
      const msg = e?.response?.data?.message || e?.message || String(e);
      showToast(msg, 'error');
    } finally {
      setInvitingId(null);
    }
  }, [salonId, t, showToast]);

  const getStatusInfo = (inv: Invitability): { label: string; icon: string; color: string; bgColor: string } => {
    switch (inv) {
      case Invitability.ALREADY_MEMBER_ELSEWHERE:
        return { label: t('profile.ownerStaff.search.memberElsewhere'), icon: 'account-switch-outline', color: colors.onSurfaceVariant, bgColor: colors.surfaceContainerHigh };
      case Invitability.ALREADY_INVITED:
        return { label: t('profile.ownerStaff.search.alreadyInvited'), icon: 'clock-outline', color: colors.tertiary, bgColor: colors.tertiary + '18' };
      case Invitability.ALREADY_IN_THIS_SALON:
        return { label: t('profile.ownerStaff.search.alreadyHere'), icon: 'check-circle-outline', color: colors.primary, bgColor: colors.primary + '18' };
      default:
        return { label: t('profile.ownerStaff.search.onFrollot'), icon: 'check-decagram', color: colors.primary, bgColor: colors.primary + '18' };
    }
  };

  const showInitialState = !hasSearched && !isSearching && query.length < MIN_QUERY;

  if (permLoading) {
    return <View style={[st.container, { justifyContent: 'center', alignItems: 'center', backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }
  if (role === 'none') {
    return <AccessDenied />;
  }

  return (
    <View style={[st.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[st.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.canGoBack() ? router.back() : router.replace('/(tabs)')} hitSlop={8}>
          <MaterialCommunityIcons name={I18nManager.isRTL ? 'arrow-right' : 'arrow-left'} size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[st.headerTitle, { color: colors.onSurface }]}>{t('profile.ownerStaff.inviteTitle')}</Text>
      </View>

      <ScrollView contentContainerStyle={st.scrollContent} keyboardShouldPersistTaps="handled">
        {/* Search input — platform-standard style */}
        <TextInput
          style={[st.input, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: query.length > 0 ? colors.primary : colors.outlineVariant }]}
          placeholder={t('profile.ownerStaff.search.placeholder')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={query}
          onChangeText={handleQueryChange}
          autoCapitalize="none"
          autoFocus
        />

        {/* Min chars hint */}
        {query.length > 0 && query.length < MIN_QUERY && (
          <Text style={[st.hintText, { color: colors.onSurfaceVariant }]}>
            {t('profile.ownerStaff.search.minChars')}
          </Text>
        )}

        {/* Loading */}
        {isSearching && (
          <View style={st.loadingRow}>
            <ActivityIndicator size="small" color={colors.primary} />
            <Text style={[st.loadingText, { color: colors.onSurfaceVariant }]}>
              {t('common.states.loading')}...
            </Text>
          </View>
        )}

        {/* No results */}
        {!isSearching && hasSearched && results.length === 0 && (
          <View style={st.emptyState}>
            <MaterialCommunityIcons name="account-search-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[st.emptyTitle, { color: colors.onSurface }]}>
              {t('profile.ownerStaff.search.noResults')}
            </Text>
            <Text style={[st.emptySubtitle, { color: colors.onSurfaceVariant }]}>
              {t('profile.ownerStaff.search.minChars')}
            </Text>
          </View>
        )}

        {/* Initial state — before any search */}
        {showInitialState && (
          <View style={st.emptyState}>
            <MaterialCommunityIcons name="account-search" size={48} color={colors.outlineVariant} />
            <Text style={[st.emptySubtitle, { color: colors.onSurfaceVariant }]}>
              {t('profile.ownerStaff.search.minChars')}
            </Text>
          </View>
        )}

        {/* Results */}
        {!isSearching && results.length > 0 && (
          <View style={st.resultsSection}>
            <Text style={[st.resultsLabel, { color: colors.onSurfaceVariant }]}>
              {results.length} {results.length === 1 ? 'coiffeur' : 'coiffeurs'}
            </Text>
            {results.map((s) => {
              const isInvitable = s.invitability === Invitability.INVITABLE;
              const statusInfo = getStatusInfo(s.invitability);
              const fullName = `${s.firstName || ''} ${s.lastName || ''}`.trim() || s.email || '?';

              return (
                <View
                  key={s.id}
                  style={[
                    st.card,
                    {
                      backgroundColor: colors.surface,
                      borderColor: isInvitable ? colors.primary + '30' : colors.outlineVariant + '60',
                      opacity: isInvitable ? 1 : 0.7,
                    },
                  ]}
                >
                  <View style={st.cardTop}>
                    <Avatar
                      imageUrl={s.avatarUrl ? resolveMediaUrl(s.avatarUrl) : undefined}
                      initials={`${s.firstName?.[0] || ''}${s.lastName?.[0] || ''}`}
                      size={50}
                    />
                    <View style={st.cardBody}>
                      <Text style={[st.cardName, { color: colors.onSurface }]} numberOfLines={1}>{fullName}</Text>
                      {s.email && (
                        <Text style={[st.cardEmail, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{s.email}</Text>
                      )}
                    </View>
                  </View>

                  {/* Status badge */}
                  <View style={[st.statusRow, { backgroundColor: statusInfo.bgColor }]}>
                    <MaterialCommunityIcons name={statusInfo.icon as any} size={14} color={statusInfo.color} />
                    <Text style={[st.statusText, { color: statusInfo.color }]}>{statusInfo.label}</Text>
                  </View>

                  {/* Invite button — only for invitable */}
                  {isInvitable && (
                    invitingId === s.id ? (
                      <View style={st.inviteBtnWrap}>
                        <ActivityIndicator size="small" color={colors.primary} />
                      </View>
                    ) : (
                      <TouchableOpacity
                        style={[st.inviteBtn, { backgroundColor: colors.primary }]}
                        onPress={() => handleInvite(s)}
                        activeOpacity={0.8}
                      >
                        <MaterialCommunityIcons name="send" size={16} color={colors.onPrimary} />
                        <Text style={[st.inviteBtnText, { color: colors.onPrimary }]}>
                          {t('profile.ownerStaff.invite')}
                        </Text>
                      </TouchableOpacity>
                    )
                  )}
                </View>
              );
            })}
          </View>
        )}

        <View style={{ height: 40 }} />
      </ScrollView>
    </View>
  );
}

const st = StyleSheet.create({
  container: { flex: 1 },
  // Header — same pattern as create-service
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16, gap: 12 },
  headerTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', flex: 1 },
  scrollContent: { paddingHorizontal: 16 },
  // Input — identical to create-service inputs
  input: { height: 52, borderRadius: 12, borderWidth: 1, paddingHorizontal: 16, fontSize: 16, fontFamily: 'Manrope-Regular', marginTop: 12, marginBottom: 12 },
  // Hints & states
  hintText: { fontFamily: 'Manrope-Regular', fontSize: 12, textAlign: 'center', marginBottom: 8 },
  loadingRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 10, paddingVertical: 32 },
  loadingText: { fontFamily: 'Manrope-Regular', fontSize: 13 },
  emptyState: { alignItems: 'center', paddingTop: 40, gap: 10 },
  emptyTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  emptySubtitle: { fontFamily: 'Manrope-Regular', fontSize: 13, textAlign: 'center' },
  // Results
  resultsSection: { marginTop: 4 },
  resultsLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600', marginBottom: 10, textTransform: 'uppercase', letterSpacing: 0.5 },
  // Card — premium feel
  card: { borderRadius: 16, borderWidth: 1, padding: 16, marginBottom: 12, gap: 12 },
  cardTop: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  cardBody: { flex: 1 },
  cardName: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  cardEmail: { fontFamily: 'Manrope-Regular', fontSize: 13, marginTop: 2 },
  // Status badge
  statusRow: { flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-start', paddingVertical: 4, paddingHorizontal: 10, borderRadius: 999 },
  statusText: { fontFamily: 'Manrope-SemiBold', fontSize: 11, fontWeight: '600' },
  // Invite button
  inviteBtnWrap: { height: 44, justifyContent: 'center', alignItems: 'center' },
  inviteBtn: { height: 44, borderRadius: 22, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
  inviteBtnText: { fontFamily: 'Manrope-Bold', fontSize: 14, fontWeight: '700' },
});
