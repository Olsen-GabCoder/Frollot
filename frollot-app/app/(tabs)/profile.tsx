import { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { Image } from 'expo-image';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { I18nManager } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { useTheme } from '../../src/theme';
import { useAuthStore } from '../../src/stores/authStore';
import { profilesApi } from '../../src/api/profiles';
import { mediaApi } from '../../src/api/media';
import { usersApi } from '../../src/api/users';
import { resolveMediaUrl } from '../../src/utils/media';
import { LogoutConfirmModal } from '../../src/components/common';

export default function ProfileScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { user, refreshUser } = useAuthStore();
  const { colors, typography: typo } = theme;

  // Stats
  const [stats, setStats] = useState<{ label: string; value: number }[]>([]);
  const [isLoadingStats, setIsLoadingStats] = useState(false);

  // Avatar upload
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  // Load profile stats (1 call per role, pre-computed by backend)
  useEffect(() => {
    if (!user) return;
    let ignore = false;
    const load = async () => {
      setIsLoadingStats(true);
      try {
        if (user.userType === 'client') {
          const profile = await profilesApi.getClientProfile(user.id);
          if (!ignore) setStats([
            { label: t('profile.stats.bookings'), value: profile.statistics.bookingsCount },
            { label: t('profile.stats.collections'), value: profile.statistics.collectionsCount },
            { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
          ]);
        } else if (user.userType === 'salon_owner') {
          const profile = await profilesApi.getSalonOwnerProfile(user.id);
          if (!ignore) setStats([
            { label: t('profile.stats.salons'), value: profile.statistics.salonsCount },
            { label: t('profile.stats.followers'), value: profile.statistics.followersCount },
            { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
          ]);
        } else if (user.userType === 'hairstylist') {
          const profile = await profilesApi.getCoiffeurProfile(user.id);
          if (!ignore) setStats([
            { label: t('profile.stats.followers'), value: profile.statistics.followersCount },
            { label: t('profile.stats.posts'), value: profile.statistics.postsCount },
            { label: t('profile.stats.likes'), value: profile.statistics.totalLikes },
          ]);
        }
      } catch {} finally {
        if (!ignore) setIsLoadingStats(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, [user]);

  const pickAvatar = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });
    if (!result.canceled && result.assets[0]) {
      setAvatarPreview(result.assets[0].uri);
    }
  };

  const saveAvatar = async () => {
    if (!avatarPreview || !user) return;
    setIsUploading(true);
    try {
      const fileName = `avatar_${user.id}_${Date.now()}.jpg`;
      const path = await mediaApi.uploadImage(avatarPreview, fileName);
      await usersApi.updateUserAvatar(user.id, path);
      await refreshUser();
      setAvatarPreview(null);
    } catch {} finally {
      setIsUploading(false);
    }
  };

  const cancelPreview = () => setAvatarPreview(null);

  const menuItems = [
    { icon: 'favorite-border' as const, label: t('profile.favorites'), onPress: () => router.push(`/favorites/${user?.id}`) },
    { icon: 'archive' as const, label: t('profile.archives'), onPress: () => router.push(`/archives/${user?.id}`) },
    { icon: 'collections-bookmark' as const, label: t('profile.collections'), onPress: () => router.push(`/collections/user/${user?.id}`) },
    { icon: 'photo-library' as const, label: t('profile.portfolios'), onPress: () => router.push(`/portfolios?ownerId=${user?.id}&ownerType=${user?.userType}`) },
    { icon: 'settings' as const, label: t('settings.title'), onPress: () => router.push('/settings') },
  ];

  const avatarUri = avatarPreview || resolveMediaUrl(user?.avatarUrl);

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      {/* Profile header */}
      <View style={styles.profileHeader}>
        <TouchableOpacity onPress={pickAvatar} activeOpacity={0.8}>
          {avatarUri ? (
            <Image source={{ uri: avatarUri }} style={styles.avatar} contentFit="cover" />
          ) : (
            <View style={[styles.avatar, { backgroundColor: colors.primaryContainer }]}>
              <Text style={[typo.headlineLarge, { color: colors.onPrimaryContainer }]}>
                {(user?.firstName?.[0] || 'F').toUpperCase()}
              </Text>
            </View>
          )}
          <View style={[styles.editBadge, { backgroundColor: colors.primary }]}>
            <MaterialIcons name="camera-alt" size={14} color={colors.onPrimary} />
          </View>
        </TouchableOpacity>

        {/* Avatar preview actions */}
        {avatarPreview && (
          <View style={styles.previewActions}>
            <TouchableOpacity style={[styles.previewBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={cancelPreview}>
              <Text style={[styles.previewBtnText, { color: colors.onSurface }]}>{t('common.actions.cancel')}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.previewBtn, { backgroundColor: colors.primary }]} onPress={saveAvatar} disabled={isUploading}>
              {isUploading ? (
                <ActivityIndicator size="small" color={colors.onPrimary} />
              ) : (
                <Text style={[styles.previewBtnText, { color: colors.onPrimary }]}>{t('common.actions.save')}</Text>
              )}
            </TouchableOpacity>
          </View>
        )}

        <Text style={[typo.headlineSmall, { color: colors.onBackground, marginTop: 12 }]}>
          {user?.firstName} {user?.lastName}
        </Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant }]}>
          {user?.email}
        </Text>
        {(user?.isVerified || user?.emailVerified) && (
          <View style={styles.verifiedRow}>
            <MaterialIcons name="verified" size={16} color={colors.primary} />
            <Text style={[typo.labelSmall, { color: colors.primary, marginStart: 4 }]}>
              {t('verification.verified')}
            </Text>
          </View>
        )}
      </View>

      {/* Stats */}
      {stats.length > 0 && (
        <View style={[styles.statsCard, { backgroundColor: colors.surface }]}>
          {stats.map((s, i) => (
            <View key={i} style={styles.statItem}>
              <Text style={[typo.headlineSmall, { color: colors.primary }]}>{s.value}</Text>
              <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant }]}>{s.label}</Text>
            </View>
          ))}
        </View>
      )}
      {isLoadingStats && (
        <ActivityIndicator size="small" color={colors.primary} style={{ marginBottom: 16 }} />
      )}

      {/* Menu */}
      <View style={[styles.menuCard, { backgroundColor: colors.surface }]}>
        {menuItems.map((item, index) => (
          <TouchableOpacity key={index} style={styles.menuItem} onPress={item.onPress}>
            <MaterialIcons name={item.icon} size={24} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyLarge, { color: colors.onSurface, flex: 1, marginStart: 16 }]}>
              {item.label}
            </Text>
            <MaterialIcons name={I18nManager.isRTL ? 'chevron-left' : 'chevron-right'} size={24} color={colors.onSurfaceVariant} />
          </TouchableOpacity>
        ))}
      </View>

      {/* Logout */}
      <TouchableOpacity style={[styles.logoutBtn, { borderColor: colors.error }]} onPress={() => setShowLogoutModal(true)}>
        <MaterialIcons name="logout" size={20} color={colors.error} />
        <Text style={[typo.labelLarge, { color: colors.error, marginStart: 8 }]}>
          {t('settings.logout')}
        </Text>
      </TouchableOpacity>

      {/* Logout confirmation modal (composant partagé) */}
      <LogoutConfirmModal visible={showLogoutModal} onClose={() => setShowLogoutModal(false)} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { paddingHorizontal: 16, paddingTop: 56, paddingBottom: 100 },
  profileHeader: { alignItems: 'center', marginBottom: 24 },
  avatar: { width: 88, height: 88, borderRadius: 44, justifyContent: 'center', alignItems: 'center', overflow: 'hidden' },
  editBadge: {
    position: 'absolute', bottom: 0, end: 0,
    width: 28, height: 28, borderRadius: 14,
    alignItems: 'center', justifyContent: 'center',
  },
  previewActions: { flexDirection: 'row', gap: 10, marginTop: 12 },
  previewBtn: { paddingVertical: 8, paddingHorizontal: 20, borderRadius: 999 },
  previewBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13, fontWeight: '700' },
  verifiedRow: { flexDirection: 'row', alignItems: 'center', marginTop: 8 },
  // Stats
  statsCard: { flexDirection: 'row', borderRadius: 16, padding: 16, marginBottom: 24 },
  statItem: { flex: 1, alignItems: 'center', gap: 2 },
  // Menu
  menuCard: { borderRadius: 16, overflow: 'hidden', marginBottom: 24 },
  menuItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 16, paddingHorizontal: 16 },
  // Logout
  logoutBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    height: 52, borderRadius: 28, borderWidth: 1,
  },
});
