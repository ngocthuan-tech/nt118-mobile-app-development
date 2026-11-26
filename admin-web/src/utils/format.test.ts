import { describe, expect, it } from 'vitest';
import { formatDate, formatDateTime, formatDuration, formatPercent01, initial, shortDay, volume } from './format';
import { formErrorLabel, labelOf, MUSCLE_GROUPS } from '../constants/labels';

describe('định dạng hiển thị', () => {
  it('đổi giờ UTC của server sang giờ Việt Nam', () => {
    expect(formatDateTime('2026-11-25T18:30:00Z')).toBe('26/11/2026 01:30');
    expect(formatDateTime(null)).toBe('—');
    expect(formatDateTime('không phải ngày')).toBe('—');
  });

  it('ngày và nhãn trục biểu đồ', () => {
    expect(formatDate('2026-11-26')).toBe('26/11/2026');
    expect(shortDay('2026-11-26')).toBe('26/11');
  });

  it('thời lượng, phần trăm, khối lượng bài', () => {
    expect(formatDuration(125)).toBe('2:05');
    expect(formatDuration(-3)).toBe('0:00');
    expect(formatPercent01(0.756)).toBe('76%');
    expect(formatPercent01(1.4)).toBe('100%');
    expect(volume(3, 15, 'reps')).toBe('3 × 15 lần');
    expect(volume(3, 45, 'seconds')).toBe('3 × 45 giây');
  });

  it('chữ cái đầu tiếng Việt', () => {
    expect(initial('  đức huy')).toBe('Đ');
    expect(initial('')).toBe('?');
  });
});

describe('nhãn tiếng Việt', () => {
  it('mã lỗi AI quen và lạ', () => {
    expect(formErrorLabel('knees_in')).toBe('Gối chụm vào trong');
    expect(formErrorLabel('ma_moi_chua_co')).toBe('ma_moi_chua_co');
  });

  it('nhãn có / không có', () => {
    expect(labelOf(MUSCLE_GROUPS, 'legs')).toBe('Chân – mông');
    expect(labelOf(MUSCLE_GROUPS, null)).toBe('—');
    expect(labelOf(MUSCLE_GROUPS, 'arms')).toBe('arms');
  });
});
