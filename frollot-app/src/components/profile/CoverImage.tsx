import React from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { LinearGradient } from 'expo-linear-gradient';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';
import { resolveMediaUrl } from '../../utils/media';

interface CoverImageProps {
  coverUrl?: string | null;
  onEditCover?: () => void;
}

const COVER_HEIGHT = 200;

export function CoverImage({ coverUrl, onEditCover }: CoverImageProps) {
  const { colors } = useTheme();
  const resolved = resolveMediaUrl(coverUrl);

  return (
    <View style={[styles.container, { backgroundColor: colors.surfaceVariant }]}>
      {resolved ? (
        <Image source={{ uri: resolved }} style={styles.image} contentFit="cover" />
      ) : (
        <LinearGradient
          colors={[colors.primary + '33', colors.surfaceVariant]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={styles.image}
        />
      )}
      <LinearGradient
        colors={['transparent', 'rgba(0,0,0,0.45)']}
        style={styles.gradient}
      />
      {onEditCover && (
        <TouchableOpacity
          style={[styles.editBtn, { backgroundColor: 'rgba(0,0,0,0.4)' }]}
          onPress={onEditCover}
          activeOpacity={0.7}
        >
          <MaterialCommunityIcons name="camera" size={18} color="#FFFFFF" />
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    height: COVER_HEIGHT,
    width: '100%',
    position: 'relative',
  },
  image: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    start: 0,
    end: 0,
  },
  gradient: {
    position: 'absolute',
    bottom: 0,
    start: 0,
    end: 0,
    height: COVER_HEIGHT * 0.5,
  },
  editBtn: {
    position: 'absolute',
    top: 12,
    end: 12,
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
