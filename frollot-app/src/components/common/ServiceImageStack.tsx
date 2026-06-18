import { View, StyleSheet, I18nManager } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { resolveMediaUrl } from '../../utils/media';
import { SERVICE_CATEGORY_META } from '../../types';
import type { ServiceCategory } from '../../types';

interface Props {
  imageUrls: string[];
  category?: ServiceCategory;
  size?: number;
  colors: {
    surface: string;
    surfaceContainerHigh: string;
    outlineVariant: string;
    onSurfaceVariant: string;
  };
}

/**
 * Thumbnail for a service: 3D stacked images if multiple, single image, or
 * category-icon fallback when no images are available.
 */
export function ServiceImageStack({ imageUrls, category, size = 52, colors }: Props) {
  const radius = Math.round(size * 0.19);

  if (imageUrls.length === 0) {
    const catMeta = category ? SERVICE_CATEGORY_META[category] : undefined;
    return (
      <View style={[st.fallback, { width: size, height: size, borderRadius: radius, borderColor: colors.outlineVariant, backgroundColor: colors.surfaceContainerHigh }]}>
        <MaterialCommunityIcons
          name={catMeta?.icon ?? 'camera-plus-outline'}
          size={Math.round(size * 0.38)}
          color={colors.onSurfaceVariant}
        />
      </View>
    );
  }

  if (imageUrls.length === 1) {
    return (
      <View style={{ width: size, height: size }}>
        <Image
          source={{ uri: resolveMediaUrl(imageUrls[0]) }}
          style={{ width: size, height: size, borderRadius: radius }}
          contentFit="cover"
        />
      </View>
    );
  }

  // Multiple images: stack of up to 3, offset + slight rotation (3D effect)
  const stack = imageUrls.slice(0, 3).reverse(); // bottom layer first
  return (
    <View style={{ width: size, height: size }}>
      {stack.map((url, i) => {
        const offset = (stack.length - 1 - i) * 4;
        const rotate = (stack.length - 1 - i) * 3;
        return (
          <Image
            key={i}
            source={{ uri: resolveMediaUrl(url) }}
            style={[
              {
                width: size,
                height: size,
                borderRadius: radius,
                position: i === stack.length - 1 ? 'relative' : 'absolute',
                top: -offset,
                [I18nManager.isRTL ? 'right' : 'left']: offset,
                transform: [{ rotate: `${rotate}deg` }],
                borderWidth: 1.5,
                borderColor: colors.surface,
                zIndex: i,
              },
            ]}
            contentFit="cover"
          />
        );
      })}
    </View>
  );
}

const st = StyleSheet.create({
  fallback: { borderWidth: 1.5, borderStyle: 'dashed', alignItems: 'center', justifyContent: 'center' },
});
