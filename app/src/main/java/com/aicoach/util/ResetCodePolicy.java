package com.aicoach.util;

/** Luật mã và đồng hồ chờ. Hiệu lực thực tế của mã luôn do server kiểm tra. */
public final class ResetCodePolicy {
    private ResetCodePolicy() { }
    public static boolean isValidCode(String code) {
        return code != null && code.matches("[0-9]{6}");
    }
    public static long deadline(long nowMillis, int seconds) {
        return nowMillis + Math.max(0, seconds) * 1000L;
    }
    public static int secondsLeft(long deadlineMillis, long nowMillis) {
        long remaining = Math.max(0, deadlineMillis - nowMillis);
        return (int) Math.min(Integer.MAX_VALUE, (remaining + 999L) / 1000L);
    }
}
