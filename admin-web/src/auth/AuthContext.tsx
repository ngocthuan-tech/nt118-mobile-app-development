import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { getMe, login as apiLogin } from '../api/admin';
import { messageOf, setSessionEndedHandler, tokenStore } from '../api/client';
import type { AdminMe } from '../api/types';
import axios from 'axios';

interface AuthState {
  admin: AdminMe | null;
  /** Đang kiểm tra token còn trong sessionStorage (mở lại tab / F5). */
  checking: boolean;
  /** Lý do vừa bị đăng xuất (hết phiên, mất quyền) để màn đăng nhập hiện cho admin biết. */
  notice: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [admin, setAdmin] = useState<AdminMe | null>(null);
  const [checking, setChecking] = useState<boolean>(() => tokenStore.get() !== null);
  const [notice, setNotice] = useState<string | null>(null);

  useEffect(() => {
    setSessionEndedHandler((reason) => {
      setAdmin(null);
      setNotice(reason);
    });
    if (!tokenStore.get()) return;
    getMe()
      .then(setAdmin)
      .catch(() => tokenStore.clear())
      .finally(() => setChecking(false));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setNotice(null);
    const { accessToken } = await apiLogin(email.trim(), password);
    try {
      // Đăng nhập được API chung chưa đủ: phải là ADMIN (server trả 403 với tài khoản thường)
      const me = await getMe(accessToken);
      tokenStore.set(accessToken);
      setAdmin(me);
    } catch (e) {
      tokenStore.clear();
      if (axios.isAxiosError(e) && e.response?.status === 403) {
        throw new Error('Tài khoản này không có quyền quản trị.');
      }
      throw new Error(messageOf(e));
    }
  }, []);

  const logout = useCallback(() => {
    tokenStore.clear();
    setAdmin(null);
    setNotice(null);
  }, []);

  const value = useMemo(() => ({ admin, checking, notice, login, logout }), [admin, checking, notice, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải nằm trong AuthProvider');
  return ctx;
}
