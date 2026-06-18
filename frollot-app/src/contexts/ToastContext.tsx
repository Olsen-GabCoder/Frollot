import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import { Toast, type ToastType } from '../components/ui/Toast';

interface ToastState {
  message: string;
  type: ToastType;
  visible: boolean;
}

interface ToastContextValue {
  showToast: (message: string, type?: ToastType) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<ToastState>({ message: '', type: 'info', visible: false });
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const showToast = useCallback((message: string, type: ToastType = 'info') => {
    // Clear any pending dismiss
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    // Reset then show (handles rapid successive toasts)
    setToast({ message: '', type: 'info', visible: false });
    requestAnimationFrame(() => {
      setToast({ message, type, visible: true });
    });
  }, []);

  const handleDismiss = useCallback(() => {
    setToast((prev) => ({ ...prev, visible: false }));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <Toast
        message={toast.message}
        type={toast.type}
        visible={toast.visible}
        onDismiss={handleDismiss}
      />
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
}
