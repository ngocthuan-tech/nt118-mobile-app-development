package com.aicoach.ui.plan;

import androidx.annotation.NonNull;

import com.aicoach.data.model.Plan;

import java.util.List;
import java.util.Objects;

/** Dữ liệu hiển thị 1 buổi tập (UI model) cho PlanSessionAdapter. */
public final class PlanSessionItem {

    public enum State { DONE, TODAY, UPCOMING, LOCKED }

    public final int dayIndex;
    public final int sessionNumber;
    @NonNull public final String dayLabel;
    @NonNull public final String exerciseNames;
    public final int minutes;
    @NonNull public final List<Plan.PlanItem> items;
    @NonNull public final State state;
    /** true = màn 17 (tên bài làm tiêu đề, hiện 3 ảnh nhỏ); false = màn 26 ("Buổi 5" làm tiêu đề). */
    public final boolean compact;
    public final boolean highlighted;

    public PlanSessionItem(int dayIndex, int sessionNumber, @NonNull String dayLabel, @NonNull String exerciseNames,
                           int minutes, @NonNull List<Plan.PlanItem> items, @NonNull State state,
                           boolean compact, boolean highlighted) {
        this.dayIndex = dayIndex;
        this.sessionNumber = sessionNumber;
        this.dayLabel = dayLabel;
        this.exerciseNames = exerciseNames;
        this.minutes = minutes;
        this.items = items;
        this.state = state;
        this.compact = compact;
        this.highlighted = highlighted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanSessionItem)) return false;
        PlanSessionItem that = (PlanSessionItem) o;
        return dayIndex == that.dayIndex && sessionNumber == that.sessionNumber && minutes == that.minutes
                && compact == that.compact && highlighted == that.highlighted && state == that.state
                && dayLabel.equals(that.dayLabel) && exerciseNames.equals(that.exerciseNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayIndex, sessionNumber, dayLabel, exerciseNames, minutes, state, compact, highlighted);
    }
}
