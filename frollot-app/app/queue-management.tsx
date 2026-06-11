import { useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialIcons } from '@expo/vector-icons';
import { useTheme } from '../src/theme';
import { queueApi } from '../src/api/queue';
import { QueueStatusResponse, QueueEntryResponse, QueueEntryStatus } from '../src/types';

export default function QueueManagementScreen() {
  const { salonId } = useLocalSearchParams<{ salonId: string }>();
  const { t } = useTranslation();
  const theme = useTheme();
  const { colors, typography: typo } = theme;

  const STATUS_COLORS: Record<QueueEntryStatus, string> = {
    [QueueEntryStatus.WAITING]: colors.warning,
    [QueueEntryStatus.CALLED]: colors.info,
    [QueueEntryStatus.CANCELLED]: colors.error,
    [QueueEntryStatus.COMPLETED]: colors.success,
  };

  const [queueStatus, setQueueStatus] = useState<QueueStatusResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionRunning, setActionRunning] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const startPoll = () => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    intervalRef.current = setInterval(loadQueue, 15000);
  };
  const stopPoll = () => {
    if (intervalRef.current) { clearInterval(intervalRef.current); intervalRef.current = null; }
  };

  const loadQueue = async () => {
    if (!salonId) return;
    try {
      const data = await queueApi.getQueueStatus(salonId);
      setQueueStatus(data);
      setError(null);
    } catch (e: any) {
      setError(e?.message || 'Impossible de charger la file');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadQueue();
    startPoll();
    return () => stopPoll();
  }, [salonId]);

  const handleCallNext = async () => {
    if (!salonId || actionRunning || !queueStatus) return;
    setActionRunning(true);
    setActionError(null);
    stopPoll();

    // Optimistic: move first WAITING to CALLED
    const snapshot = queueStatus;
    const firstWaiting = queueStatus.entries.find(e => e.status === QueueEntryStatus.WAITING);
    if (firstWaiting) {
      setQueueStatus({
        ...queueStatus,
        entries: queueStatus.entries.map(e =>
          e.entryId === firstWaiting.entryId ? { ...e, status: QueueEntryStatus.CALLED } : e
        ),
      });
    }

    try {
      await queueApi.callNextClient(salonId);
    } catch (e: any) {
      setQueueStatus(snapshot); // rollback
      setActionError(e?.response?.data?.message || 'Erreur lors de l\'appel');
    } finally {
      setActionRunning(false);
      startPoll();
    }
  };

  const handleRemove = async (entryId: string) => {
    if (!salonId || actionRunning || !queueStatus) return;
    setActionRunning(true);
    setActionError(null);
    stopPoll();

    // Optimistic: remove entry from list
    const snapshot = queueStatus;
    setQueueStatus({
      ...queueStatus,
      entries: queueStatus.entries.filter(e => e.entryId !== entryId),
    });

    try {
      await queueApi.removeQueueEntry(salonId, entryId);
    } catch (e: any) {
      setQueueStatus(snapshot); // rollback
      setActionError(e?.response?.data?.message || 'Erreur lors du retrait');
    } finally {
      setActionRunning(false);
      startPoll();
    }
  };

  const waitingEntries = queueStatus?.entries.filter((e) => e.status === QueueEntryStatus.WAITING) ?? [];
  const calledEntries = queueStatus?.entries.filter((e) => e.status === QueueEntryStatus.CALLED) ?? [];
  const firstWaitingId = waitingEntries[0]?.entryId;

  if (isLoading) {
    return <View style={[styles.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[styles.header, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={() => router.back()}>
          <MaterialIcons name="arrow-back" size={24} color={colors.onSurface} />
        </TouchableOpacity>
        <Text style={[typo.titleLarge, { color: colors.onSurface, flex: 1, marginLeft: 16 }]}>
          {t('salon.queue')}
        </Text>
        <TouchableOpacity onPress={loadQueue} disabled={actionRunning}>
          <MaterialIcons name="refresh" size={24} color={actionRunning ? colors.outlineVariant : colors.onSurface} />
        </TouchableOpacity>
      </View>

      {/* Dashboard stats */}
      <View style={styles.statsRow}>
        <View style={[styles.statCard, { backgroundColor: colors.warningContainer }]}>
          <Text style={[typo.headlineSmall, { color: colors.onWarningContainer }]}>{waitingEntries.length}</Text>
          <Text style={[typo.labelSmall, { color: colors.onWarningContainer }]}>En attente</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: colors.infoContainer }]}>
          <Text style={[typo.headlineSmall, { color: colors.onInfoContainer }]}>{calledEntries.length}</Text>
          <Text style={[typo.labelSmall, { color: colors.onInfoContainer }]}>Appeles</Text>
        </View>
        <View style={[styles.statCard, { backgroundColor: colors.surfaceContainerHigh }]}>
          <Text style={[typo.headlineSmall, { color: colors.onSurface }]}>{queueStatus?.estimatedWaitForNew ?? 0}</Text>
          <Text style={[typo.labelSmall, { color: colors.onSurfaceVariant }]}>min attente</Text>
        </View>
      </View>

      {error && (
        <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
          <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{error}</Text>
        </View>
      )}

      {actionError && (
        <View style={[styles.errorCard, { backgroundColor: colors.errorContainer }]}>
          <Text style={[typo.bodyMedium, { color: colors.onErrorContainer }]}>{actionError}</Text>
        </View>
      )}

      <FlatList
        data={queueStatus?.entries ?? []}
        keyExtractor={(item) => item.entryId}
        renderItem={({ item }) => {
          const statusColor = STATUS_COLORS[item.status] || colors.onSurfaceVariant;
          const isFirstWaiting = item.entryId === firstWaitingId;

          return (
            <View style={[styles.entryCard, { backgroundColor: colors.surface }]}>
              <View style={styles.entryHeader}>
                <View style={[styles.positionBadge, { backgroundColor: colors.primaryContainer }]}>
                  <Text style={[typo.labelLarge, { color: colors.onPrimaryContainer }]}>{item.position}</Text>
                </View>
                <View style={{ flex: 1, marginLeft: 12 }}>
                  <Text style={[typo.titleSmall, { color: colors.onSurface }]}>{item.clientName}</Text>
                  {item.requestedServiceName && (
                    <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant }]}>{item.requestedServiceName}</Text>
                  )}
                </View>
                <View style={[styles.statusBadge, { backgroundColor: statusColor + '20' }]}>
                  <Text style={[typo.labelSmall, { color: statusColor }]}>{item.status}</Text>
                </View>
              </View>

              <View style={styles.entryMeta}>
                <MaterialIcons name="access-time" size={14} color={colors.onSurfaceVariant} />
                <Text style={[typo.bodySmall, { color: colors.onSurfaceVariant, marginLeft: 4 }]}>
                  ~{item.estimatedWaitMinutes} min
                </Text>
              </View>

              {/* Actions */}
              <View style={styles.actionsRow}>
                {isFirstWaiting && (
                  <TouchableOpacity
                    style={[styles.actionBtn, { backgroundColor: colors.primary }]}
                    onPress={handleCallNext} disabled={actionRunning}
                  >
                    {actionRunning ? <ActivityIndicator size="small" color={colors.onPrimary} /> : (
                      <Text style={[typo.labelMedium, { color: colors.onPrimary }]}>Appeler</Text>
                    )}
                  </TouchableOpacity>
                )}
                <TouchableOpacity
                  style={[styles.actionBtn, { borderColor: colors.error, borderWidth: 1 }]}
                  onPress={() => handleRemove(item.entryId)} disabled={actionRunning}
                >
                  <Text style={[typo.labelMedium, { color: colors.error }]}>Retirer</Text>
                </TouchableOpacity>
              </View>
            </View>
          );
        }}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <MaterialIcons name="people-outline" size={48} color={colors.onSurfaceVariant} />
            <Text style={[typo.bodyMedium, { color: colors.onSurfaceVariant, marginTop: 12 }]}>
              {t('salon.queueEmpty')}
            </Text>
          </View>
        }
        contentContainerStyle={styles.list}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', paddingTop: 52, paddingBottom: 12, paddingHorizontal: 16 },
  statsRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 12 },
  statCard: { flex: 1, padding: 16, borderRadius: 16, alignItems: 'center' },
  errorCard: { marginHorizontal: 16, padding: 12, borderRadius: 12, marginBottom: 8 },
  list: { paddingHorizontal: 16, paddingBottom: 100 },
  entryCard: { borderRadius: 16, padding: 16, marginBottom: 8 },
  entryHeader: { flexDirection: 'row', alignItems: 'center' },
  positionBadge: { width: 36, height: 36, borderRadius: 18, justifyContent: 'center', alignItems: 'center' },
  statusBadge: { paddingHorizontal: 10, paddingVertical: 4, borderRadius: 999 },
  entryMeta: { flexDirection: 'row', alignItems: 'center', marginTop: 8 },
  actionsRow: { flexDirection: 'row', gap: 8, marginTop: 12 },
  actionBtn: { paddingVertical: 8, paddingHorizontal: 16, borderRadius: 999 },
  emptyState: { alignItems: 'center', padding: 48 },
});
