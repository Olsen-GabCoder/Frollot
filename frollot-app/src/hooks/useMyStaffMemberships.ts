import { useState, useEffect, useCallback } from 'react';
import { salonsApi } from '../api/salons';
import { useAuthStore } from '../stores/authStore';

export interface StaffMembership {
  salonId: string;
  salonName: string;
  staffId: string;
}

export function useMyStaffMemberships() {
  const user = useAuthStore((s) => s.user);
  const [memberships, setMemberships] = useState<StaffMembership[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!user) { setIsLoading(false); return; }
    setIsLoading(true);
    setError(null);
    try {
      const salons = await salonsApi.getMySalons();
      const results: StaffMembership[] = [];
      await Promise.all(
        salons.map(async (salon) => {
          if (salon.ownerId === user.id) return;
          try {
            const staff = await salonsApi.getSalonStaff(salon.id);
            const me = staff.find((s) => s.userId === user.id && s.isActive);
            if (me) {
              results.push({ salonId: salon.id, salonName: salon.name, staffId: me.id });
            }
          } catch {}
        }),
      );
      setMemberships(results);
    } catch (e: any) {
      setError(e?.message || 'error');
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  useEffect(() => { load(); }, [load]);

  return { memberships, isLoading, error, reload: load };
}
