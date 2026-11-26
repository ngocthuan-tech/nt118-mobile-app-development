package com.aicoach.common;

/**
 * Tín hiệu "vừa lưu thêm một buổi tập". Màn Tập AI (Trưởng nhóm) bật tín hiệu sau POST /sessions;
 * Trang chủ (A) và Tiến độ (B) kiểm tra khi quay lại màn để tải lại số liệu mới.
 * Chỉ đọc/ghi trên main thread nên dùng một bộ đếm phiên bản đơn giản là đủ.
 */
public final class WorkoutSavedSignal {

    private static volatile long version;

    private WorkoutSavedSignal() {
    }

    public static void markChanged() {
        version++;
    }

    /** Mỗi lần lưu buổi tập thì số này tăng — so sánh với số đã nhớ để biết dữ liệu đã cũ. */
    public static long version() {
        return version;
    }
}
