package com.aicoach.ui.exercise;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aicoach.data.model.Exercise;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Bộ lọc Thư viện (Figma 23 chip nhóm cơ + 24 Bộ lọc). Lọc ở app trên danh sách server trả về. */
public final class ExerciseFilter {

    public final Set<String> groups = new LinkedHashSet<>();
    public final Set<String> difficulties = new LinkedHashSet<>();
    /** null = tất cả, "reps" = đếm số lần, "seconds" = tính giờ. */
    @Nullable public String measure;
    public boolean noEquipment;

    @NonNull
    public ExerciseFilter copy() {
        ExerciseFilter f = new ExerciseFilter();
        f.groups.addAll(groups);
        f.difficulties.addAll(difficulties);
        f.measure = measure;
        f.noEquipment = noEquipment;
        return f;
    }

    public boolean isEmpty() {
        return groups.isEmpty() && difficulties.isEmpty() && measure == null && !noEquipment;
    }

    public boolean matches(@NonNull Exercise e) {
        if (!groups.isEmpty() && !groups.contains(e.getMuscleGroup())) return false;
        if (!difficulties.isEmpty() && !difficulties.contains(e.getDifficulty())) return false;
        if (measure != null && !measure.equals(e.getMeasureType())) return false;
        return !noEquipment || !ExerciseUi.NEEDS_EQUIPMENT.equals(e.getId());
    }

    @NonNull
    public List<Exercise> apply(@Nullable List<Exercise> all) {
        List<Exercise> out = new ArrayList<>();
        if (all == null) return out;
        for (Exercise e : all) if (matches(e)) out.add(e);
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseFilter)) return false;
        ExerciseFilter f = (ExerciseFilter) o;
        return noEquipment == f.noEquipment && groups.equals(f.groups) && difficulties.equals(f.difficulties)
                && Objects.equals(measure, f.measure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups, difficulties, measure, noEquipment);
    }
}
