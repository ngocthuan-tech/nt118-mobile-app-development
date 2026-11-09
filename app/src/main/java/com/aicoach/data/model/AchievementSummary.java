package com.aicoach.data.model;

import java.util.Collections;
import java.util.List;

/** Lifetime totals from users/me/achievements, not the paginated history. */
public final class AchievementSummary {
    private Long totalXp;
    private List<ExerciseProgress> exercises;
    public long getTotalXp() { return totalXp == null ? 0 : Math.max(0, totalXp); }
    public List<ExerciseProgress> getExercises() {
        return exercises == null ? Collections.emptyList() : Collections.unmodifiableList(exercises);
    }
    public boolean isValid() {
        if (totalXp == null || totalXp < 0 || exercises == null) return false;
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ExerciseProgress e : exercises) {
            if (e == null || e.exerciseId == null || e.exerciseId.trim().isEmpty()
                    || e.exerciseName == null || e.totalXp == null || e.totalXp < 0 || !seen.add(e.exerciseId)) return false;
        }
        return true;
    }
    public static final class ExerciseProgress {
        private String exerciseId, exerciseName, imageName, measureType;
        private Long totalXp;
        public String getExerciseId() { return exerciseId; }
        public String getExerciseName() { return exerciseName; }
        public String getImageName() { return imageName == null ? exerciseId : imageName; }
        public long getTotalXp() { return totalXp == null ? 0 : Math.max(0, totalXp); }
    }
}
