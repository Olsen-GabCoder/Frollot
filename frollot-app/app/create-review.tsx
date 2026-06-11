import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { reviewsApi } from '../src/api/reviews';

export default function CreateReviewScreen() {
  const { salonId, salonName, bookingId, serviceName } = useLocalSearchParams<{
    salonId: string;
    salonName: string;
    bookingId: string;
    serviceName: string;
  }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const [rating, setRating] = useState(0);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (rating === 0) {
      setError('Veuillez selectionner une note');
      return;
    }
    if (!salonId || !bookingId) {
      setError(t('common.error'));
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      await reviewsApi.createReview({
        salonId,
        bookingId,
        rating,
        title: title.trim() || undefined,
        content: content.trim() || undefined,
      });
      Alert.alert(t('common.done'), t('review.submit'), [
        { text: 'OK', onPress: () => router.back() },
      ]);
    } catch (e: any) {
      setError(e?.response?.data?.message || t('common.error'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const ratingLabels = ['', 'Mauvais', 'Moyen', 'Bien', 'Tres bien', 'Excellent'];

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, marginLeft: 16 }]}>
          {t('review.writeReview')}
        </Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Booking info */}
        <View style={[styles.card, { backgroundColor: colors.surface }]}>
          <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{serviceName}</Text>
          <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 4 }]}>{salonName}</Text>
        </View>

        {/* Rating */}
        <View style={[styles.card, { backgroundColor: colors.surface, alignItems: 'center' }]}>
          <Text style={[typo.titleMedium, { color: colors.onSurface, marginBottom: 12 }]}>
            {t('review.rating')}
          </Text>
          <View style={styles.starsRow}>
            {[1, 2, 3, 4, 5].map((star) => (
              <TouchableOpacity key={star} onPress={() => setRating(star)} style={styles.starBtn}>
                <MaterialIcons
                  name={star <= rating ? 'star' : 'star-border'}
                  size={40}
                  color={colors.tertiary}
                />
              </TouchableOpacity>
            ))}
          </View>
          {rating > 0 && (
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
              {ratingLabels[rating]}
            </Text>
          )}
        </View>

        {/* Title */}
        <TextInput
          style={[styles.input, {
            backgroundColor: colors.surfaceContainerHigh,
            color: colors.onSurface,
            borderColor: colors.outlineVariant,
          }]}
          placeholder={`${t('review.title')} (${t('booking.notes').replace(/\(.*\)/, '').trim()})`}
          placeholderTextColor={colors.onSurfaceVariant}
          value={title}
          onChangeText={setTitle}
        />

        {/* Content */}
        <TextInput
          style={[styles.input, styles.contentInput, {
            backgroundColor: colors.surfaceContainerHigh,
            color: colors.onSurface,
            borderColor: colors.outlineVariant,
          }]}
          placeholder={t('review.content')}
          placeholderTextColor={colors.onSurfaceVariant}
          value={content}
          onChangeText={setContent}
          multiline
          textAlignVertical="top"
        />

        {/* Error */}
        {error && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
          </View>
        )}

        {/* Submit */}
        <TouchableOpacity
          style={[styles.submitBtn, { backgroundColor: rating > 0 ? colors.primary : colors.surfaceContainerHigh }]}
          onPress={handleSubmit}
          disabled={isSubmitting || rating === 0}
        >
          {isSubmitting ? (
            <ActivityIndicator color={colors.onPrimary} />
          ) : (
            <Text style={[typo.labelLarge, { color: rating > 0 ? colors.onPrimary : colors.onSurfaceVariant }]}>
              {t('review.submit')}
            </Text>
          )}
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  scrollContent: { padding: 16 },
  card: { padding: 20, borderRadius: 16, marginBottom: 12 },
  starsRow: { flexDirection: 'row' },
  starBtn: { paddingHorizontal: 4 },
  input: { height: 52, borderRadius: 12, paddingHorizontal: 16, marginBottom: 12, fontSize: 16, borderWidth: 1 },
  contentInput: { height: 120, paddingTop: 16 },
  errorCard: { padding: 12, borderRadius: 12, marginBottom: 12 },
  submitBtn: { height: 52, borderRadius: 28, justifyContent: 'center', alignItems: 'center', marginTop: 8 },
});
