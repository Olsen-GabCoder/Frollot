import { useState } from 'react';
import { View, Text, TextInput, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { router } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../../src/theme';
import { resolveMediaUrl } from '../../src/utils/media';
import { salonsApi } from '../../src/api/salons';
import { Salon } from '../../src/types';

export default function ExploreScreen() {
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [query, setQuery] = useState('');
  const [results, setResults] = useState<Salon[]>([]);
  const [searched, setSearched] = useState(false);

  const handleSearch = async () => {
    if (!query.trim()) return;
    try {
      const salons = await salonsApi.getSalons({ query: query.trim() });
      setResults(salons);
      setSearched(true);
    } catch {}
  };

  const renderSalon = ({ item }: { item: Salon }) => (
    <TouchableOpacity
      style={[styles.salonCard, { backgroundColor: colors.surface }]}
      onPress={() => router.push(`/salon/${item.id}`)}
    >
      <View style={[styles.salonThumb, { backgroundColor: colors.surfaceContainerHigh }]}>
        {item.coverPhotoUrl && (
          <Image source={{ uri: resolveMediaUrl(item.coverPhotoUrl) }} style={styles.salonThumb} contentFit="cover" />
        )}
      </View>
      <View style={styles.salonInfo}>
        <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.name}</Text>
        <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>
          {item.address}, {item.city}
        </Text>
        {item.isVerified && (
          <View style={styles.verifiedBadge}>
            <MaterialIcons name="verified" size={14} color={colors.primary} />
            <Text style={[typo.labelSmall, { color: colors.primary, marginLeft: 4 }]}>
              {t('salon.verified')}
            </Text>
          </View>
        )}
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <Text style={[typo.headlineSmall, { color: colors.onBackground, paddingHorizontal: 16, paddingTop: 56, paddingBottom: 16 }]}>
        {t('tabs.explore')}
      </Text>

      <View style={styles.searchRow}>
        <TextInput
          style={[styles.searchInput, { backgroundColor: colors.surfaceContainerHigh, color: colors.onSurface, borderColor: colors.outlineVariant }]}
          placeholder={t('home.searchPlaceholder')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={query}
          onChangeText={setQuery}
          onSubmitEditing={handleSearch}
          returnKeyType="search"
        />
        <TouchableOpacity style={[styles.searchBtn, { backgroundColor: colors.primary }]} onPress={handleSearch}>
          <MaterialIcons name="search" size={24} color={colors.onPrimary} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={results}
        renderItem={renderSalon}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          searched ? (
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 40 }]}>
              {t('common.noResults')}
            </Text>
          ) : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  searchRow: { flexDirection: 'row', paddingHorizontal: 16, marginBottom: 16, gap: 8 },
  searchInput: { flex: 1, height: 48, borderRadius: 12, paddingHorizontal: 16, fontSize: 16, borderWidth: 1 },
  searchBtn: { width: 48, height: 48, borderRadius: 12, justifyContent: 'center', alignItems: 'center' },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  salonCard: { flexDirection: 'row', borderRadius: 16, padding: 12, marginBottom: 10 },
  salonThumb: { width: 72, height: 72, borderRadius: 12 },
  salonInfo: { flex: 1, marginLeft: 12, justifyContent: 'center' },
  verifiedBadge: { flexDirection: 'row', alignItems: 'center', marginTop: 4 },
});
