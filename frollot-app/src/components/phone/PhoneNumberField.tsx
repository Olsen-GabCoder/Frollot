import React, { useEffect, useRef, useState } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import {
  AsYouType,
  CountryCode,
  isValidPhoneNumber,
  parsePhoneNumberFromString,
} from 'libphonenumber-js';
import { useTheme } from '../../theme';
import { Country, getCountryByIso2, getDefaultCountry } from '../../utils/countries';
import { CountryFlag } from './CountryFlag';
import { CountryPickerModal } from './CountryPickerModal';

interface PhoneNumberFieldProps {
  /** E.164 ('+24106123456') ou chaîne vide */
  value: string;
  /** E.164 à chaque frappe, null si champ vide */
  onChangeE164: (e164: string | null) => void;
  /** Validité du numéro courant (champ vide = true : « pas de numéro » est un état valide) */
  onValidityChange?: (valid: boolean) => void;
  label?: string;
  defaultIso2?: string;
  editable?: boolean;
}

/**
 * Champ téléphone international réutilisable (incrément 2) :
 * bouton-pays (drapeau + indicatif -> CountryPickerModal) accolé à la saisie
 * NATIONALE formatée à la frappe (AsYouType). Ne parle PAS au backend : produit
 * un E.164 + un booléen de validité via les callbacks, rien d'autre.
 */
export function PhoneNumberField({
  value,
  onChangeE164,
  onValidityChange,
  label,
  defaultIso2 = 'GA',
  editable = true,
}: PhoneNumberFieldProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const [country, setCountry] = useState<Country>(
    () => getCountryByIso2(defaultIso2) ?? getDefaultCountry()
  );
  const [display, setDisplay] = useState('');
  const [valid, setValid] = useState(true);
  const [focused, setFocused] = useState(false);
  const [pickerVisible, setPickerVisible] = useState(false);
  // Dernier E.164 émis : évite de re-parser notre propre émission quand le
  // parent renvoie value (boucle contrôlée), tout en acceptant un value externe.
  const lastEmittedRef = useRef<string | null>(null);

  const emit = (rawNational: string, iso2: CountryCode) => {
    const trimmed = rawNational.trim();
    if (!trimmed) {
      lastEmittedRef.current = null;
      setValid(true);
      onChangeE164(null);
      onValidityChange?.(true);
      return;
    }
    const parsed = parsePhoneNumberFromString(trimmed, iso2);
    const e164 = parsed?.number ?? null;
    const isValid = isValidPhoneNumber(trimmed, iso2);
    lastEmittedRef.current = e164;
    setValid(isValid);
    onChangeE164(e164);
    onValidityChange?.(isValid);
  };

  // Synchronisation depuis le parent (pré-remplissage E.164, reset à '') —
  // ignorée si value est simplement l'écho de notre dernière émission.
  useEffect(() => {
    if (value === (lastEmittedRef.current ?? '')) return;
    if (!value) {
      setDisplay('');
      setValid(true);
      lastEmittedRef.current = null;
      return;
    }
    const parsed = parsePhoneNumberFromString(value);
    if (parsed?.country) {
      const c = getCountryByIso2(parsed.country);
      if (c) setCountry(c);
      setDisplay(parsed.formatNational());
      setValid(parsed.isValid());
      lastEmittedRef.current = parsed.number;
    } else {
      // E.164 non attribuable à un pays : afficher brut, ne pas crasher
      setDisplay(value);
      setValid(false);
      lastEmittedRef.current = value;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value]);

  const handleTextChange = (text: string) => {
    // Formatage à la frappe SEULEMENT quand le texte s'allonge : en suppression,
    // AsYouType ré-insérerait la ponctuation qu'on vient d'effacer (piège connu).
    const next =
      text.length > display.length ? new AsYouType(country.iso2).input(text) : text;
    setDisplay(next);
    emit(next, country.iso2);
  };

  const handleCountrySelect = (next: Country) => {
    setPickerVisible(false);
    setCountry(next);
    // Reformater le numéro courant pour le nouveau pays (chiffres conservés)
    const digits = display.replace(/\D/g, '');
    const reformatted = digits ? new AsYouType(next.iso2).input(digits) : '';
    setDisplay(reformatted);
    emit(reformatted, next.iso2);
  };

  const hasInput = display.trim().length > 0;
  const activeColor = focused ? colors.primary : colors.onSurfaceVariant;
  const borderColor = focused ? colors.primary : colors.outline;

  return (
    <View>
      <Text style={[styles.label, { color: activeColor }]}>{label ?? t('phone.numberLabel')}</Text>
      <View
        style={[
          styles.container,
          { borderColor, borderWidth: focused ? 2 : 1, backgroundColor: colors.surface },
        ]}
      >
        <TouchableOpacity
          style={[styles.countryBtn, { borderEndColor: colors.outlineVariant }]}
          onPress={() => setPickerVisible(true)}
          disabled={!editable}
          accessibilityRole="button"
          accessibilityLabel={t('phone.countryButtonA11y', { name: country.nameFr, code: country.callingCode })}
        >
          <CountryFlag iso2={country.iso2} size={24} />
          <Text style={[styles.callingCode, { color: colors.onSurface }]}>
            +{country.callingCode}
          </Text>
          <MaterialIcons name="arrow-drop-down" size={20} color={colors.onSurfaceVariant} />
        </TouchableOpacity>

        <TextInput
          style={[styles.input, { color: colors.onSurface }]}
          value={display}
          onChangeText={handleTextChange}
          placeholder={t('phone.numberPlaceholder')}
          placeholderTextColor={colors.onSurfaceVariant}
          keyboardType="phone-pad"
          autoComplete="tel"
          textContentType="telephoneNumber"
          editable={editable}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
        />

        {/* Indicateur de validité DISCRET : check quand valide, rien d'agressif pendant la frappe */}
        {hasInput && valid && (
          <MaterialIcons name="check-circle" size={20} color={colors.success} />
        )}
      </View>

      <CountryPickerModal
        visible={pickerVisible}
        selectedIso2={country.iso2}
        onSelect={handleCountrySelect}
        onClose={() => setPickerVisible(false)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  label: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 12.5,
    fontWeight: '600',
    marginBottom: 6,
    letterSpacing: 0.3,
  },
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 56,
    borderRadius: 8,
    paddingEnd: 14,
  },
  countryBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingStart: 14,
    paddingEnd: 8,
    height: '100%',
    borderEndWidth: StyleSheet.hairlineWidth,
    marginEnd: 12,
  },
  callingCode: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 15,
    fontWeight: '600',
    fontVariant: ['tabular-nums'],
  },
  input: {
    flex: 1,
    fontFamily: 'Manrope-Regular',
    fontSize: 16,
    lineHeight: 24,
    padding: 0,
    margin: 0,
    borderWidth: 0,
    backgroundColor: 'transparent',
    ...Platform.select({
      web: {
        outlineStyle: 'none',
        outlineWidth: 0,
      } as any,
    }),
  },
});
