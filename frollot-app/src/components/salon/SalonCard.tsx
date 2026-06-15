import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '../../theme';
import { resolveMediaUrl } from '../../utils/media';

interface SalonCardProps {
  name: string;
  location: string;
  rating?: number;
  reviewCount?: number;
  imageUrl?: string;
  onPress?: () => void;
  onFavoritePress?: () => void;
}

export function SalonCard({ name, location, rating = 0, reviewCount = 0, imageUrl, onPress, onFavoritePress }: SalonCardProps) {
  const { colors } = useTheme();
  return (
    <TouchableOpacity activeOpacity={0.85} onPress={onPress} style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
      <View style={styles.imageContainer}>
        {imageUrl ? (
          <Image source={{ uri: resolveMediaUrl(imageUrl) }} style={styles.image} contentFit="cover" />
        ) : (
          <View style={[styles.placeholder, { backgroundColor: colors.primaryContainer }]}>
            <MaterialCommunityIcons name="store" size={40} color={colors.primary} />
          </View>
        )}
        <TouchableOpacity style={styles.favoriteBtn} onPress={onFavoritePress}>
          <MaterialCommunityIcons name="heart-outline" size={18} color={colors.secondary} />
        </TouchableOpacity>
      </View>
      <View style={styles.content}>
        <Text style={[styles.name, { color: colors.onSurface }]} numberOfLines={1}>{name}</Text>
        <View style={styles.locationRow}>
          <MaterialCommunityIcons name="map-marker" size={15} color={colors.onSurfaceVariant} />
          <Text style={[styles.locationText, { color: colors.onSurfaceVariant }]} numberOfLines={1}>{location}</Text>
        </View>
        <View style={styles.ratingRow}>
          <MaterialCommunityIcons name="star" size={16} color={colors.tertiary} />
          <Text style={[styles.ratingText, { color: colors.onSurface }]}>{rating.toFixed(1)}</Text>
          <Text style={[styles.reviewText, { color: colors.onSurfaceVariant }]}>({reviewCount})</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 220,
    borderRadius: 16,
    overflow: 'hidden',
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.12,
    shadowRadius: 3,
    elevation: 1,
    borderWidth: 1,
  },
  imageContainer: {
    position: 'relative',
    height: 132,
  },
  image: {
    width: '100%',
    height: 132,
  },
  placeholder: {
    width: '100%',
    height: 132,
    alignItems: 'center',
    justifyContent: 'center',
  },
  favoriteBtn: {
    position: 'absolute',
    top: 10,
    end: 10,
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: 'rgba(255,255,255,0.9)', // design-fixed
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    padding: 12,
    paddingHorizontal: 14,
    paddingBottom: 14,
  },
  name: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '600',
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    marginTop: 4,
  },
  locationText: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
    lineHeight: 16,
    flex: 1,
  },
  ratingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 8,
  },
  ratingText: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
  reviewText: {
    fontFamily: 'Manrope-Regular',
    fontSize: 12,
  },
});
