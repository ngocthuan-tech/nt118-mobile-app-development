package com.aicoach.pose;

import androidx.annotation.Nullable;

/** Kết quả một hiệp vừa xong — hiển thị ở màn Nghỉ giữa hiệp (Figma 32). */
public final class SetResult {

    public final int setNumber;
    public final int sets;
    /** Số lần đúng (hoặc số giây giữ đúng) trong hiệp. */
    public final int done;
    public final int target;
    /** Số lần đã làm trong hiệp, cả đúng lẫn sai (bài giây: số giây giữ tư thế). */
    public final int attempts;
    /** Số lần mắc lỗi trong hiệp. */
    public final int errors;
    /** Lỗi gặp nhiều nhất trong hiệp, null nếu không có lỗi. */
    @Nullable public final FormIssue topIssue;
    public final boolean timed;

    public SetResult(int setNumber, int sets, int done, int target, int attempts, int errors,
                     @Nullable FormIssue topIssue, boolean timed) {
        this.setNumber = setNumber;
        this.sets = sets;
        this.done = done;
        this.target = target;
        this.attempts = attempts;
        this.errors = errors;
        this.topIssue = topIssue;
        this.timed = timed;
    }

    /** Điểm tư thế của riêng hiệp này (0–100). */
    public int score() {
        return attempts <= 0 ? 100 : Math.min(100, Math.round(done * 100f / attempts));
    }
}
