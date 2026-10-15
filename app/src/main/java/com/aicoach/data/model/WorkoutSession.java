package com.aicoach.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.HashMap;
import java.util.Map;

/**
 * HỢP ĐỒNG giữa Trưởng nhóm và Thành viên B — document users/{uid}/sessions/{sessionId}.
 * Trưởng nhóm điền object này ở màn Kết quả (S18), rồi gọi
 * SessionRepository.saveCompletedSession(session) do Thành viên B viết.
 * KHÔNG đổi tên field khi chưa báo cả nhóm.
 */
public class WorkoutSession {

    @DocumentId
    private String id;
    private String exerciseId;
    private String exerciseName;
    private Timestamp startedAt;
    private int durationSec;
    private int targetReps;
    private int totalReps;
    private int correctReps;
    private double accuracy;                            // 0..1 = correctReps / totalReps
    private double calories;
    private Map<String, Long> formErrors = new HashMap<>();  // mã lỗi -> số lần, ví dụ "knee_over_toe" -> 3
    private String planId;                              // null nếu tập lẻ, không theo kế hoạch

    public WorkoutSession() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

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

    public Map<String, Long> getFormErrors() { return formErrors; }
    public void setFormErrors(Map<String, Long> formErrors) { this.formErrors = formErrors; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}
