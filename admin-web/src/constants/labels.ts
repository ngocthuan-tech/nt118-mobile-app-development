/**
 * Mã lỗi tư thế AI → tên tiếng Việt (khớp FormIssue.java trong app Android).
 * Mã lạ (sau này thêm vào app) → hiện nguyên mã, không lỗi trang.
 */
export const FORM_ERRORS: Record<string, string> = {
  squat_depth: 'Squat chưa xuống đủ sâu',
  knees_in: 'Gối chụm vào trong',
  torso_lean: 'Ngả người quá nhiều',
  hip_sag: 'Võng hông',
  hip_pike: 'Nhô mông quá cao',
  pushup_depth: 'Hít đất chỉ hạ nửa chừng',
  lunge_depth: 'Lunge gối chưa gập đủ',
  bridge_height: 'Cầu mông chưa nâng hông đủ',
  dip_depth: 'Chống ghế chưa hạ đủ sâu',
  arms_low: 'Bật nhảy tay chưa đưa lên hết',
  legs_closed: 'Bật nhảy chân chưa dang rộng',
  knee_low: 'Nâng gối quá thấp',
  toe_reach: 'Chưa với tới mũi chân',
  // Mã cũ trong dữ liệu mẫu của tài khoản demo
  knee_over_toe: 'Gối vượt quá mũi chân',
};

export function formErrorLabel(key: string): string {
  return FORM_ERRORS[key] ?? key;
}

export const MUSCLE_GROUPS: Record<string, string> = {
  legs: 'Chân – mông',
  chest_arms: 'Ngực – tay',
  core: 'Bụng – lưng',
  cardio: 'Cardio',
};

export const DIFFICULTIES: Record<string, string> = { easy: 'Dễ', medium: 'Trung bình', hard: 'Khó' };

export const LEVELS: Record<string, string> = {
  beginner: 'Người mới',
  intermediate: 'Trung bình',
  advanced: 'Nâng cao',
};

export const GOALS: Record<string, string> = {
  lose_weight: 'Giảm cân',
  build_muscle: 'Tăng cơ',
  stay_fit: 'Giữ dáng',
};

export function labelOf(map: Record<string, string>, key: string | null | undefined): string {
  if (!key) return '—';
  return map[key] ?? key;
}
