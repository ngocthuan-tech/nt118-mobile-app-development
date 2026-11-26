import axios, { AxiosError } from 'axios';
import type { ApiEnvelope } from './types';

/**
 * Token admin để trong sessionStorage (đóng tab là hết), KHÔNG để localStorage — giảm rủi ro token bị đọc trộm lâu dài.
 * Không bao giờ in token / mật khẩu ra console.
 */
const TOKEN_KEY = 'aicoach_admin_token';

export const tokenStore = {
  get: (): string | null => sessionStorage.getItem(TOKEN_KEY),
  set: (token: string) => sessionStorage.setItem(TOKEN_KEY, token),
  clear: () => sessionStorage.removeItem(TOKEN_KEY),
};

/** Gọi cùng tên miền với web (Caddy chuyển /api tới Spring Boot) → không cần CORS. */
export const http = axios.create({ baseURL: '/api/v1', timeout: 20000 });

http.interceptors.request.use((config) => {
  const token = tokenStore.get();
  if (token && !config.headers.Authorization) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

/** Hết phiên (401) hoặc mất quyền (403) ở một API admin → xoá token, về màn đăng nhập. */
let onSessionEnded: ((reason: string) => void) | null = null;
export function setSessionEndedHandler(handler: (reason: string) => void) {
  onSessionEnded = handler;
}

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiEnvelope<unknown>>) => {
    const status = error.response?.status;
    const isLogin = error.config?.url?.includes('/auth/login');
    if (!isLogin && (status === 401 || status === 403) && tokenStore.get()) {
      tokenStore.clear();
      onSessionEnded?.(messageOf(error));
    }
    return Promise.reject(error);
  },
);

/** Câu báo lỗi tiếng Việt: ưu tiên "message" server trả trong envelope. */
export function messageOf(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const body = error.response?.data as ApiEnvelope<unknown> | undefined;
    if (body && typeof body.message === 'string' && body.message.trim()) return body.message;
    if (!error.response) return 'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.';
    if (error.response.status === 403) return 'Tài khoản không có quyền quản trị.';
    if (error.response.status >= 500) return 'Máy chủ đang gặp sự cố, thử lại sau ít phút.';
  }
  return 'Đã có lỗi xảy ra, vui lòng thử lại.';
}

/** Lấy phần data trong envelope { success, message, data }. */
export async function unwrap<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const res = await promise;
  return res.data.data;
}
