import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';
import { Avatar } from '../common/Avatar';
import { CoverImage } from './CoverImage';
import { StatCounter } from './StatCounter';

interface StatDef {
  label: string;
  value: number;
}

interface ProfileHeaderProps {
  coverUrl?: string | null;
  avatarUrl?: string | null;
  name: string;
  verified?: boolean;
  subtitle?: string;
  stats?: StatDef[];
  bio?: string | null;
  actions?: React.ReactNode;
  isOwnProfile?: boolean;
  onEditCover?: () => void;
  onEditAvatar?: () => void;
  onEditBio?: () => void;
}

const AVATAR_SIZE = 88;
const AVATAR_OVERLAP = AVATAR_SIZE / 2;

export function ProfileHeader({
  coverUrl,
  avatarUrl,
  name,
  verified,
  subtitle,
  stats,
  bio,
  actions,
  isOwnProfile,
  onEditCover,
  onEditAvatar,
  onEditBio,
}: ProfileHeaderProps) {
  const { colors } = useTheme();

  return (
    <View>
      <CoverImage
        coverUrl={coverUrl}
        onEditCover={isOwnProfile ? onEditCover : undefined}
      />

      <View style={[styles.body, { backgroundColor: colors.background }]}>
        {/* Avatar chevauchant */}
        <View style={styles.avatarRow}>
          <View style={[styles.avatarWrapper, { borderColor: colors.background }]}>
            <Avatar
              imageUrl={avatarUrl ?? undefined}
              initials={name.charAt(0).toUpperCase()}
              size={AVATAR_SIZE}
              tone="primary"
              ring
            />
            {isOwnProfile && onEditAvatar && (
              <TouchableOpacity
                style={[styles.avatarEditBtn, { backgroundColor: colors.primary }]}
                onPress={onEditAvatar}
                activeOpacity={0.7}
              >
                <MaterialCommunityIcons name="camera" size={14} color={colors.onPrimary} />
              </TouchableOpacity>
            )}
          </View>
        </View>

        {/* Nom + badge */}
        <View style={styles.nameRow}>
          <Text style={[styles.name, { color: colors.onBackground }]} numberOfLines={1}>
            {name}
          </Text>
          {verified && (
            <MaterialCommunityIcons
              name="check-decagram"
              size={20}
              color={colors.primary}
              style={styles.badge}
            />
          )}
        </View>

        {/* Sous-titre */}
        {subtitle ? (
          <Text style={[styles.subtitle, { color: colors.onSurfaceVariant }]}>{subtitle}</Text>
        ) : null}

        {/* Stats */}
        {stats && stats.length > 0 && (
          <View style={styles.statsRow}>
            {stats.map((s) => (
              <StatCounter key={s.label} value={s.value} label={s.label} />
            ))}
          </View>
        )}

        {/* Bio */}
        {bio ? (
          <Text style={[styles.bio, { color: colors.onBackground }]}>{bio}</Text>
        ) : null}

        {/* Zone d'actions (boutons reçus par props) */}
        {actions ? <View style={styles.actions}>{actions}</View> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  body: {
    paddingHorizontal: 20,
    paddingBottom: 8,
  },
  avatarRow: {
    marginTop: -AVATAR_OVERLAP,
    marginBottom: 8,
  },
  avatarWrapper: {
    width: AVATAR_SIZE + 6,
    height: AVATAR_SIZE + 6,
    borderRadius: (AVATAR_SIZE + 6) / 2,
    borderWidth: 3,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  avatarEditBtn: {
    position: 'absolute',
    bottom: 0,
    end: 0,
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  name: {
    fontFamily: 'CormorantGaramond-SemiBold',
    fontSize: 24,
    fontWeight: '600',
  },
  badge: {
    marginStart: 6,
  },
  subtitle: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    marginTop: 2,
  },
  statsRow: {
    flexDirection: 'row',
    gap: 24,
    marginTop: 14,
  },
  bio: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    lineHeight: 20,
    marginTop: 12,
  },
  actions: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 14,
  },
});
