import React, { useState, useCallback } from 'react';
import { TouchableOpacity, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { useTheme } from '../../theme';
import { socialApi } from '../../api/social';

type FollowTargetType = 'coiffeur' | 'salon' | 'user';

interface FollowButtonProps {
  targetId: string;
  targetType: FollowTargetType;
  isFollowed: boolean;
  onToggled?: (nowFollowed: boolean) => void;
  followLabel?: string;
  followingLabel?: string;
}

export function FollowButton({
  targetId,
  targetType,
  isFollowed,
  onToggled,
  followLabel,
  followingLabel,
}: FollowButtonProps) {
  const { colors } = useTheme();
  const [optimistic, setOptimistic] = useState(isFollowed);
  const [loading, setLoading] = useState(false);

  // Sync when parent changes (e.g. after refetch)
  React.useEffect(() => { setOptimistic(isFollowed); }, [isFollowed]);

  const handlePress = useCallback(async () => {
    if (loading) return;
    const next = !optimistic;
    setOptimistic(next); // optimistic
    setLoading(true);
    try {
      if (targetType === 'coiffeur') {
        if (next) await socialApi.followCoiffeur(targetId);
        else await socialApi.unfollowCoiffeur(targetId);
      } else if (targetType === 'salon') {
        if (next) await socialApi.followSalon(targetId);
        else await socialApi.unfollowSalon(targetId);
      } else {
        if (next) await socialApi.followUser(targetId);
        else await socialApi.unfollowUser(targetId);
      }
      onToggled?.(next);
    } catch {
      setOptimistic(!next); // rollback
    } finally {
      setLoading(false);
    }
  }, [loading, optimistic, targetId, targetType, onToggled]);

  const active = optimistic;

  return (
    <TouchableOpacity
      style={[
        styles.btn,
        { backgroundColor: active ? colors.surfaceContainerHigh : colors.primary },
      ]}
      onPress={handlePress}
      activeOpacity={0.7}
      disabled={loading}
    >
      {loading ? (
        <ActivityIndicator size="small" color={active ? colors.onSurfaceVariant : colors.onPrimary} />
      ) : (
        <Text
          style={[
            styles.label,
            { color: active ? colors.onSurfaceVariant : colors.onPrimary },
          ]}
        >
          {active ? (followingLabel ?? 'Suivi') : (followLabel ?? 'Suivre')}
        </Text>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  btn: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 999,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 40,
  },
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
});
