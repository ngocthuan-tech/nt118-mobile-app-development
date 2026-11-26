import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

/** Mọi ngày giờ hiển thị theo giờ Việt Nam, bất kể máy admin đặt múi giờ nào. */
export const VN_TZ = 'Asia/Ho_Chi_Minh';

/** "2026-11-26T03:15:00Z" → "26/11/2026 10:15". */
export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = dayjs(iso);
  return d.isValid() ? d.tz(VN_TZ).format('DD/MM/YYYY HH:mm') : '—';
}

/** "2026-11-26" (đã là ngày Việt Nam do server tính) → "26/11/2026". */
export function formatDate(date: string | null | undefined): string {
  if (!date) return '—';
  const d = dayjs(date);
  return d.isValid() ? d.format('DD/MM/YYYY') : '—';
}

/** Nhãn trục biểu đồ: "26/11". */
export function shortDay(date: string): string {
  const d = dayjs(date);
  return d.isValid() ? d.format('DD/MM') : date;
}

/** 125 → "2:05". */
export function formatDuration(sec: number): string {
  const s = Math.max(0, Math.round(sec));
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
}

/** 0.756 → "76%". */
export function formatPercent01(value: number): string {
  if (!Number.isFinite(value)) return '—';
  return `${Math.round(Math.max(0, Math.min(1, value)) * 100)}%`;
}

export function formatNumber(n: number): string {
  return new Intl.NumberFormat('vi-VN').format(n);
}

/** "3 × 15 lần" hoặc "3 × 45 giây" (plank tính bằng giây). */
export function volume(sets: number, reps: number, measureType: string): string {
  return `${sets} × ${reps} ${measureType === 'seconds' ? 'giây' : 'lần'}`;
}

/** Chữ cái đầu cho avatar: "đức huy" → "Đ". */
export function initial(name: string | null | undefined): string {
  const t = (name ?? '').trim();
  return t ? Array.from(t)[0].toLocaleUpperCase('vi') : '?';
}
