import { useEffect, useState, useMemo, useCallback } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, FlatList } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useAuthStore } from '../../src/stores/authStore';
import { salonsApi } from '../../src/api/salons';
import { bookingsApi } from '../../src/api/bookings';
import { BookingStepper } from '../../src/components/booking';
import { PrimaryButton, OutlineButton } from '../../src/components/ui';
import { Avatar } from '../../src/components/common';
import { SalonService, StaffMember, TimeSlot, AvailableSlotsResponse } from '../../src/types';
import { useTheme } from '../../src/theme';

const MONTHS = ['Janvier','Février','Mars','Avril','Mai','Juin','Juillet','Août','Septembre','Octobre','Novembre','Décembre'];
const DAYS_SHORT = ['DIM','LUN','MAR','MER','JEU','VEN','SAM'];

type BookingStep = 'service' | 'staff' | 'date' | 'summary' | 'success';
const STEP_LABELS = ['Service', 'Coiffeur', 'Date', 'Récap'];

function getWeekDays(date: Date): Date[] {
  const d = new Date(date);
  const dayOfWeek = d.getDay();
  const monday = new Date(d);
  monday.setDate(d.getDate() - ((dayOfWeek + 6) % 7));
  return Array.from({ length: 7 }, (_, i) => { const r = new Date(monday); r.setDate(monday.getDate() + i); return r; });
}

export default function BookingScreen() {
  const { salonId, serviceId } = useLocalSearchParams<{ salonId: string; serviceId?: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { user } = useAuthStore();

  // Data
  const [services, setServices] = useState<SalonService[]>([]);
  const [staff, setStaff] = useState<StaffMember[]>([]);
  const [availableSlots, setAvailableSlots] = useState<TimeSlot[]>([]);

  // Selections
  const [selectedService, setSelectedService] = useState<SalonService | null>(null);
  const [selectedStaff, setSelectedStaff] = useState<StaffMember | null>(null); // null = any staff
  const [currentWeekStart, setCurrentWeekStart] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedTime, setSelectedTime] = useState<string | null>(null); // HH:mm for display
  const [selectedSlotDatetime, setSelectedSlotDatetime] = useState<string | null>(null); // full LocalDateTime for API
  const [notes, setNotes] = useState('');

  // UI state
  const [step, setStep] = useState<BookingStep>('service');
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingStaff, setIsLoadingStaff] = useState(false);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [bookingId, setBookingId] = useState<string | null>(null);

  const stepIndex = step === 'service' ? 0 : step === 'staff' ? 1 : step === 'date' ? 2 : 3;

  // Load services on mount
  useEffect(() => {
    const load = async () => {
      if (!salonId) return;
      try {
        const svcs = await salonsApi.getSalonServices(salonId);
        setServices(svcs);
        if (serviceId) {
          const found = svcs.find((s) => s.id === serviceId);
          if (found) { setSelectedService(found); setStep('staff'); }
        }
      } catch {} finally { setIsLoading(false); }
    };
    load();
  }, [salonId, serviceId]);

  // Load staff when service is selected and step is staff
  useEffect(() => {
    if (step !== 'staff' || !salonId || !selectedService) return;
    let ignore = false;
    const load = async () => {
      setIsLoadingStaff(true);
      try {
        const result = await salonsApi.getStaffBySpecialty(salonId, selectedService.category);
        if (!ignore) setStaff(result);
      } catch {
        if (!ignore) setStaff([]);
      } finally {
        if (!ignore) setIsLoadingStaff(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, [step, salonId, selectedService]);

  // Load available slots when date changes
  useEffect(() => {
    if (!salonId || !selectedService || !selectedDate) return;
    let ignore = false;
    const load = async () => {
      setIsLoadingSlots(true);
      setAvailableSlots([]);
      try {
        const dateStr = selectedDate.toISOString().split('T')[0];
        const result: AvailableSlotsResponse = await bookingsApi.getAvailableSlots(salonId, {
          salonId,
          serviceId: selectedService.id,
          staffId: selectedStaff?.id,
          date: dateStr,
          duration: selectedService.durationMinutes,
        });
        if (!ignore) setAvailableSlots(result.slots?.filter(s => s.available) ?? []);
      } catch {
        if (!ignore) setAvailableSlots([]);
      } finally {
        if (!ignore) setIsLoadingSlots(false);
      }
    };
    load();
    return () => { ignore = true; };
  }, [selectedDate, selectedStaff, selectedService, salonId]);

  const weekDays = useMemo(() => getWeekDays(currentWeekStart), [currentWeekStart]);
  const today = new Date(); today.setHours(0,0,0,0);
  const monthLabel = `${MONTHS[currentWeekStart.getMonth()]} ${currentWeekStart.getFullYear()}`;

  // Navigation helpers
  const goToStaff = (service: SalonService) => { setSelectedService(service); setStep('staff'); };
  const goToDate = (staffMember: StaffMember | null) => { setSelectedStaff(staffMember); setStep('date'); };
  const goToSummary = () => { setStep('summary'); };
  const goBack = () => {
    if (step === 'staff') { setStep('service'); setSelectedService(null); setSelectedStaff(null); }
    else if (step === 'date') { setStep('staff'); setSelectedDate(null); setSelectedTime(null); setSelectedSlotDatetime(null); }
    else if (step === 'summary') { setStep('date'); }
    else router.back();
  };

  const handleBook = async () => {
    if (!salonId || !user || !selectedService || !selectedSlotDatetime) return;
    setBookingError(null);
    setIsBooking(true);
    try {
      const booking = await bookingsApi.createBooking({
        salonId, clientId: user.id, serviceId: selectedService.id,
        staffId: selectedStaff?.id,
        bookingDatetime: selectedSlotDatetime,
        notesClient: notes.trim() || undefined,
      });
      setBookingId(booking.id);
      setStep('success');
    } catch (e: any) {
      setBookingError(e?.response?.data?.message || 'Erreur lors de la réservation. Veuillez réessayer.');
    } finally { setIsBooking(false); }
  };

  if (isLoading) return <View style={[s.centered, { backgroundColor: colors.background }]}><ActivityIndicator size="large" color={colors.primary} /></View>;

  // === SUCCESS SCREEN ===
  if (step === 'success') {
    return (
      <View style={[s.container, { backgroundColor: colors.background }]}>
        <View style={[s.successContent, { backgroundColor: colors.background }]}>
          <View style={[s.successIcon, { backgroundColor: colors.successContainer }]}>
            <MaterialCommunityIcons name="check-circle" size={64} color={colors.success} />
          </View>
          <Text style={[s.successTitle, { color: colors.onSurface }]}>Réservation confirmée !</Text>
          <Text style={[s.successMessage, { color: colors.onSurfaceVariant }]}>
            Votre rendez-vous a été enregistré. Vous recevrez une confirmation.
          </Text>
          {selectedService && (
            <View style={[s.successCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              <Text style={[s.successServiceName, { color: colors.onSurface }]}>{selectedService.name}</Text>
              {selectedDate && selectedTime && (
                <Text style={[s.successDetail, { color: colors.onSurfaceVariant }]}>
                  {DAYS_SHORT[selectedDate.getDay()]}. {selectedDate.getDate()} {MONTHS[selectedDate.getMonth()].toLowerCase()} · {selectedTime}
                </Text>
              )}
              {selectedStaff && (
                <Text style={[s.successDetail, { color: colors.onSurfaceVariant }]}>
                  Avec {selectedStaff.userFirstName} {selectedStaff.userLastName}
                </Text>
              )}
              <Text style={[s.successPrice, { color: colors.primary }]}>{selectedService.price} €</Text>
            </View>
          )}
          <View style={s.successActions}>
            <PrimaryButton icon="calendar-check" full onPress={() => router.replace(`/booking/${bookingId}`)}>
              Voir ma réservation
            </PrimaryButton>
            <OutlineButton full onPress={() => router.replace('/(tabs)')}>
              Retour à l'accueil
            </OutlineButton>
          </View>
        </View>
      </View>
    );
  }

  return (
    <View style={[s.container, { backgroundColor: colors.background }]}>
      {/* Header */}
      <View style={[s.header, { backgroundColor: colors.surface }]}>
        <View style={s.headerRow}>
          <TouchableOpacity style={s.iconBtn} onPress={goBack}>
            <MaterialCommunityIcons name="arrow-left" size={24} color={colors.onSurface} />
          </TouchableOpacity>
          <View style={{ flex: 1 }}>
            <Text style={[s.headerTitle, { color: colors.onSurface }]}>Réserver</Text>
            <Text style={[s.headerSub, { color: colors.onSurfaceVariant }]}>Salon</Text>
          </View>
          <TouchableOpacity style={s.iconBtn} onPress={() => router.back()}>
            <MaterialCommunityIcons name="close" size={24} color={colors.onSurface} />
          </TouchableOpacity>
        </View>
        <BookingStepper steps={STEP_LABELS} currentStep={stepIndex} />
      </View>

      <ScrollView style={s.scroll} contentContainerStyle={s.scrollContent} showsVerticalScrollIndicator={false}>

        {/* === STEP: SERVICE SELECTION === */}
        {step === 'service' && services.map((svc) => (
          <TouchableOpacity key={svc.id} style={[s.serviceItem, { borderBottomColor: colors.outlineVariant }]} onPress={() => goToStaff(svc)}>
            <View style={{ flex: 1 }}>
              <Text style={[s.serviceCategory, { color: colors.secondary }]}>{svc.category}</Text>
              <Text style={[s.serviceItemName, { color: colors.onSurface }]}>{svc.name}</Text>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 5, marginTop: 5 }}>
                <MaterialCommunityIcons name="clock-outline" size={15} color={colors.onSurfaceVariant} />
                <Text style={[s.serviceItemMeta, { color: colors.onSurfaceVariant }]}>{svc.durationMinutes} min</Text>
                <Text style={{ color: colors.outlineVariant, marginHorizontal: 4 }}>·</Text>
                <Text style={[s.serviceItemPrice, { color: colors.onSurface }]}>{svc.price} €</Text>
              </View>
            </View>
            <View style={[s.reserveBtn, { borderColor: colors.primary }]}><Text style={[s.reserveBtnText, { color: colors.primary }]}>Choisir</Text></View>
          </TouchableOpacity>
        ))}

        {/* === STEP: STAFF SELECTION === */}
        {step === 'staff' && (
          <>
            {/* Service summary */}
            <View style={[s.serviceSummary, { backgroundColor: colors.primaryContainer }]}>
              <MaterialCommunityIcons name="content-cut" size={22} color={colors.onPrimaryContainer} />
              <View style={{ flex: 1 }}>
                <Text style={[s.serviceName, { color: colors.onPrimaryContainer }]}>{selectedService?.name}</Text>
                <Text style={[s.serviceMeta, { color: colors.onPrimaryContainer, opacity: 0.8 }]}>{selectedService?.durationMinutes} min · {selectedService?.price} €</Text>
              </View>
            </View>

            <Text style={[s.sectionTitle, { color: colors.onSurface }]}>Choisir un coiffeur</Text>

            {isLoadingStaff ? (
              <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 20 }} />
            ) : (
              <>
                {/* Any staff option */}
                <TouchableOpacity
                  style={[s.staffItem, { borderBottomColor: colors.outlineVariant, backgroundColor: selectedStaff === null ? colors.primaryContainer : undefined }]}
                  onPress={() => goToDate(null)}
                >
                  <View style={[s.staffAvatar, { backgroundColor: colors.surfaceContainerHigh }]}>
                    <MaterialCommunityIcons name="account-group" size={24} color={colors.onSurfaceVariant} />
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={[s.staffName, { color: colors.onSurface }]}>N'importe qui</Text>
                    <Text style={[s.staffSpecialty, { color: colors.onSurfaceVariant }]}>Premier coiffeur disponible</Text>
                  </View>
                  <MaterialCommunityIcons name="chevron-right" size={22} color={colors.onSurfaceVariant} />
                </TouchableOpacity>

                {staff.map((m) => (
                  <TouchableOpacity
                    key={m.id}
                    style={[s.staffItem, { borderBottomColor: colors.outlineVariant }]}
                    onPress={() => goToDate(m)}
                  >
                    <Avatar initials={`${m.userFirstName?.[0] || ''}${m.userLastName?.[0] || ''}`} size={44} tone="secondary" imageUrl={m.userAvatarUrl} />
                    <View style={{ flex: 1 }}>
                      <Text style={[s.staffName, { color: colors.onSurface }]}>{m.userFirstName} {m.userLastName}</Text>
                      <Text style={[s.staffSpecialty, { color: colors.onSurfaceVariant }]}>{m.specialtyLabels?.join(', ') || m.specialties?.join(', ')}</Text>
                    </View>
                    <MaterialCommunityIcons name="chevron-right" size={22} color={colors.onSurfaceVariant} />
                  </TouchableOpacity>
                ))}

                {staff.length === 0 && !isLoadingStaff && (
                  <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>Aucun coiffeur spécialisé trouvé. Choisissez « N'importe qui ».</Text>
                )}
              </>
            )}
          </>
        )}

        {/* === STEP: DATE/TIME SELECTION === */}
        {step === 'date' && selectedService && (
          <>
            {/* Service + staff summary */}
            <View style={[s.serviceSummary, { backgroundColor: colors.primaryContainer }]}>
              <MaterialCommunityIcons name="content-cut" size={22} color={colors.onPrimaryContainer} />
              <View style={{ flex: 1 }}>
                <Text style={[s.serviceName, { color: colors.onPrimaryContainer }]}>{selectedService.name}</Text>
                <Text style={[s.serviceMeta, { color: colors.onPrimaryContainer, opacity: 0.8 }]}>
                  {selectedService.durationMinutes} min · {selectedService.price} € · {selectedStaff ? `${selectedStaff.userFirstName}` : 'N\'importe qui'}
                </Text>
              </View>
            </View>

            {/* Month nav */}
            <View style={s.monthRow}>
              <Text style={[s.monthTitle, { color: colors.onSurface }]}>{monthLabel}</Text>
              <View style={{ flexDirection: 'row', gap: 4 }}>
                <TouchableOpacity style={s.navBtn} onPress={() => {
                  const d = new Date(currentWeekStart); d.setDate(d.getDate() - 7);
                  setCurrentWeekStart(d); setSelectedDate(null); setSelectedTime(null); setSelectedSlotDatetime(null);
                }}>
                  <MaterialCommunityIcons name="chevron-left" size={22} color={colors.onSurface} />
                </TouchableOpacity>
                <TouchableOpacity style={s.navBtn} onPress={() => {
                  const d = new Date(currentWeekStart); d.setDate(d.getDate() + 7);
                  setCurrentWeekStart(d); setSelectedDate(null); setSelectedTime(null); setSelectedSlotDatetime(null);
                }}>
                  <MaterialCommunityIcons name="chevron-right" size={22} color={colors.onSurface} />
                </TouchableOpacity>
              </View>
            </View>

            {/* Days */}
            <View style={s.daysRow}>
              {weekDays.map((day, i) => {
                const isPast = day < today;
                const isOff = day.getDay() === 0;
                const isSel = selectedDate?.toDateString() === day.toDateString();
                return (
                  <TouchableOpacity
                    key={i}
                    disabled={isPast || isOff}
                    onPress={() => { setSelectedDate(day); setSelectedTime(null); setSelectedSlotDatetime(null); }}
                    style={[
                      s.dayCell,
                      isSel
                        ? { backgroundColor: colors.primary }
                        : isOff
                        ? [s.dayOff, { borderColor: colors.outlineVariant }]
                        : { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.outlineVariant },
                      (isPast || isOff) && { opacity: 0.5 },
                    ]}
                  >
                    <Text style={[s.dayAbbr, { color: isSel ? colors.onPrimary : colors.onSurfaceVariant }]}>{DAYS_SHORT[day.getDay()]}</Text>
                    <Text style={[s.dayNum, { color: isSel ? colors.onPrimary : colors.onSurface }]}>{day.getDate()}</Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Time slots (from API) */}
            {selectedDate && (
              <>
                <Text style={[s.slotsOverline, { color: colors.onSurfaceVariant }]}>
                  Créneaux disponibles · {DAYS_SHORT[selectedDate.getDay()]} {selectedDate.getDate()}
                </Text>
                {isLoadingSlots ? (
                  <ActivityIndicator size="small" color={colors.primary} style={{ marginTop: 12 }} />
                ) : availableSlots.length === 0 ? (
                  <Text style={[s.emptyText, { color: colors.onSurfaceVariant }]}>Aucun créneau disponible pour cette date.</Text>
                ) : (
                  <View style={s.slotsGrid}>
                    {availableSlots.map((slot, i) => {
                      const time = slot.datetime?.split('T')[1]?.substring(0, 5) || slot.datetime;
                      const isSel = selectedTime === time;
                      return (
                        <TouchableOpacity
                          key={i}
                          onPress={() => { setSelectedTime(time); setSelectedSlotDatetime(slot.datetime); }}
                          style={[s.slot, isSel ? { backgroundColor: colors.primary } : { backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.outlineVariant }]}
                        >
                          <Text style={[s.slotText, { color: isSel ? colors.onPrimary : colors.onSurface }]}>{time}</Text>
                        </TouchableOpacity>
                      );
                    })}
                  </View>
                )}
              </>
            )}
          </>
        )}

        {/* === STEP: SUMMARY === */}
        {step === 'summary' && selectedService && selectedDate && selectedTime && (
          <>
            <Text style={[s.sectionTitle, { color: colors.onSurface }]}>Récapitulatif</Text>

            <View style={[s.summaryCard, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
              <View style={s.summaryRow}>
                <MaterialCommunityIcons name="content-cut" size={18} color={colors.primary} />
                <Text style={[s.summaryLabel, { color: colors.onSurfaceVariant }]}>Service</Text>
                <Text style={[s.summaryValue, { color: colors.onSurface }]}>{selectedService.name}</Text>
              </View>
              <View style={[s.summaryDivider, { backgroundColor: colors.outlineVariant }]} />
              <View style={s.summaryRow}>
                <MaterialCommunityIcons name="account" size={18} color={colors.primary} />
                <Text style={[s.summaryLabel, { color: colors.onSurfaceVariant }]}>Coiffeur</Text>
                <Text style={[s.summaryValue, { color: colors.onSurface }]}>
                  {selectedStaff ? `${selectedStaff.userFirstName} ${selectedStaff.userLastName}` : 'N\'importe qui'}
                </Text>
              </View>
              <View style={[s.summaryDivider, { backgroundColor: colors.outlineVariant }]} />
              <View style={s.summaryRow}>
                <MaterialCommunityIcons name="calendar" size={18} color={colors.primary} />
                <Text style={[s.summaryLabel, { color: colors.onSurfaceVariant }]}>Date</Text>
                <Text style={[s.summaryValue, { color: colors.onSurface }]}>
                  {DAYS_SHORT[selectedDate.getDay()]}. {selectedDate.getDate()} {MONTHS[selectedDate.getMonth()].toLowerCase()} · {selectedTime}
                </Text>
              </View>
              <View style={[s.summaryDivider, { backgroundColor: colors.outlineVariant }]} />
              <View style={s.summaryRow}>
                <MaterialCommunityIcons name="clock-outline" size={18} color={colors.primary} />
                <Text style={[s.summaryLabel, { color: colors.onSurfaceVariant }]}>Durée</Text>
                <Text style={[s.summaryValue, { color: colors.onSurface }]}>{selectedService.durationMinutes} min</Text>
              </View>
              <View style={[s.summaryDivider, { backgroundColor: colors.outlineVariant }]} />
              <View style={s.summaryRow}>
                <MaterialCommunityIcons name="cash" size={18} color={colors.primary} />
                <Text style={[s.summaryLabel, { color: colors.onSurfaceVariant }]}>Prix</Text>
                <Text style={[s.summaryValue, { color: colors.primary }]}>{selectedService.price} €</Text>
              </View>
            </View>

            {/* Booking error */}
            {bookingError && (
              <View style={[s.errorCard, { backgroundColor: colors.errorContainer }]}>
                <MaterialCommunityIcons name="alert-circle" size={18} color={colors.onErrorContainer} />
                <Text style={[s.errorText, { color: colors.onErrorContainer }]}>{bookingError}</Text>
              </View>
            )}

            {/* Notes */}
            <Text style={[s.notesLabel, { color: colors.onSurface }]}>Notes ou demandes spéciales</Text>
            <TextInput
              style={[s.notesInput, { backgroundColor: colors.surface, color: colors.onSurface, borderColor: colors.outlineVariant }]}
              placeholder="Ex: Coupe courte avec dégradé, allergie au latex..."
              placeholderTextColor={colors.onSurfaceVariant}
              value={notes}
              onChangeText={setNotes}
              multiline
              textAlignVertical="top"
            />
          </>
        )}
      </ScrollView>

      {/* Bottom bar — Date step */}
      {step === 'date' && selectedDate && selectedTime && (
        <View style={[s.bottomBar, { backgroundColor: colors.surface, borderTopColor: colors.outlineVariant }]}>
          <View>
            <Text style={[s.bottomDate, { color: colors.onSurfaceVariant }]}>
              {DAYS_SHORT[selectedDate.getDay()]}. {selectedDate.getDate()} {MONTHS[selectedDate.getMonth()].toLowerCase()} · {selectedTime}
            </Text>
            <Text style={[s.bottomPrice, { color: colors.onSurface }]}>{selectedService?.price} €</Text>
          </View>
          <PrimaryButton icon="arrow-right" full onPress={goToSummary} style={s.continueBtn}>
            Continuer
          </PrimaryButton>
        </View>
      )}

      {/* Bottom bar — Summary step */}
      {step === 'summary' && (
        <View style={[s.bottomBar, { backgroundColor: colors.surface, borderTopColor: colors.outlineVariant }]}>
          <View>
            <Text style={[s.bottomDate, { color: colors.onSurfaceVariant }]}>
              {selectedDate && selectedTime && `${DAYS_SHORT[selectedDate.getDay()]}. ${selectedDate.getDate()} ${MONTHS[selectedDate.getMonth()].toLowerCase()} · ${selectedTime}`}
            </Text>
            <Text style={[s.bottomPrice, { color: colors.onSurface }]}>{selectedService?.price} €</Text>
          </View>
          <PrimaryButton icon="check" full onPress={handleBook} loading={isBooking} style={s.continueBtn}>
            Confirmer
          </PrimaryButton>
        </View>
      )}
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  centered: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  header: { paddingHorizontal: 8, paddingTop: 6, paddingBottom: 16 },
  headerRow: { flexDirection: 'row', alignItems: 'center', gap: 6, minHeight: 52 },
  iconBtn: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { fontFamily: 'Manrope-SemiBold', fontSize: 22, fontWeight: '600', lineHeight: 24 },
  headerSub: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  scroll: { flex: 1 },
  scrollContent: { padding: 20, paddingBottom: 120 },
  // Service summary
  serviceSummary: {
    flexDirection: 'row', alignItems: 'center', gap: 12,
    padding: 14, borderRadius: 12, marginBottom: 22,
  },
  serviceName: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  serviceMeta: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  modifyLink: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  sectionTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600', marginBottom: 16 },
  // Service list
  serviceItem: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 16, borderBottomWidth: 1 },
  serviceCategory: { fontFamily: 'Manrope-Bold', fontSize: 10.5, fontWeight: '800', letterSpacing: 1 },
  serviceItemName: { fontFamily: 'Manrope-SemiBold', fontSize: 15.5, fontWeight: '600', marginTop: 3 },
  serviceItemMeta: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  serviceItemPrice: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
  reserveBtn: { height: 40, paddingHorizontal: 18, borderRadius: 999, borderWidth: 1, justifyContent: 'center' },
  reserveBtnText: { fontFamily: 'Manrope-Bold', fontSize: 13.5, fontWeight: '700' },
  // Staff list
  staffItem: { flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 14, borderBottomWidth: 1 },
  staffAvatar: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' },
  staffName: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  staffSpecialty: { fontFamily: 'Manrope-Regular', fontSize: 12, marginTop: 2 },
  emptyText: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', marginTop: 24, paddingHorizontal: 20 },
  // Month
  monthRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 },
  monthTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 22, fontWeight: '600' },
  navBtn: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  // Days
  daysRow: { flexDirection: 'row', gap: 8, marginBottom: 26 },
  dayCell: { flex: 1, alignItems: 'center', paddingVertical: 10, paddingBottom: 12, borderRadius: 12 },
  dayOff: { borderWidth: 1, borderStyle: 'dashed' as const },
  dayAbbr: { fontFamily: 'Manrope-Bold', fontSize: 10.5, fontWeight: '700', letterSpacing: 0.5 },
  dayNum: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600', marginTop: 3 },
  // Slots
  slotsOverline: {
    fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700',
    letterSpacing: 2, textTransform: 'uppercase', marginBottom: 12,
  },
  slotsGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  slot: { width: '31%', alignItems: 'center', paddingVertical: 13, borderRadius: 8 },
  slotText: { fontFamily: 'Manrope-Bold', fontSize: 15, fontWeight: '700' },
  // Summary card
  summaryCard: { borderRadius: 16, borderWidth: 1, padding: 16, marginBottom: 20 },
  summaryRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 8 },
  summaryLabel: { fontFamily: 'Manrope-Regular', fontSize: 12, width: 70 },
  summaryValue: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', flex: 1 },
  summaryDivider: { height: 1, marginVertical: 2 },
  // Error
  errorCard: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 14, borderRadius: 12, marginBottom: 16 },
  errorText: { fontFamily: 'Manrope-SemiBold', fontSize: 13, fontWeight: '600', flex: 1 },
  // Notes
  notesLabel: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600', marginBottom: 8 },
  notesInput: { borderWidth: 1, borderRadius: 12, padding: 14, minHeight: 80, fontSize: 14, fontFamily: 'Manrope-Regular' },
  // Bottom
  bottomBar: {
    borderTopWidth: 1,
    paddingHorizontal: 20, paddingVertical: 14, paddingBottom: 18,
    flexDirection: 'row', alignItems: 'center', gap: 14,
  },
  bottomDate: { fontFamily: 'Manrope-Regular', fontSize: 12 },
  bottomPrice: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  continueBtn: { shadowColor: 'rgb(39,26,44)', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.12, shadowRadius: 6, elevation: 2 }, // design-fixed
  // Success screen
  successContent: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 32 },
  successIcon: { width: 100, height: 100, borderRadius: 50, alignItems: 'center', justifyContent: 'center', marginBottom: 24 },
  successTitle: { fontFamily: 'CormorantGaramond-SemiBold', fontSize: 28, fontWeight: '600', textAlign: 'center', marginBottom: 8 },
  successMessage: { fontFamily: 'Manrope-Regular', fontSize: 14, textAlign: 'center', lineHeight: 20, marginBottom: 24 },
  successCard: { borderWidth: 1, borderRadius: 16, padding: 20, width: '100%', gap: 6, marginBottom: 32 },
  successServiceName: { fontFamily: 'Manrope-SemiBold', fontSize: 16, fontWeight: '600' },
  successDetail: { fontFamily: 'Manrope-Regular', fontSize: 13 },
  successPrice: { fontFamily: 'Manrope-Bold', fontSize: 18, fontWeight: '700', marginTop: 4 },
  successActions: { width: '100%', gap: 12 },
});
