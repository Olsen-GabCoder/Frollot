import { useState, useEffect, useMemo } from 'react';
import { salonsApi } from '../api/salons';

interface PermissionsState {
  role: string;
  permissions: string[];
  isLoading: boolean;
  error: unknown;
}

const EMPTY_PERMISSIONS: string[] = [];

export function usePermissions(salonId: string | null | undefined) {
  const [state, setState] = useState<PermissionsState>({
    role: 'none',
    permissions: EMPTY_PERMISSIONS,
    isLoading: false,
    error: null,
  });

  useEffect(() => {
    if (!salonId) {
      setState({ role: 'none', permissions: EMPTY_PERMISSIONS, isLoading: false, error: null });
      return;
    }

    let cancelled = false;
    setState((prev) => ({ ...prev, isLoading: true, error: null }));

    salonsApi
      .getMyPermissions(salonId)
      .then((data) => {
        if (!cancelled) {
          setState({
            role: data.role,
            permissions: data.permissions,
            isLoading: false,
            error: null,
          });
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          if (__DEV__) {
            console.warn('[usePermissions] fetch failed for salon', salonId, err);
          }
          setState({ role: 'none', permissions: EMPTY_PERMISSIONS, isLoading: false, error: err });
        }
      });

    return () => {
      cancelled = true;
    };
  }, [salonId]);

  return useMemo(
    () => ({
      role: state.role,
      permissions: state.permissions,
      isLoading: state.isLoading,
      error: state.error,
      can: (permissionKey: string): boolean => state.permissions.includes(permissionKey),
      isOwner: state.role === 'owner',
    }),
    [state],
  );
}
