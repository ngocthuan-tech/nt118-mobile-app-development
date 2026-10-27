package com.aicoach.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Luật kiểm tra dữ liệu nhập ở app — khớp với luật server (server vẫn kiểm tra lại).
 * Viết bằng Java thuần (không dùng android.util.Patterns) để chạy được unit test.
 */
public final class ValidationUtils {

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 64;

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtils() {
    }

    /** "  Minh@Gmail.COM " → "minh@gmail.com". */
    @NonNull
    public static String normalizeEmail(@Nullable String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(@Nullable String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean hasMinLength(@Nullable String p) {
        return p != null && p.length() >= PASSWORD_MIN;
    }

    public static boolean hasLetter(@Nullable String p) {
        return p != null && p.matches(".*\\p{L}.*");
    }

    public static boolean hasUpperAndLower(@Nullable String p) {
        return p != null && p.matches(".*\\p{Lu}.*") && p.matches(".*\\p{Ll}.*");
    }

    public static boolean hasDigit(@Nullable String p) {
        return p != null && p.matches(".*\\d.*");
    }

    public static boolean hasSpecial(@Nullable String p) {
        return p != null && p.matches(".*[^\\p{L}\\d\\s].*");
    }

    /** Luật bắt buộc (giống server): 8–64 ký tự, có chữ và có số. */
    public static boolean isValidPassword(@Nullable String p) {
        return p != null && p.length() >= PASSWORD_MIN && p.length() <= PASSWORD_MAX
                && hasLetter(p) && hasDigit(p);
    }

    /** Độ mạnh 0–4 cho thanh 4 vạch (Figma 10, 12): mỗi tiêu chí đạt được 1 vạch. */
    public static int strength(@Nullable String p) {
        if (p == null || p.isEmpty()) return 0;
        int score = 0;
        if (hasMinLength(p)) score++;
        if (hasUpperAndLower(p)) score++;
        if (hasDigit(p)) score++;
        if (hasSpecial(p)) score++;
        return score;
    }
}
