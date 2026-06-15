import React, { useMemo, useState } from 'react';
import {
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { useTheme } from '../../theme';
import { TextField } from '../ui';
import { Country, searchCountries } from '../../utils/countries';
import { CountryFlag } from './CountryFlag';

interface CountryPickerModalProps {
  visible: boolean;
  /** Pays actuellement sélectionné (marqué dans la liste) */
  selectedIso2?: string;
  onSelect: (country: Country) => void;
  onClose: () => void;
}

/**
 * Sélecteur de pays/indicatif (patron modal B22).
 * Recherche en direct par nom FR OU indicatif (« gab » ou « 241 » -> Gabon),
 * insensible casse/accents. ~250 lignes -> FlatList.
 */
export function CountryPickerModal({
  visible,
  selectedIso2,
  onSelect,
  onClose,
}: CountryPickerModalProps) {
  const { colors, typography: typo } = useTheme();
  const { t } = useTranslation();
  const [query, setQuery] = useState('');

  const results = useMemo(() => searchCountries(query), [query]);

  const handleClose = () => {
    setQuery('');
    onClose();
  };

  const handleSelect = (country: Country) => {
    setQuery('');
    onSelect(country);
  };

  const renderItem = ({ item }: { item: Country }) => {
    const selected = item.iso2 === selectedIso2;
    return (
      <TouchableOpacity
        style={[styles.row, selected && { backgroundColor: colors.primaryContainer }]}
        onPress={() => handleSelect(item)}
        accessibilityRole="button"
        accessibilityState={{ selected }}
      >
        <CountryFlag iso2={item.iso2} />
        <Text
          style={[
            styles.rowName,
            { color: selected ? colors.onPrimaryContainer : colors.onSurface },
          ]}
          numberOfLines={1}
        >
          {item.nameFr}
        </Text>
        <Text
          style={[
            styles.rowCode,
            { color: selected ? colors.onPrimaryContainer : colors.onSurfaceVariant },
          ]}
        >
          +{item.callingCode}
        </Text>
        {selected && <MaterialIcons name="check" size={20} color={colors.primary} />}
      </TouchableOpacity>
    );
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
      <Pressable style={styles.overlay} onPress={handleClose}>
        <Pressable
          onPress={(e) => e.stopPropagation()}
          style={[styles.card, { backgroundColor: colors.surface }]}
        >
          <View style={styles.header}>
            <Text style={[typo.titleMedium, { color: colors.onSurface, flex: 1 }]}>
              {t('phone.countryPickerTitle')}
            </Text>
            <TouchableOpacity onPress={handleClose} hitSlop={8} accessibilityLabel={t('common.actions.close')}>
              <MaterialIcons name="close" size={22} color={colors.onSurfaceVariant} />
            </TouchableOpacity>
          </View>

          <TextField
            label={t('common.actions.search')}
            icon="magnify"
            placeholder={t('phone.searchPlaceholder')}
            value={query}
            onChangeText={setQuery}
            autoCorrect={false}
            autoCapitalize="none"
          />

          <FlatList
            data={results}
            keyExtractor={(c) => c.iso2}
            renderItem={renderItem}
            style={styles.list}
            keyboardShouldPersistTaps="handled"
            initialNumToRender={20}
            ItemSeparatorComponent={() => (
              <View style={[styles.separator, { backgroundColor: colors.outlineVariant }]} />
            )}
            ListEmptyComponent={
              <Text
                style={[typo.bodyMedium, styles.empty, { color: colors.onSurfaceVariant }]}
              >
                {t('phone.noCountryMatch', { query })}
              </Text>
            }
          />
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.4)', // design-fixed
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  card: {
    width: '100%',
    maxWidth: 420,
    maxHeight: '85%',
    borderRadius: 24,
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  list: { marginTop: 12 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    paddingVertical: 12,
    paddingHorizontal: 10,
    borderRadius: 12,
  },
  rowName: {
    flex: 1,
    fontFamily: 'Manrope-SemiBold',
    fontSize: 15,
    fontWeight: '600',
  },
  rowCode: {
    fontFamily: 'Manrope-Regular',
    fontSize: 14,
    fontVariant: ['tabular-nums'],
  },
  separator: { height: StyleSheet.hairlineWidth, marginStart: 52 },
  empty: { textAlign: 'center', paddingVertical: 28 },
});
