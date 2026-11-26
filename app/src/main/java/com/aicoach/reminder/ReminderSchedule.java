package com.aicoach.reminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Lịch nhắc tập — thuần Java để kiểm thử trên máy (không cần Android).
 * Dữ liệu lấy từ cài đặt người dùng chọn ở Khảo sát 15 / Hồ sơ: giờ "19:00", ngày "T2,T4,T6" (CN = Chủ nhật).
 */
public final class ReminderSchedule {

    /** Máy tắt / Doze lâu rồi mới chạy: quá 3 tiếng sau giờ nhắc thì bỏ, không nhắc sai giờ. */
    static final Duration LATE_LIMIT = Duration.ofHours(3);
    static final LocalTime DEFAULT_TIME = LocalTime.of(19, 0);

    private ReminderSchedule() {
    }

    /** "07:30" → 07:30; sai định dạng → 19:00. */
    @NonNull
    public static LocalTime parseTime(@Nullable String hhmm) {
        if (hhmm == null) return DEFAULT_TIME;
        String[] p = hhmm.trim().split(":");
        if (p.length != 2) return DEFAULT_TIME;
        try {
            int h = Integer.parseInt(p[0].trim());
            int m = Integer.parseInt(p[1].trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) return DEFAULT_TIME;
            return LocalTime.of(h, m);
        } catch (NumberFormatException e) {
            return DEFAULT_TIME;
        }
    }

    /** "T2,T4,CN" → {Thứ 2, Thứ 4, Chủ nhật}; nhãn lạ bị bỏ qua. */
    @NonNull
    public static Set<DayOfWeek> parseDays(@Nullable String csv) {
        Set<DayOfWeek> out = EnumSet.noneOf(DayOfWeek.class);
        if (csv == null) return out;
        for (String raw : csv.split(",")) {
            String d = raw.trim().toUpperCase(java.util.Locale.ROOT);
            if (d.equals("CN")) {
                out.add(DayOfWeek.SUNDAY);
            } else if (d.length() == 2 && d.charAt(0) == 'T' && d.charAt(1) >= '2' && d.charAt(1) <= '7') {
                out.add(DayOfWeek.of(d.charAt(1) - '1'));       // T2 → MONDAY(1) … T7 → SATURDAY(6)
            }
        }
        return out;
    }

    /** Lần nhắc kế tiếp SAU {@code now}: đúng giờ đã chọn, vào một ngày đã chọn. Không chọn ngày nào → null. */
    @Nullable
    public static LocalDateTime next(@NonNull LocalDateTime now, @NonNull LocalTime time, @NonNull Set<DayOfWeek> days) {
        if (days.isEmpty()) return null;
        for (int i = 0; i <= 7; i++) {
            LocalDateTime candidate = now.toLocalDate().plusDays(i).atTime(time);
            if (candidate.isAfter(now) && days.contains(candidate.getDayOfWeek())) return candidate;
        }
        return null;
    }

    /** Có nên hiện thông báo lúc {@code now} không: hôm nay là ngày tập, đã tới giờ và chưa trễ quá 3 tiếng. */
    public static boolean shouldNotify(@NonNull LocalDateTime now, @NonNull LocalTime time, @NonNull Set<DayOfWeek> days) {
        if (!days.contains(now.getDayOfWeek())) return false;
        LocalDateTime due = now.toLocalDate().atTime(time);
        // Cho chạy sớm 1 phút (đồng hồ hệ thống lệch nhẹ) nhưng không quá trễ
        return !now.isBefore(due.minusMinutes(1)) && !now.isAfter(due.plus(LATE_LIMIT));
    }
}
