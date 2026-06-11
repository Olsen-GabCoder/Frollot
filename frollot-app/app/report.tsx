import { useEffect, useState } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet, ScrollView,
  TextInput, ActivityIndicator, KeyboardAvoidingView, Platform,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { Image } from 'expo-image';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { moderationApi } from '../src/api/moderation';
import { socialApi } from '../src/api/social';
import { resolveMediaUrl } from '../src/utils/media';
import { ReportedEntityType, ReportReason, PostResponse } from '../src/types';

const ENTITY_LABELS: Record<ReportedEntityType, string> = {
  [ReportedEntityType.POST]: 'Post',
  [ReportedEntityType.COMMENT]: 'Commentaire',
  [ReportedEntityType.USER]: 'Utilisateur',
  [ReportedEntityType.SALON]: 'Salon',
};

const REASONS: { reason: ReportReason; label: string; description: string }[] = [
  { reason: ReportReason.INAPPROPRIE, label: 'Contenu inapproprié', description: 'Violence, harcèlement ou contenu choquant' },
  { reason: ReportReason.SPAM, label: 'Spam publicitaire', description: 'Publicité non sollicitée ou contenu répétitif' },
  { reason: ReportReason.FAUX, label: 'Faux avant / après', description: 'Résultat trompeur ou photos falsifiées' },
  { reason: ReportReason.COPYRIGHT, label: 'Violation de droits d\u2019auteur', description: 'Contenu publié sans autorisation de son auteur' },
  { reason: ReportReason.AUTRE, label: 'Autre', description: 'Un problème qui ne figure pas dans cette liste' },
];

const MAX_INFO_LENGTH = 1000;

export default function ReportScreen() {
  const params = useLocalSearchParams<{ entityType?: string; entityId?: string }>();
  const { colors, typography: typo } = useTheme();

  const entityType = (params.entityType as ReportedEntityType) || ReportedEntityType.POST;
  const entityId = params.entityId ?? '';

  const [selectedReason, setSelectedReason] = useState<ReportReason | null>(null);
  const [additionalInfo, setAdditionalInfo] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitted, setIsSubmitted] = useState(false);

  // Contexte du contenu signalé (post uniquement pour l'instant)
  const [reportedPost, setReportedPost] = useState<PostResponse | null>(null);
  const [isLoadingContext, setIsLoadingContext] = useState(false);

  useEffect(() => {
    if (entityType !== ReportedEntityType.POST || !entityId) return;
    setIsLoadingContext(true);
    socialApi.getPostById(entityId)
      .then(setReportedPost)
      .catch(() => {}) // contexte facultatif : l'écran reste utilisable sans
      .finally(() => setIsLoadingContext(false));
  }, [entityId]);

  const goBack = () => (router.canGoBack() ? router.back() : router.replace('/(tabs)'));

  const handleSubmit = async () => {
    if (!selectedReason || isSubmitting) return;
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      await moderationApi.reportContent({
        reportedEntityType: entityType,
        reportedEntityId: entityId,
        reason: selectedReason,
        additionalInfo: additionalInfo.trim() || undefined,
      });
      setIsSubmitted(true);
    } catch (error: any) {
      setErrorMessage(error?.response?.data?.message || 'Impossible d\u2019envoyer le signalement. Réessayez.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const contextImage = reportedPost
    ? resolveMediaUrl(reportedPost.media?.[0]?.mediaUrl || reportedPost.imageUrl)
    : null;

  // Écran de confirmation après envoi
  if (isSubmitted) {
    return (
      <View style={[styles.container, styles.successWrap, { backgroundColor: colors.background }]}>
        <View style={[styles.successCircle, { backgroundColor: colors.successContainer }]}>
          <MaterialIcons name="check" size={40} color={colors.success} />
        </View>
        <Text style={[typo.headlineSmall, { color: colors.onBackground, textAlign: 'center', marginTop: 24 }]}>
          Signalement envoyé
        </Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: 8, maxWidth: 280 }]}>
          Merci de veiller sur la communauté. Notre équipe de modération examinera ce contenu rapidement.
        </Text>
        <TouchableOpacity style={[styles.successBtn, { backgroundColor: colors.primary }]} onPress={goBack}>
          <Text style={[typo.labelLarge, { color: colors.onPrimary }]}>Retour</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity style={[styles.backBtn, { backgroundColor: colors.surfaceContainerHigh }]} onPress={goBack}>
          <MaterialIcons name="arrow-back" size={22} color={colors.onSurface} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
        <Text style={[typo.overline, { color: colors.secondary }]}>Modération</Text>
        <Text style={[typo.headlineMedium, { color: colors.onBackground, marginTop: 4 }]}>Signaler</Text>
        <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 8 }]}>
          Votre signalement est confidentiel. La personne concernée ne saura pas qui est à l'origine du signalement.
        </Text>

        {/* Contexte du contenu signalé */}
        {entityType === ReportedEntityType.POST && (
          isLoadingContext ? (
            <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 24 }} />
          ) : reportedPost ? (
            <View style={[styles.contextCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              <View style={styles.contextHeader}>
                <View style={[styles.contextAvatar, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.titleMedium, { color: colors.onPrimaryContainer }]}>
                    {(reportedPost.authorName?.[0] || '?').toUpperCase()}
                  </Text>
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]} numberOfLines={1}>
                    {reportedPost.authorName}
                  </Text>
                  <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>Auteur du post</Text>
                </View>
                <View style={[styles.entityBadge, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.labelSmall, { color: colors.onPrimaryContainer }]}>{ENTITY_LABELS[entityType]}</Text>
                </View>
              </View>
              {!!reportedPost.content && (
                <Text style={[typo.bodyMedium, { color: colors.onSurface, marginTop: 10 }]} numberOfLines={4}>
                  {reportedPost.content}
                </Text>
              )}
              {contextImage && (
                <Image source={{ uri: contextImage }} style={styles.contextImage} contentFit="cover" />
              )}
            </View>
          ) : null
        )}

        {/* Motif */}
        <Text style={[typo.overline, styles.sectionTitle, { color: colors.secondary }]}>Motif du signalement</Text>
        {REASONS.map((r) => {
          const on = selectedReason === r.reason;
          return (
            <TouchableOpacity
              key={r.reason}
              style={[styles.reasonCard, {
                backgroundColor: on ? colors.primaryContainer : colors.surface,
                borderColor: on ? colors.primary : colors.outlineVariant,
                borderWidth: on ? 2 : 1,
              }]}
              activeOpacity={0.7}
              onPress={() => setSelectedReason(r.reason)}
              disabled={isSubmitting}
            >
              <View style={[styles.radioOuter, { borderColor: on ? colors.primary : colors.outline }]}>
                {on && <View style={[styles.radioInner, { backgroundColor: colors.primary }]} />}
              </View>
              <View style={{ flex: 1 }}>
                <Text style={[typo.titleSmall, { color: on ? colors.onPrimaryContainer : colors.onSurface }]}>
                  {r.label}
                </Text>
                <Text style={[typo.bodySmall, { color: on ? colors.onPrimaryContainer : colors.onSurfaceVariant, marginTop: 2 }]}>
                  {r.description}
                </Text>
              </View>
            </TouchableOpacity>
          );
        })}

        {/* Détails */}
        <Text style={[typo.overline, styles.sectionTitle, { color: colors.secondary }]}>Détails (facultatif)</Text>
        <TextInput
          style={[styles.infoInput, typo.bodyMedium, {
            backgroundColor: colors.surface,
            borderColor: colors.outlineVariant,
            color: colors.onSurface,
          }]}
          placeholder="Décrivez brièvement le problème..."
          placeholderTextColor={colors.onSurfaceVariant}
          multiline
          value={additionalInfo}
          onChangeText={(text) => text.length <= MAX_INFO_LENGTH && setAdditionalInfo(text)}
          editable={!isSubmitting}
        />
        {additionalInfo.length > 0 && (
          <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant, textAlign: 'right', marginTop: 4 }]}>
            {additionalInfo.length}/{MAX_INFO_LENGTH}
          </Text>
        )}

        {/* Erreur visible */}
        {errorMessage && (
          <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
            <MaterialIcons name="error-outline" size={20} color={colors.onErrorContainer} />
            <Text style={[typo.bodyMedium, { color: colors.onErrorContainer, flex: 1 }]}>{errorMessage}</Text>
          </View>
        )}

        {/* Actions */}
        <View style={styles.actions}>
          <TouchableOpacity
            style={[styles.actionBtn, { borderColor: colors.outlineVariant, borderWidth: 1 }]}
            onPress={goBack}
            disabled={isSubmitting}
          >
            <Text style={[typo.labelLarge, { color: colors.onSurface }]}>Annuler</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.actionBtn, {
              backgroundColor: selectedReason && !isSubmitting ? colors.primary : colors.surfaceContainerHigh,
            }]}
            onPress={handleSubmit}
            disabled={!selectedReason || isSubmitting}
          >
            {isSubmitting ? (
              <ActivityIndicator size="small" color={colors.onPrimary} />
            ) : (
              <Text style={[typo.labelLarge, { color: selectedReason ? colors.onPrimary : colors.onSurfaceVariant }]}>
                Envoyer
              </Text>
            )}
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { paddingTop: 52, paddingHorizontal: 16, paddingBottom: 4 },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: 'center', justifyContent: 'center' },
  content: { paddingHorizontal: 16, paddingTop: 12, paddingBottom: 48 },
  sectionTitle: { marginTop: 28, marginBottom: 10, marginLeft: 4 },
  // Context card
  contextCard: { borderRadius: 20, borderWidth: 1, padding: 16, marginTop: 24 },
  contextHeader: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  contextAvatar: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  entityBadge: { paddingVertical: 4, paddingHorizontal: 10, borderRadius: 999 },
  contextImage: { width: '100%', height: 180, borderRadius: 14, marginTop: 12 },
  // Reasons
  reasonCard: {
    flexDirection: 'row', alignItems: 'center', gap: 14,
    borderRadius: 16, padding: 16, marginBottom: 10,
  },
  radioOuter: { width: 22, height: 22, borderRadius: 11, borderWidth: 2, alignItems: 'center', justifyContent: 'center' },
  radioInner: { width: 12, height: 12, borderRadius: 6 },
  // Details
  infoInput: { borderRadius: 16, borderWidth: 1, padding: 14, minHeight: 110, textAlignVertical: 'top' },
  // Error
  errorCard: {
    flexDirection: 'row', alignItems: 'center', gap: 10,
    borderRadius: 14, padding: 14, marginTop: 20,
  },
  // Actions
  actions: { flexDirection: 'row', gap: 12, marginTop: 28 },
  actionBtn: { flex: 1, height: 52, borderRadius: 28, alignItems: 'center', justifyContent: 'center' },
  // Success
  successWrap: { justifyContent: 'center', alignItems: 'center', padding: 32 },
  successCircle: { width: 80, height: 80, borderRadius: 40, alignItems: 'center', justifyContent: 'center' },
  successBtn: { marginTop: 32, paddingVertical: 14, paddingHorizontal: 48, borderRadius: 28 },
});
