package com.aicoach.ui.home;

import androidx.annotation.NonNull;

import java.util.Objects;

/** Dữ liệu hiển thị cho 1 thẻ bài tập (UI model, không phải model API). */
public final class ExerciseCardItem {

    public final String exerciseId;
    public final String name;
    public final String volume;      // ví dụ "3 × 15 lần"

    public ExerciseCardItem(@NonNull String exerciseId, @NonNull String name, @NonNull String volume) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.volume = volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseCardItem)) return false;
        ExerciseCardItem that = (ExerciseCardItem) o;
        return exerciseId.equals(that.exerciseId) && name.equals(that.name) && volume.equals(that.volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exerciseId, name, volume);
    }
}
