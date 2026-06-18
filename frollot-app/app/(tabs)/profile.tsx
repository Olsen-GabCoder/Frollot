import { useEffect, useState, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Share,
  I18nManager,
  RefreshControl,
  TextInput,
  Alert,
} from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons, MaterialCommunityIcons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { profilesApi } from '../../src/api/profiles';
import { mediaApi } from '../../src/api/media';
import { usersApi } from '../../src/api/users';
import { salonsApi } from '../../src/api/salons';
import { CoiffeurProfileResponse } from '../../src/types';
import { LogoutConfirmModal } from '../../src/components/common';
import { ProfileHeader, ProfileInfoCard, EditBottomSheet } from '../../src/components/profile';
import type { InfoCardItem } from '../../src/components/profile';

// Suggestions de specialites (vocabulaire coiffure courant, FR)
const SPECIALTY_SUGGESTIONS = [
  'Coupe homme', 'Coupe femme', 'Dégradé', 'Barbe', 'Coloration',
  'Balayage', 'Mèches', 'Tresses', 'Locks', 'Lissage',
  'Brushing', 'Chignon', 'Soin capillaire',
];

export default function ProfileScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user, refreshUser } = useAuthStore();

  const [stats, setStats] = useState<{ label: string; value: number }[]>([]);
  const [isLoadingStats, setIsLoadingStats] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Coiffeur profile (stored for pre-filling edit sheets + info card)
  const [coiffeurProfile, setCoiffeurProfile] = useState<CoiffeurProfileResponse | null>(null);

  const isHairstylist = user?.userType === 'hairstylist';

  // Invitation count for hairstylists (badge + banner)
  const [pendingInvCount, setPendingInvCount] = useState(0);

  // --- Bio edit ---
  const [showBioSheet, setShowBioSheet] = useState(false);
  const [bioDraft, setBioDraft] = useState('');
  const [isSavingBio, setIsSavingBio] = useState(false);
  const BIO_MAX = 150;

  const openBioSheet = () => { setBioDraft(user?.bio || ''); setShowBioSheet(true); };
  const saveBio = async () => {
    if (!user) return;
    setIsSavingBio(true);
    try {
      await usersApi.updateProfile({ bio: bioDraft.trim() });
      await refreshUser();
      setShowBioSheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingBio(false); }
  };

  // --- City edit ---
  const [showCitySheet, setShowCitySheet] = useState(false);
  const [cityDraft, setCityDraft] = useState('');
  const [isSavingCity, setIsSavingCity] = useState(false);
  const CITY_MAX = 100;

  const openCitySheet = () => { setCityDraft(user?.city || ''); setShowCitySheet(true); };
  const saveCity = async () => {
    if (!user) return;
    setIsSavingCity(true);
    try {
      await usersApi.updateProfile({ city: cityDraft.trim() });
      await refreshUser();
      setShowCitySheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingCity(false); }
  };

  // --- Instagram edit ---
  const [showInstagramSheet, setShowInstagramSheet] = useState(false);
  const [instagramDraft, setInstagramDraft] = useState('');
  const [isSavingInstagram, setIsSavingInstagram] = useState(false);
  const INSTAGRAM_MAX = 30;

  const openInstagramSheet = () => { setInstagramDraft(user?.instagramHandle || ''); setShowInstagramSheet(true); };
  const saveInstagram = async () => {
    if (!user) return;
    setIsSavingInstagram(true);
    try {
      await usersApi.updateProfile({ instagramHandle: instagramDraft.trim() });
      await refreshUser();
      setShowInstagramSheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingInstagram(false); }
  };

  // --- Experience edit (coiffeur only) ---
  const [showExpSheet, setShowExpSheet] = useState(false);
  const [expDraft, setExpDraft] = useState('');
  const [isSavingExp, setIsSavingExp] = useState(false);

  const openExpSheet = () => {
    setExpDraft(coiffeurProfile?.yearsExperience?.toString() || '');
    setShowExpSheet(true);
  };
  const buildCoiffeurPayload = (override: Record<string, unknown>) => ({
    bio: coiffeurProfile?.bio || undefined,
    specialties: coiffeurProfile?.specialties || [],
    yearsExperience: coiffeurProfile?.yearsExperience ?? undefined,
    certifications: coiffeurProfile?.certifications || undefined,
    instagramHandle: coiffeurProfile?.instagramHandle || undefined,
    ...override,
  });
  const saveExp = async () => {
    if (!user) return;
    const val = parseInt(expDraft, 10);
    if (isNaN(val) || val < 0 || val > 100) {
      Alert.alert(t('common.states.error'), t('profile.editExp.rangeError'));
      return;
    }
    setIsSavingExp(true);
    try {
      await profilesApi.updateCoiffeurProfile(user.id, buildCoiffeurPayload({ yearsExperience: val }));
      await reloadCoiffeurProfile();
      setShowExpSheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingExp(false); }
  };

  // --- Certifications edit (coiffeur only) ---
  const [showCertsSheet, setShowCertsSheet] = useState(false);
  const [certsDraft, setCertsDraft] = useState('');
  const [isSavingCerts, setIsSavingCerts] = useState(false);
  const CERTS_MAX = 2000;

  const openCertsSheet = () => {
    setCertsDraft(coiffeurProfile?.certifications || '');
    setShowCertsSheet(true);
  };
  const saveCerts = async () => {
    if (!user) return;
    setIsSavingCerts(true);
    try {
      await profilesApi.updateCoiffeurProfile(user.id, buildCoiffeurPayload({ certifications: certsDraft.trim() }));
      await reloadCoiffeurProfile();
      setShowCertsSheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingCerts(false); }
  };

  // --- Specialties edit (coiffeur only) ---
  const [showSpecSheet, setShowSpecSheet] = useState(false);
  const [specDraft, setSpecDraft] = useState<string[]>([]);
  const [isSavingSpec, setIsSavingSpec] = useState(false);
  const [customSpecInput, setCustomSpecInput] = useState('');
  const SPEC_MAX = 5;
  const SPEC_ITEM_MAX = 100;

  const openSpecSheet = () => {
    setSpecDraft([...(coiffeurProfile?.specialties || [])]);
    setCustomSpecInput('');
    setShowSpecSheet(true);
  };
  const toggleSpec = (spec: string) => {
    setSpecDraft((prev) =>
      prev.includes(spec) ? prev.filter((s) => s !== spec) : prev.length < SPEC_MAX ? [...prev, spec] : prev
    );
  };
  const addCustomSpec = () => {
    const val = customSpecInput.trim();
    if (!val || val.length > SPEC_ITEM_MAX || specDraft.length >= SPEC_MAX || specDraft.includes(val)) return;
    setSpecDraft((prev) => [...prev, val]);
    setCustomSpecInput('');
  };
  const saveSpec = async () => {
    if (!user) return;
    setIsSavingSpec(true);
    try {
      await profilesApi.updateCoiffeurProfile(user.id, buildCoiffeurPayload({ specialties: specDraft }));
      await reloadCoiffeurProfile();
      setShowSpecSheet(false);
    } catch (e: any) {
      Alert.alert(t('common.states.error'), e?.response?.data?.message || e?.message || t('common.states.error'));
    } finally { setIsSavingSpec(false); }
  };

  // --- Avatar upload ---
  const pickAndUploadAvatar = async () => {
    if (!user) return;
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.8,
    });
    if (result.canceled || !result.assets[0]) return;
    try {
      const path = await mediaApi.uploadImage(result.assets[0].uri, `avatar_${user.id}_${Date.now()}.jpg`);
      await usersApi.updateUserAvatar(user.id, path);
      await refreshUser();
    } catch {}
  };

  // --- Cover upload ---
  const pickAndUploadCover = async () => {
    if (!user) return;
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'], allowsEditing: true, aspect: [16, 9], quality: 0.8,
    });
    if (result.canceled || !result.assets[0]) return;
    try {
      const path = await mediaApi.uploadImage(result.assets[0].uri, `cover_${user.id}_${Date.now()}.jpg`);
      await usersApi.updateUserCoverImage(user.id, path);
      await refreshUser();
    } catch {}
  };

  // --- Load stats + coiffeur profile ---
  const reloadCoiffeurProfile = useCallback(async () => {
    if (!user || user.userType !== 'hairstylist') return;
    try {
      const p = await profilesApi.getCoiffeurProfile(user.id);
      setCoiffeurProfile(p);
      setStats([
        { label: t('profile.stats.followers'), value: p.statistics.followersCount },
        { label: t('profile.stats.posts'), value: p.statistics.postsCount },
        { label: t('profile.stats.likes'), value: p.statistics.totalLikes },
      ]);
    } catch {}
  }, [user, t]);

  const loadStats = useCallback(async () => {
    if (!user) return;
    setIsLoadingStats(true);
    try {
      if (user.userType === 'client') {
        const profile = await profilesApi.getClientProfile(user.id);
        setStats([
          { label: t('profile.stats.bookings'), value: profile.statistics.bookingsCount },
          { label: t('profile.stats.collections'), value: profile.statistics.collectionsCount },
          { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
        ]);
      } else if (user.userType === 'salon_owner') {
        const profile = await profilesApi.getSalonOwnerProfile(user.id);
        setStats([
          { label: t('profile.stats.salons'), value: profile.statistics.salonsCount },
          { label: t('profile.stats.followers'), value: profile.statistics.followersCount },
          { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
        ]);
      } else if (user.userType === 'hairstylist') {
        await reloadCoiffeurProfile();
        try {
          const invs = await salonsApi.getMyInvitations();
          setPendingInvCount(invs.length);
        } catch {
          setPendingInvCount(0);
        }
      }
    } catch {} finally {
      setIsLoadingStats(false);
    }
  }, [user, t, reloadCoiffeurProfile]);

  useEffect(() => { loadStats(); }, [loadStats]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    await Promise.all([refreshUser(), loadStats()]);
    setRefreshing(false);
  }, [refreshUser, loadStats]);

  const handleShare = useCallback(async () => {
    if (!user) return;
    const name = [user.firstName, user.lastName].filter(Boolean).join(' ');
    try { await Share.share({ message: `${name} sur Frollot` }); } catch {}
  }, [user]);

  const subtitleKey =
    user?.userType === 'hairstylist' ? 'profile.type.hairstylist' :
    user?.userType === 'salon_owner' ? 'profile.type.owner' :
    'profile.type.client';

  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || '?';

  const menuItems = [
    ...(user?.userType === 'salon_owner' ? [{
      icon: 'store' as const,
      label: t('profile.ownerDashboard.menuLabel'),
      onPress: () => router.push('/owner-dashboard'),
    }] : []),
    ...(isHairstylist ? [{
      icon: 'mail-outline' as const,
      label: t('myInvitations.title'),
      onPress: () => router.push('/my-invitations' as any),
      badge: pendingInvCount > 0 ? pendingInvCount : undefined,
    }] : []),
    { icon: 'favorite-border' as const, label: t('profile.favorites'), onPress: () => router.push(`/favorites/${user?.id}`) },
    { icon: 'archive' as const, label: t('profile.archives'), onPress: () => router.push(`/archives/${user?.id}`) },
    { icon: 'collections-bookmark' as const, label: t('profile.collections'), onPress: () => router.push(`/collections/user/${user?.id}`) },
    { icon: 'photo-library' as const, label: t('profile.portfolios'), onPress: () => router.push(`/portfolios?ownerId=${user?.id}&ownerType=${user?.userType}`) },
    { icon: 'settings' as const, label: t('settings.title'), onPress: () => router.push('/settings') },
  ];

  const actions = (
    <>
      <TouchableOpacity style={[s.actionBtn, { backgroundColor: colors.primary }]} onPress={openBioSheet} activeOpacity={0.7}>
        <MaterialCommunityIcons name="pencil-outline" size={16} color={colors.onPrimary} />
        <Text style={[s.actionLabel, { color: colors.onPrimary }]}>{t('profile.editBioTitle')}</Text>
      </TouchableOpacity>
      <TouchableOpacity style={[s.actionBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={handleShare} activeOpacity={0.7}>
        <MaterialCommunityIcons name="share-variant-outline" size={16} color={colors.onSurfaceVariant} />
        <Text style={[s.actionLabel, { color: colors.onSurfaceVariant }]}>{t('common.actions.share')}</Text>
      </TouchableOpacity>
    </>
  );

  // --- Info card items (conditional on type) ---
  const infoItems: InfoCardItem[] = [
    { icon: 'map-marker-outline', value: user?.city || '—', onEdit: openCitySheet },
    { icon: 'instagram', value: user?.instagramHandle ? `@${user.instagramHandle}` : '—', onEdit: openInstagramSheet },
  ];
  if (isHairstylist) {
    const cp = coiffeurProfile;
    infoItems.push({
      icon: 'briefcase-outline',
      value: cp?.yearsExperience != null ? `${cp.yearsExperience} ${t('profile.experience')}` : '—',
      onEdit: openExpSheet,
    });
    infoItems.push({
      icon: 'certificate-outline',
      value: cp?.certifications || '—',
      onEdit: openCertsSheet,
    });
    infoItems.push({
      icon: 'scissors-cutting',
      value: cp?.specialties && cp.specialties.length > 0 ? cp.specialties.join(', ') : '—',
      onEdit: openSpecSheet,
    });
  }

  // --- All suggestions for specialties sheet (merge custom + known) ---
  const allSuggestions = [...new Set([...SPECIALTY_SUGGESTIONS, ...specDraft])];

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      <ScrollView
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={handleRefresh} tintColor={colors.primary} />}
        contentContainerStyle={s.scrollContent}
        automaticallyAdjustContentInsets={false}
        contentInsetAdjustmentBehavior="never"
      >
        <ProfileHeader
          coverUrl={user?.coverImageUrl}
          avatarUrl={user?.avatarUrl}
          name={fullName}
          verified={user?.isVerified || user?.emailVerified}
          subtitle={t(subtitleKey)}
          stats={stats}
          bio={user?.bio}
          actions={actions}
          isOwnProfile
          onEditAvatar={pickAndUploadAvatar}
          onEditCover={pickAndUploadCover}
        />

        <ProfileInfoCard items={infoItems} />

        {/* Menu */}
        <View style={[s.menuCard, { backgroundColor: colors.surface }]}>
          {/* Invitation banner for hairstylists */}
          {isHairstylist && pendingInvCount > 0 && (
            <TouchableOpacity
              style={[s.invBanner, { backgroundColor: colors.tertiary + '18', borderColor: colors.tertiary + '40' }]}
              onPress={() => router.push('/my-invitations' as any)}
              activeOpacity={0.7}
            >
              <MaterialCommunityIcons name="email-fast-outline" size={22} color={colors.tertiary} />
              <Text style={[s.invBannerText, { color: colors.tertiary }]}>
                {t('myInvitations.banner', { count: pendingInvCount })}
              </Text>
              <MaterialIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={20} color={colors.tertiary} />
            </TouchableOpacity>
          )}
          {menuItems.map((item, index) => (
            <TouchableOpacity key={index} style={s.menuItem} onPress={item.onPress}>
              <MaterialIcons name={item.icon} size={24} color={colors.onSurfaceVariant} />
              <Text style={[s.menuLabel, { color: colors.onSurface }]}>{item.label}</Text>
              {'badge' in item && (item as any).badge > 0 && (
                <View style={[s.menuBadge, { backgroundColor: colors.error }]}>
                  <Text style={[s.menuBadgeText, { color: colors.onError }]}>{(item as any).badge}</Text>
                </View>
              )}
              <MaterialIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={24} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          ))}
        </View>

        <TouchableOpacity style={[s.logoutBtn, { borderColor: colors.error }]} onPress={() => setShowLogoutModal(true)}>
          <MaterialIcons name="logout" size={20} color={colors.error} />
          <Text style={[s.logoutLabel, { color: colors.error }]}>{t('settings.logout')}</Text>
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>

      <LogoutConfirmModal visible={showLogoutModal} onClose={() => setShowLogoutModal(false)} />

      {/* Bio sheet */}
      <EditBottomSheet visible={showBioSheet} onClose={() => setShowBioSheet(false)} title={t('profile.editBioTitle')} onSave={saveBio} isSaving={isSavingBio} saveDisabled={bioDraft.length > BIO_MAX}>
        <TextInput style={[s.bioInput, { color: colors.onSurface, backgroundColor: colors.surfaceContainerHigh }]} value={bioDraft} onChangeText={setBioDraft} placeholder={t('profile.editBioPlaceholder')} placeholderTextColor={colors.onSurfaceVariant} multiline maxLength={BIO_MAX} textAlignVertical="top" />
        <Text style={[s.counter, { color: bioDraft.length > BIO_MAX ? colors.error : colors.onSurfaceVariant }]}>{bioDraft.length} / {BIO_MAX}</Text>
      </EditBottomSheet>

      {/* City sheet */}
      <EditBottomSheet visible={showCitySheet} onClose={() => setShowCitySheet(false)} title={t('profile.editCityTitle')} onSave={saveCity} isSaving={isSavingCity} saveDisabled={cityDraft.length > CITY_MAX}>
        <TextInput style={[s.singleInput, { color: colors.onSurface, backgroundColor: colors.surfaceContainerHigh }]} value={cityDraft} onChangeText={setCityDraft} placeholder={t('profile.editCityPlaceholder')} placeholderTextColor={colors.onSurfaceVariant} maxLength={CITY_MAX} />
        <Text style={[s.counter, { color: colors.onSurfaceVariant }]}>{cityDraft.length} / {CITY_MAX}</Text>
      </EditBottomSheet>

      {/* Instagram sheet */}
      <EditBottomSheet visible={showInstagramSheet} onClose={() => setShowInstagramSheet(false)} title={t('profile.editInstagramTitle')} onSave={saveInstagram} isSaving={isSavingInstagram} saveDisabled={instagramDraft.length > INSTAGRAM_MAX}>
        <TextInput style={[s.singleInput, { color: colors.onSurface, backgroundColor: colors.surfaceContainerHigh }]} value={instagramDraft} onChangeText={setInstagramDraft} placeholder={t('profile.editInstagramPlaceholder')} placeholderTextColor={colors.onSurfaceVariant} maxLength={INSTAGRAM_MAX} autoCapitalize="none" autoCorrect={false} />
        <Text style={[s.counter, { color: colors.onSurfaceVariant }]}>{instagramDraft.length} / {INSTAGRAM_MAX}</Text>
      </EditBottomSheet>

      {/* Experience sheet (coiffeur) */}
      {isHairstylist && (
        <EditBottomSheet visible={showExpSheet} onClose={() => setShowExpSheet(false)} title={t('profile.editExp.title')} onSave={saveExp} isSaving={isSavingExp}>
          <TextInput style={[s.singleInput, { color: colors.onSurface, backgroundColor: colors.surfaceContainerHigh }]} value={expDraft} onChangeText={setExpDraft} placeholder="0" placeholderTextColor={colors.onSurfaceVariant} keyboardType="number-pad" maxLength={3} />
          <Text style={[s.counter, { color: colors.onSurfaceVariant }]}>{t('profile.editExp.hint')}</Text>
        </EditBottomSheet>
      )}

      {/* Certifications sheet (coiffeur) */}
      {isHairstylist && (
        <EditBottomSheet visible={showCertsSheet} onClose={() => setShowCertsSheet(false)} title={t('profile.editCerts.title')} onSave={saveCerts} isSaving={isSavingCerts} saveDisabled={certsDraft.length > CERTS_MAX}>
          <TextInput style={[s.bioInput, { color: colors.onSurface, backgroundColor: colors.surfaceContainerHigh }]} value={certsDraft} onChangeText={setCertsDraft} placeholder={t('profile.editCerts.placeholder')} placeholderTextColor={colors.onSurfaceVariant} multiline maxLength={CERTS_MAX} textAlignVertical="top" />
          <Text style={[s.counter, { color: certsDraft.length > CERTS_MAX ? colors.error : colors.onSurfaceVariant }]}>{certsDraft.length} / {CERTS_MAX}</Text>
        </EditBottomSheet>
      )}

      {/* Specialties sheet (coiffeur) */}
      {isHairstylist && (
        <EditBottomSheet visible={showSpecSheet} onClose={() => setShowSpecSheet(false)} title={t('profile.editSpec.title')} onSave={saveSpec} isSaving={isSavingSpec} saveDisabled={specDraft.length > SPEC_MAX}>
          <Text style={[s.counter, { color: specDraft.length >= SPEC_MAX ? colors.error : colors.onSurfaceVariant, marginBottom: 10 }]}>
            {specDraft.length} / {SPEC_MAX}
          </Text>
          <View style={s.chipsWrap}>
            {allSuggestions.map((spec) => {
              const selected = specDraft.includes(spec);
              return (
                <TouchableOpacity
                  key={spec}
                  style={[
                    s.chip,
                    selected
                      ? { backgroundColor: colors.primary }
                      : { backgroundColor: 'transparent', borderColor: colors.outlineVariant, borderWidth: 1 },
                  ]}
                  onPress={() => toggleSpec(spec)}
                  activeOpacity={0.7}
                >
                  {selected && <MaterialCommunityIcons name="check" size={14} color={colors.onPrimary} />}
                  <Text style={[s.chipText, { color: selected ? colors.onPrimary : colors.onSurface }]}>{spec}</Text>
                </TouchableOpacity>
              );
            })}
            {/* Add custom */}
            <View style={[s.chip, { backgroundColor: 'transparent', borderColor: colors.outlineVariant, borderWidth: 1, borderStyle: 'dashed' }]}>
              <TextInput
                style={[s.customSpecInput, { color: colors.onSurface }]}
                value={customSpecInput}
                onChangeText={setCustomSpecInput}
                placeholder={t('profile.editSpec.other')}
                placeholderTextColor={colors.onSurfaceVariant}
                maxLength={SPEC_ITEM_MAX}
                onSubmitEditing={addCustomSpec}
                returnKeyType="done"
              />
              {customSpecInput.trim().length > 0 && (
                <TouchableOpacity onPress={addCustomSpec} hitSlop={8}>
                  <MaterialCommunityIcons name="plus" size={16} color={colors.primary} />
                </TouchableOpacity>
              )}
            </View>
          </View>
        </EditBottomSheet>
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  scrollContent: { paddingBottom: 100 },
  actionBtn: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, paddingVertical: 10, paddingHorizontal: 16, borderRadius: 999, minHeight: 40 },
  actionLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  menuCard: { borderRadius: 16, overflow: 'hidden', marginHorizontal: 16, marginTop: 16, marginBottom: 24 },
  invBanner: { flexDirection: 'row', alignItems: 'center', gap: 10, marginHorizontal: 16, marginBottom: 8, paddingVertical: 12, paddingHorizontal: 16, borderRadius: 14, borderWidth: 1 },
  invBannerText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', flex: 1 },
  menuItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 16, paddingHorizontal: 16 },
  menuLabel: { fontFamily: 'Manrope-Regular', fontSize: 16, flex: 1, marginStart: 16 },
  menuBadge: { minWidth: 20, height: 20, borderRadius: 10, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 6, marginEnd: 8 },
  menuBadgeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  logoutBtn: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', height: 52, borderRadius: 28, borderWidth: 1, marginHorizontal: 16 },
  logoutLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', marginStart: 8 },
  singleInput: { fontFamily: 'Manrope-Regular', fontSize: 15, borderRadius: 14, padding: 14, minHeight: 48 },
  bioInput: { fontFamily: 'Manrope-Regular', fontSize: 15, lineHeight: 22, borderRadius: 14, padding: 14, minHeight: 120, textAlignVertical: 'top' },
  counter: { fontFamily: 'Manrope-Regular', fontSize: 12, textAlign: 'right', marginTop: 6 },
  chipsWrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: { flexDirection: 'row', alignItems: 'center', gap: 4, paddingVertical: 8, paddingHorizontal: 14, borderRadius: 999 },
  chipText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600' },
  customSpecInput: { fontFamily: 'Manrope-Regular', fontSize: 13, minWidth: 80, paddingVertical: 0 },
});
