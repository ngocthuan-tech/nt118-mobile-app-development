package com.aicoach.ui.workout;

import androidx.annotation.NonNull;

import java.util.Objects;

/** 1 dòng "Phân tích từ AI" trên màn Kết quả: lỗi nhẹ (WARNING), lỗi nặng (ERROR) hoặc điểm làm tốt (GOOD). */
public final class AiFeedbackItem {

    public enum Type { WARNING, ERROR, GOOD }

    public final Type type;
    public final String title;
    public final String detail;

    public AiFeedbackItem(@NonNull Type type, @NonNull String title, @NonNull String detail) {
        this.type = type;
        this.title = title;
        this.detail = detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiFeedbackItem)) return false;
        AiFeedbackItem that = (AiFeedbackItem) o;
        return type == that.type && title.equals(that.title) && detail.equals(that.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, title, detail);
    }
}
