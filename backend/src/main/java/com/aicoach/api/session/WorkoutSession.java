package com.aicoach.api.session;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Một buổi tập đã hoàn thành. Tên field khớp model WorkoutSession bên app. */
@Entity
@Table(name = "workout_sessions", indexes = {
        @Index(name = "idx_session_user_time", columnList = "user_id, started_at")
})
public class WorkoutSession {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(nullable = false, length = 40)
    private String exerciseId;
    @Column(length = 60)
    private String exerciseName;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    private int durationSec;
    private int targetReps;
    private int totalReps;
    private int correctReps;
    private double accuracy;        // 0..1
    private double calories;
    @Column(length = 64)
    private String planId;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ElementCollection
    @CollectionTable(name = "session_form_errors", joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "error_code", length = 40)
    @Column(name = "error_count")
    private Map<String, Long> formErrors = new HashMap<>();

    public String getId() { return id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int totalReps) { this.totalReps = totalReps; }

    public int getCorrectReps() { return correctReps; }
    public void setCorrectReps(int correctReps) { this.correctReps = correctReps; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public Instant getCreatedAt() { return createdAt; }

    public Map<String, Long> getFormErrors() { return formErrors; }
    public void setFormErrors(Map<String, Long> formErrors) { this.formErrors = formErrors; }
}
