package com.aicoach.pose;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tổng kết một buổi tập — đúng các trường của POST /sessions (CreateSessionRequest).
 * Bài tính giây (Plank): totalReps = số giây giữ tư thế, correctReps = số giây giữ ĐÚNG.
 */
public final class WorkoutSummary {

    public final String exerciseId;
    public final String exerciseName;
    public final long startedAtMs;
    public final int durationSec;
    public final int setsDone;
    public final int targetReps;
    public final int totalReps;
    public final int correctReps;
    public final boolean timed;
    /** key lỗi (FormIssue.key) → số lần. */
    public final Map<String, Long> formErrors;

    public WorkoutSummary(String exerciseId, String exerciseName, long startedAtMs, int durationSec, int setsDone,
                          int targetReps, int totalReps, int correctReps, boolean timed,
                          Map<String, Long> formErrors) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.startedAtMs = startedAtMs;
        this.durationSec = durationSec;
        this.setsDone = setsDone;
        this.targetReps = targetReps;
        this.totalReps = totalReps;
        this.correctReps = correctReps;
        this.timed = timed;
        this.formErrors = Collections.unmodifiableMap(new LinkedHashMap<>(formErrors));
    }

    /** Điểm tư thế 0–100 = tỉ lệ lần đúng. */
    public int postureScore() {
        return totalReps == 0 ? 0 : Math.round(correctReps * 100f / totalReps);
    }

    /** % hoàn thành so với mục tiêu cả buổi. */
    public int completionPercent() {
        return targetReps == 0 ? 0 : Math.min(100, Math.round(correctReps * 100f / targetReps));
    }
}
